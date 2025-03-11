#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import sys
import django
from datetime import datetime, timedelta
import random

# 設置Django環境
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'glucoseBE.settings')
django.setup()

from django.utils import timezone
from django.db import connection
from account_session.models import CustomUser
from getdata.models import BloodSugarRecord, get_time_slot, BloodSugarComparison, UserModelConsent, UserPersonalizedModel

def create_default_user():
    """創建默認用戶，如果不存在的話"""
    email = "default@example.com"
    username = "default_user"
    password = "defaultpassword123"
    
    # 檢查用戶是否已經存在
    try:
        user = CustomUser.objects.get(email=email)
        print(f"用戶 {email} 已存在，跳過創建步驟。")
    except CustomUser.DoesNotExist:
        # 創建新用戶
        user = CustomUser.objects.create_user(email=email, username=username, password=password)
        print(f"創建了新用戶 {email}")
    
    # 更新用戶個人資料
    user.weight = 65.5  # 體重（kg）
    user.height = 170.0  # 身高（cm）
    user.age = 35  # 年齡
    user.save()
    
    print(f"更新了用戶 {email} 的個人資料：身高 {user.height}cm，體重 {user.weight}kg，年齡 {user.age}歲")
    return user

def create_blood_sugar_records(user, days=30):
    """為用戶創建血糖記錄"""
    # 首先檢查是否已存在記錄
    existing_records = BloodSugarRecord.objects.filter(user=user).count()
    if existing_records > 0:
        print(f"用戶 {user.email} 已有 {existing_records} 條血糖記錄，跳過創建步驟。")
        return
    
    end_date = timezone.now()
    start_date = end_date - timedelta(days=days)
    
    records_created = 0
    all_records = []  # 保存所有創建的記錄以用於創建比較數據
    
    # 生成記錄
    current_date = start_date
    while current_date <= end_date:
        # 每天創建2-4條記錄
        daily_records = random.randint(2, 4)
        
        for _ in range(daily_records):
            # 隨機時間（早上7點到晚上10點）
            hour = random.randint(7, 22)
            minute = random.choice([0, 15, 30, 45])
            record_time = current_date.replace(hour=hour, minute=minute)
            
            # 計算時間槽
            time_slot = get_time_slot(record_time)
            
            # 血糖值（模擬典型的血糖波動）
            if hour < 11:  # 早上空腹
                blood_glucose = random.uniform(90, 130)
            elif hour < 15:  # 午餐後
                blood_glucose = random.uniform(120, 170)
            else:  # 晚餐前後
                blood_glucose = random.uniform(100, 150)
            
            # 餐前餐後模式
            meal_pattern = hour % 6 <= 1  # 大約在6, 12, 18時為餐點時間
            
            # 有70%的概率會記錄碳水攝入（主要在餐點時間）
            if meal_pattern and random.random() < 0.9:
                carbohydrate_intake = round(random.uniform(30, 70), 1)
            elif random.random() < 0.3:  # 零食
                carbohydrate_intake = round(random.uniform(10, 25), 1)
            else:
                carbohydrate_intake = None
            
            # 有40%的概率會記錄運動（通常在非餐點時間）
            if not meal_pattern and random.random() < 0.5:
                exercise_duration = round(random.uniform(15, 60), 1)
            elif random.random() < 0.2:
                exercise_duration = round(random.uniform(5, 15), 1)
            else:
                exercise_duration = None
            
            # 有30%的概率會記錄胰島素注射（主要在餐點時間）
            if meal_pattern and random.random() < 0.6:
                insulin_injection = round(random.uniform(2, 5), 1)
            elif random.random() < 0.1:
                insulin_injection = round(random.uniform(1, 2), 1)
            else:
                insulin_injection = None
            
            # 創建記錄
            record = BloodSugarRecord(
                user=user,
                blood_glucose=round(blood_glucose, 1),
                carbohydrate_intake=carbohydrate_intake,
                exercise_duration=exercise_duration,
                insulin_injection=insulin_injection,
                time_slot=time_slot
            )
            
            # 手動保存記錄以獲取ID
            record.save()
            
            # 使用SQL直接更新created_at字段
            with connection.cursor() as cursor:
                cursor.execute(
                    "UPDATE getdata_bloodsugarrecord SET created_at = %s WHERE id = %s",
                    [record_time.strftime('%Y-%m-%d %H:%M:%S'), record.id]
                )
            
            # 重新獲取記錄以更新created_at值
            record = BloodSugarRecord.objects.get(id=record.id)
            all_records.append(record)
            
            records_created += 1
        
        # 進入下一天
        current_date += timedelta(days=1)
    
    print(f"為用戶 {user.email} 創建了 {records_created} 條血糖記錄")
    return all_records

