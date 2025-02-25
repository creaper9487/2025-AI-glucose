from rest_framework import serializers

class DiabetesPredictionSerializer(serializers.Serializer):
    age = serializers.FloatField()
    bmi = serializers.FloatField()
    blood_pressure = serializers.FloatField()
    feature4 = serializers.FloatField()
    feature5 = serializers.FloatField()
    feature6 = serializers.FloatField()
    feature7 = serializers.FloatField()
    feature8 = serializers.FloatField()
    feature9 = serializers.FloatField()
    feature10 = serializers.FloatField()