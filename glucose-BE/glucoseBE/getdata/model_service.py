import os
import torch
import torch.nn as nn
import torch.optim as optim
import numpy as np
import pandas as pd
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error
from django.utils import timezone
from django.conf import settings
from .models import BloodSugarComparison, UserPersonalizedModel
import threading
import time
import logging
from django.db import transaction, connection

# 配置日誌
logger = logging.getLogger(__name__)

# 線程鎖和訓練狀態字典
training_status_lock = threading.Lock()
training_status = {}  # user_id -> (status, message, timestamp)
TRAINING_STATUS_EXPIRY = 24 * 60 * 60  # 24小時


# 定義相同的神經網絡模型架構
class DeepBloodGlucoseModel(nn.Module):
    def __init__(self, input_dim, output_dim=2):
        super(DeepBloodGlucoseModel, self).__init__()
        
        # 主要網絡路徑
        self.main_path = nn.Sequential(
            nn.Linear(input_dim, 128),
            nn.BatchNorm1d(128),
            nn.LeakyReLU(0.1),
            nn.Dropout(0.3),
            
            nn.Linear(128, 256),
            nn.BatchNorm1d(256),
            nn.LeakyReLU(0.1),
            nn.Dropout(0.3),
            
            nn.Linear(256, 512),
            nn.BatchNorm1d(512),
            nn.LeakyReLU(0.1),
            nn.Dropout(0.3),
            
            nn.Linear(512, 256),
            nn.BatchNorm1d(256),
            nn.LeakyReLU(0.1),
            nn.Dropout(0.3),
            
            nn.Linear(256, 128),
            nn.BatchNorm1d(128),
            nn.LeakyReLU(0.1),
            nn.Dropout(0.3),
            
            nn.Linear(128, 64),
            nn.BatchNorm1d(64),
            nn.LeakyReLU(0.1),
            nn.Dropout(0.2),
        )
        
        # 殘差連接
        self.skip_connection = nn.Sequential(
            nn.Linear(input_dim, 64),
            nn.BatchNorm1d(64),
            nn.LeakyReLU(0.1)
        )
        
        # 主網絡和殘差連接合併後的最終層
        self.final_layers = nn.Sequential(
            nn.Linear(64 * 2, 32),
            nn.BatchNorm1d(32),
            nn.LeakyReLU(0.1),
            
            nn.Linear(32, 16),
            nn.BatchNorm1d(16),
            nn.LeakyReLU(0.1),
            
            nn.Linear(16, output_dim)
        )
    
    def forward(self, x):
        main_output = self.main_path(x)
        skip_output = self.skip_connection(x)
        
        # 合併輸出
        combined = torch.cat((main_output, skip_output), dim=1)
        
        return self.final_layers(combined)

# 確保模型存儲目錄存在
def ensure_model_dir_exists():
    model_dir = os.path.join(settings.MEDIA_ROOT, 'user_models')
    if not os.path.exists(model_dir):
        os.makedirs(model_dir)
    return model_dir

# 準備用戶的訓練數據
def prepare_training_data(user):
    # 獲取用戶的血糖比較記錄
    comparisons = BloodSugarComparison.objects.filter(user=user)
    
    if comparisons.count() < 20:
        return None, None, None, None
    
    # 準備特徵和標籤
    data = []
    for comp in comparisons:
        # 特徵: 上次血糖、胰島素注射量、碳水攝取量、時間間隔（小時）
        # 標籤: 本次血糖
        row = {
            '上次血糖': comp.previous_blood_glucose,
            '胰島素注射量': comp.insulin_injection or 0,  # 若為 None 則使用 0
            '碳水攝取量': comp.carbohydrate_intake or 0,  # 若為 None 則使用 0
            '時間間隔': comp.time_interval.total_seconds() / 3600,  # 轉換為小時
            '本次血糖': comp.current_blood_glucose,
            '時間段': comp.current_record.time_slot.hour  # 添加時間段作為標籤
        }
        data.append(row)
    
    # 轉換為 DataFrame
    df = pd.DataFrame(data)
    
    # 分離特徵和標籤
    X = df[['上次血糖', '胰島素注射量', '碳水攝取量', '時間間隔']]
    y = df[['本次血糖', '時間段']]
    
    # 分割訓練集和測試集
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    # 標準化特徵
    scaler_X = StandardScaler()
    scaler_y = StandardScaler()
    
    X_train_scaled = scaler_X.fit_transform(X_train)
    X_test_scaled = scaler_X.transform(X_test)
    
    y_train_scaled = scaler_y.fit_transform(y_train)
    y_test_scaled = scaler_y.transform(y_test)
    
    return (X_train_scaled, y_train_scaled, X_test_scaled, y_test_scaled), scaler_X, scaler_y, df.shape[0]