def create_blood_sugar_comparisons(user, records):
    """為用戶創建血糖比較記錄"""
    # 檢查是否已存在比較記錄
    existing_comparisons = BloodSugarComparison.objects.filter(user=user).count()
    if existing_comparisons > 0:
        print(f"用戶 {user.email} 已有 {existing_comparisons} 條血糖比較記錄，跳過創建步驟。")
        return
    
    if not records or len(records) < 2:
        print("記錄不足，無法創建比較數據")
        return
    
    # 按時間排序記錄
    sorted_records = sorted(records, key=lambda r: r.created_at)
    comparisons_created = 0
    
    # 遍歷記錄創建比較數據（從第二條記錄開始）
    for i in range(1, len(sorted_records)):
        current_record = sorted_records[i]
        previous_record = sorted_records[i-1]
        
        # 計算時間間隔（只比較12小時內的記錄）
        time_interval = current_record.created_at - previous_record.created_at
        if time_interval > timedelta(hours=12):
            continue
        
        # 創建比較記錄
        comparison = BloodSugarComparison(
            user=user,
            previous_record=previous_record,
            current_record=current_record,
            previous_blood_glucose=previous_record.blood_glucose,
            current_blood_glucose=current_record.blood_glucose,
            insulin_injection=previous_record.insulin_injection,
            carbohydrate_intake=previous_record.carbohydrate_intake,
            time_interval=time_interval
        )
        comparison.save()
        comparisons_created += 1
    
    print(f"為用戶 {user.email} 創建了 {comparisons_created} 條血糖比較記錄")

def setup_model_consent(user, has_consented=True):
    """設置用戶對模型訓練的同意狀態"""
    consent, created = UserModelConsent.objects.get_or_create(user=user)
    consent.has_consented = has_consented
    if has_consented:
        consent.consent_date = timezone.now()
    consent.save()
    
    status = "同意" if has_consented else "不同意"
    print(f"用戶 {user.email} {status}模型訓練")

def setup_model_status(user):
    """設置用戶的模型訓練狀態"""
    # 創建用戶模型記錄但不設置為已訓練
    model, created = UserPersonalizedModel.objects.get_or_create(user=user)
    if created:
        model.is_trained = False
        model.save()
        print(f"為用戶 {user.email} 創建模型記錄")

def main():
    """腳本入口點"""
    # 創建用戶和血糖記錄
    user = create_default_user()
    records = create_blood_sugar_records(user)
    
    # 創建血糖比較記錄
    create_blood_sugar_comparisons(user, records)
    
    # 設置用戶同意訓練模型
    setup_model_consent(user, has_consented=True)
    
    # 初始化模型狀態
    setup_model_status(user)
    
    print("\n完成！默認用戶及其數據已成功創建。")
    print(f"用戶名: {user.username}")
    print(f"密碼: defaultpassword123")
    print(f"電子郵件: {user.email}")
    print("\n用戶已同意進行模型訓練，並且已生成足夠的血糖比較數據用於測試。")
    print("您可以使用這些憑據登錄並測試個人化模型訓練和預測功能。")

if __name__ == "__main__":
    main()