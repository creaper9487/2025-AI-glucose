from django.db import models
from django.conf import settings  # 假設 settings.AUTH_USER_MODEL 已設定為你的 CustomUser
from django.utils import timezone
from datetime import timedelta

def get_time_slot(dt):
    """
    依照 dt 時間取得固定時段
    範例：若 dt 的分鐘數 < 30，則時間槽為當小時的 30 分；
          若 dt 的分鐘數 >= 30，則時間槽為下一小時的 00 分。
    例如：10:15 -> 10:30, 10:40 -> 11:00, 11:30 -> 12:00
    """
    if dt.minute < 30:
        return dt.replace(minute=30, second=0, microsecond=0)
    else:
        # 先將分鐘、秒數、微秒設為 0，再加一小時
        return dt.replace(minute=0, second=0, microsecond=0) + timedelta(hours=1)

class BloodSugarRecord(models.Model):
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='blood_sugar_records'
    )
    # 攝取的碳水（可為 null）
    carbohydrate_intake = models.FloatField(null=True, blank=True, help_text="攝取的碳水 (g)")
    # 血糖數據，這裡假設為必填
    blood_glucose = models.FloatField(help_text="血糖數據 (mg/dL)")
    # 運動時常（可為 null），例如分鐘數
    exercise_duration = models.FloatField(null=True, blank=True, help_text="運動時常 (分鐘)")
    # 胰島素注射量（可為 null）
    insulin_injection = models.FloatField(null=True, blank=True, help_text="胰島素注射量 (單位)")
    
    created_at = models.DateTimeField(auto_now_add=True)
    # 為方便覆蓋機制，額外儲存該筆資料所屬的時間槽
    time_slot = models.DateTimeField(help_text="依照固定時段計算的時間槽")
    
    def save(self, *args, **kwargs):
        # 若尚未設定 time_slot，可依據現在時間計算
        if not self.time_slot:
            self.time_slot = get_time_slot(timezone.now())
        super().save(*args, **kwargs)
    
    def __str__(self):
        return f"{self.user.username} - {self.time_slot.strftime('%Y-%m-%d %H:%M')}"
