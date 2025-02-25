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
from getdata.models import BloodSugarRecord, get_time_slot

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
            
            # 創建記錄，但不保存
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
            
            records_created += 1
        
        # 進入下一天
        current_date += timedelta(days=1)
    
    print(f"為用戶 {user.email} 創建了 {records_created} 條血糖記錄")

def main():
    """腳本入口點"""
    user = create_default_user()
    create_blood_sugar_records(user)
    print("\n完成！默認用戶及其數據已成功創建。")
    print(f"用戶名: {user.username}")
    print(f"密碼: defaultpassword123")
    print(f"電子郵件: {user.email}")
    print("\n您可以使用這些憑據登錄並查看完整的血糖記錄。")

if __name__ == "__main__":
    main()