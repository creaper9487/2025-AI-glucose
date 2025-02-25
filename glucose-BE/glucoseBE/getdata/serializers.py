from rest_framework import serializers
from .models import BloodSugarRecord

class BloodSugarRecordSerializer(serializers.ModelSerializer):
    class Meta:
        model = BloodSugarRecord
        # user、created_at 與 time_slot 由系統自動設定，不由前端指定
        fields = ['id', 'user', 'carbohydrate_intake', 'blood_glucose',
                  'exercise_duration', 'insulin_injection', 'created_at', 'time_slot']
        read_only_fields = ('user', 'created_at', 'time_slot')