# 訓練模型
def train_personalized_model(user):
    # 準備數據
    data, scaler_X, scaler_y, data_count = prepare_training_data(user)
    if data is None:
        return False, "數據不足，無法訓練模型"
    
    X_train_scaled, y_train_scaled, X_test_scaled, y_test_scaled = data
    
    # 定義模型
    input_dim = X_train_scaled.shape[1]
    output_dim = y_train_scaled.shape[1]
    model = DeepBloodGlucoseModel(input_dim, output_dim)
    
    # 加載基礎模型（如果存在）
    try:
        base_model_path = os.path.join(settings.BASE_DIR, 'AIchat', 'advanced_blood_glucose_model.pth')
        model.load_state_dict(torch.load(base_model_path))
        print(f"已加載基礎模型: {base_model_path}")
    except Exception as e:
        print(f"無法加載基礎模型: {e}")
    
    # 定義損失函數和優化器
    criterion = nn.MSELoss()
    optimizer = optim.Adam(model.parameters(), lr=0.001, weight_decay=1e-5)
    scheduler = optim.lr_scheduler.ReduceLROnPlateau(optimizer, mode='min', factor=0.5, patience=10, verbose=True)
    
    # 準備數據加載器
    X_train_tensor = torch.tensor(X_train_scaled, dtype=torch.float32)
    y_train_tensor = torch.tensor(y_train_scaled, dtype=torch.float32)
    X_test_tensor = torch.tensor(X_test_scaled, dtype=torch.float32)
    y_test_tensor = torch.tensor(y_test_scaled, dtype=torch.float32)
    
    # 訓練模型
    num_epochs = 200
    batch_size = 8
    
    for epoch in range(num_epochs):
        model.train()
        running_loss = 0.0
        
        # 批次處理（小數據集可以使用完整數據集）
        for i in range(0, len(X_train_tensor), batch_size):
            # 獲取批次數據
            inputs = X_train_tensor[i:i+batch_size]
            targets = y_train_tensor[i:i+batch_size]
            
            # 前向傳播
            outputs = model(inputs)
            loss = criterion(outputs, targets)
            
            # 反向傳播和優化
            optimizer.zero_grad()
            loss.backward()
            torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)
            optimizer.step()
            
            running_loss += loss.item() * inputs.size(0)
        
        # 評估模型
        model.eval()
        with torch.no_grad():
            test_outputs = model(X_test_tensor)
            test_loss = criterion(test_outputs, y_test_tensor).item()
        
        # 更新學習率
        scheduler.step(test_loss)
        
        if (epoch + 1) % 20 == 0:
            print(f'Epoch [{epoch+1}/{num_epochs}], Loss: {running_loss/len(X_train_tensor):.4f}, Test Loss: {test_loss:.4f}')
    
    # 評估模型性能
    model.eval()
    with torch.no_grad():
        y_pred_scaled = model(X_test_tensor)
        y_pred = scaler_y.inverse_transform(y_pred_scaled.numpy())
        y_true = scaler_y.inverse_transform(y_test_tensor.numpy())
        
        # 計算血糖預測的 MAE
        mae_bg = mean_absolute_error(y_true[:, 0], y_pred[:, 0])
    
    # 保存模型
    model_dir = ensure_model_dir_exists()
    model_filename = f"user_{user.id}_model.pth"
    model_path = os.path.join(model_dir, model_filename)
    torch.save(model.state_dict(), model_path)
    
    # 保存標準化器
    scaler_X_filename = f"user_{user.id}_scaler_X.joblib"
    scaler_y_filename = f"user_{user.id}_scaler_y.joblib"
    import joblib
    joblib.dump(scaler_X, os.path.join(model_dir, scaler_X_filename))
    joblib.dump(scaler_y, os.path.join(model_dir, scaler_y_filename))
    
    # 更新用戶模型記錄
    user_model, created = UserPersonalizedModel.objects.get_or_create(user=user)
    user_model.model_path = model_path
    user_model.is_trained = True
    user_model.last_trained = timezone.now()
    user_model.training_data_count = data_count
    user_model.model_version += 0 if created else 1
    user_model.model_performance = mae_bg
    user_model.save()
    
    return True, f"模型訓練成功，MAE: {mae_bg:.2f}"

