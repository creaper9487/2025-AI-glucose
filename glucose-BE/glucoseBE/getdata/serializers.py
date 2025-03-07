from rest_framework import serializers
from .models import BloodSugarRecord
from .models import BloodSugarComparison
from .models import UserModelConsent
from .models import UserPersonalizedModel
class BloodSugarRecordSerializer(serializers.ModelSerializer):
    class Meta:
        model = BloodSugarRecord
        # user、created_at 與 time_slot 由系統自動設定，不由前端指定
        fields = ['id', 'user', 'carbohydrate_intake', 'blood_glucose',
                  'exercise_duration', 'insulin_injection', 'created_at', 'time_slot']
        read_only_fields = ('user', 'created_at', 'time_slot')


class BloodSugarComparisonSerializer(serializers.ModelSerializer):
    time_interval_hours = serializers.SerializerMethodField()
    
    class Meta:
        model = BloodSugarComparison
        fields = ['id', 'previous_blood_glucose', 'current_blood_glucose', 
                  'insulin_injection', 'carbohydrate_intake', 'time_interval', 
                  'time_interval_hours', 'created_at']
    
    def get_time_interval_hours(self, obj):
        # 将时间间隔转换为小时（浮点数）
        return obj.time_interval.total_seconds() / 3600
    
class UserModelConsentSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserModelConsent
        fields = ['has_consented', 'consent_date']
        read_only_fields = ['consent_date']

class UserPersonalizedModelSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserPersonalizedModel
        fields = ['is_trained', 'last_trained', 'training_data_count', 'model_version', 'model_performance']
        read_only_fields = ['is_trained', 'last_trained', 'training_data_count', 'model_version', 'model_performance']