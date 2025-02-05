from rest_framework import serializers
from .models import UserData, UpdateData

class UserDataSerializer(serializers.ModelSerializer):

    class Meta:
        model = UserData
        fields = ['user', 'birthday', 'height', 'weight']

class UpdateDataSerializer(serializers.ModelSerializer):
    
    class Meta:
        model = UpdateData
        fields = ['user', 'bloodsuger', 'carbohydrate', 'exercise', 'insuiln']
