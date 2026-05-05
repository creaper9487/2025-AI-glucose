from rest_framework import serializers
from .models import BloodSugarRecord
from .models import BloodSugarComparison, UserModelConsent, UserPersonalizedModel
from .models import MediSciNetUpload, MediSciNetSubscription

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


class MediSciNetUploadSerializer(serializers.ModelSerializer):
    class Meta:
        model = MediSciNetUpload
        fields = [
            'id', 'user', 'blob_id', 'seal_id', 'walrus_epoch', 'walrus_epoch_end',
            'file_size_bytes', 'record_count', 'date_range_start', 'date_range_end',
            'schema_version', 'checksum_sha256', 'uploaded_at', 'status', 'user_file_cap_id',
            'sub_state_id', 'upload_cost_mist', 'service_active_until_epoch',
        ]
        read_only_fields = [
            'id', 'user', 'uploaded_at', 'blob_id', 'seal_id', 'walrus_epoch',
            'walrus_epoch_end', 'status', 'user_file_cap_id',
        ]


class MediSciNetSubscriptionSerializer(serializers.ModelSerializer):
    class Meta:
        model = MediSciNetSubscription
        fields = [
            'sub_state_id', 'vault_id',
            'service_active_until_epoch', 'projected_service_end_epoch',
            'vault_balance_mist', 'service_credit_mist',
            'settlement_approval_id', 'last_synced_at',
        ]
        read_only_fields = ['last_synced_at']