# 使用用戶個人化模型進行預測
def predict_with_personalized_model(user, features):
    """
    使用用戶的個人化模型進行預測
    
    Args:
        user: 用戶對象
        features: 字典，包含 '上次血糖', '胰島素注射量', '碳水攝取量', '時間間隔' 等特徵
        
    Returns:
        tuple: (預測血糖值, 預測時間段)
    """
    try:
        # 檢查用戶是否有訓練好的模型
        user_model = UserPersonalizedModel.objects.get(user=user, is_trained=True)
        model_dir = ensure_model_dir_exists()
        
        # 載入標準化器
        import joblib
        scaler_X_path = os.path.join(model_dir, f"user_{user.id}_scaler_X.joblib")
        scaler_y_path = os.path.join(model_dir, f"user_{user.id}_scaler_y.joblib")
        
        scaler_X = joblib.load(scaler_X_path)
        scaler_y = joblib.load(scaler_y_path)
        
        # 準備輸入特徵
        input_features = np.array([[
            features['上次血糖'],
            features['胰島素注射量'],
            features['碳水攝取量'],
            features['時間間隔']
        ]])
        
        # 標準化特徵
        input_scaled = scaler_X.transform(input_features)
        
        # 載入模型
        input_dim = input_scaled.shape[1]
        output_dim = 2  # 血糖和時間段
        model = DeepBloodGlucoseModel(input_dim, output_dim)
        model.load_state_dict(torch.load(user_model.model_path))
        model.eval()
        
        # 進行預測
        with torch.no_grad():
            input_tensor = torch.tensor(input_scaled, dtype=torch.float32)
            output_scaled = model(input_tensor)
            output = scaler_y.inverse_transform(output_scaled.numpy())
        
        # 返回預測的血糖值和時間段
        return output[0][0], output[0][1]
    
    except UserPersonalizedModel.DoesNotExist:
        raise Exception("用戶沒有訓練好的個人化模型")
    except Exception as e:
        raise Exception(f"預測過程中發生錯誤: {str(e)}")
    
def clean_expired_training_status():
    """清理過期的訓練狀態"""
    current_time = time.time()
    with training_status_lock:
        expired_keys = [k for k, v in training_status.items() 
                      if current_time - v[2] > TRAINING_STATUS_EXPIRY]
        for key in expired_keys:
            del training_status[key]

def train_personalized_model_thread(user):
    """
    在單獨的線程中訓練用戶的個人化模型
    
    Args:
        user: 用戶對象
        
    Returns:
        tuple: (成功標誌, 消息)
    """
    # 清理過期狀態
    clean_expired_training_status()
    
    # 檢查是否已經有訓練在進行
    with training_status_lock:
        current_status = training_status.get(user.id, (None, None, 0))[0]
        if current_status == "training":
            return False, "模型訓練已經在進行中"
        
        # 設置初始狀態
        training_status[user.id] = ("training", "模型訓練中...", time.time())
    
    def _train_thread():
        # 關閉舊連接，讓Django在線程中創建新連接
        connection.close()
        
        try:
            # 更新用戶模型狀態為"訓練中"
            try:
                with transaction.atomic():
                    user_model, created = UserPersonalizedModel.objects.get_or_create(user=user)
                    user_model.last_trained = timezone.now()
                    user_model.save()
            except Exception as e:
                logger.error(f"更新用戶模型狀態失敗: {str(e)}")
            
            # 執行訓練
            success, message = train_personalized_model(user)
            
            # 更新訓練狀態
            with training_status_lock:
                if success:
                    training_status[user.id] = ("completed", message, time.time())
                else:
                    training_status[user.id] = ("failed", message, time.time())
        except Exception as e:
            error_msg = f"訓練過程中發生錯誤: {str(e)}"
            logger.error(error_msg)
            with training_status_lock:
                training_status[user.id] = ("error", error_msg, time.time())
    
    # 創建並啟動線程
    thread = threading.Thread(target=_train_thread)
    thread.daemon = True
    thread.start()
    
    return True, "模型訓練已開始，請稍後查詢訓練狀態"

def get_training_status(user_id):
    """
    獲取用戶模型訓練的狀態
    
    Args:
        user_id: 用戶ID
    
    Returns:
        tuple: (狀態, 消息) 或 (None, None) 如果沒有訓練記錄
    """
    with training_status_lock:
        status_data = training_status.get(user_id)
        if status_data:
            return status_data[0], status_data[1]
        return None, None