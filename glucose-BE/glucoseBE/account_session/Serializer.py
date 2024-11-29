from rest_framework import serializers
from .models import CustomUser
from django.contrib.auth import get_user_model
from django.core.exceptions import ValidationError
from . import jwtdecoder
User = get_user_model()

class CustomUserSerializer(serializers.ModelSerializer):
    class Meta:
        model = CustomUser
        fields = ('id', 'email', 'username', 'password')
        extra_kwargs = {'password': {'write_only': True}}

    def create(self, validated_data):
        user = CustomUser.objects.create_user(
            email=validated_data['email'],
            username=validated_data['username'],
            password=validated_data['password'],
        )
        return user
    
class CustomLoginSerializer(serializers.Serializer):
    username_or_email = serializers.CharField()
    password = serializers.CharField()

    def validate(self, data):
        username_or_email = data.get('username_or_email')
        password = data.get('password')

        if username_or_email and password:
            # Check if input is an email
            if '@' in username_or_email:
                user = User.objects.filter(email=username_or_email).first()
            else:
                user = User.objects.filter(username=username_or_email).first()

            if user and user.check_password(password):
                return user
        raise ValidationError('Unable to log in with provided credentials.')
    
class UpdateLangSerializer(serializers.ModelSerializer):
    lang = serializers.CharField(required=True)

    class Meta:
        model = CustomUser
        fields = ['lang']

class UpdateInfoSerializer(serializers.ModelSerializer):
    weight = serializers.CharField(required=True)
    height = serializers.CharField(required=True)
    age = serializers.CharField(required=True)

    class Meta:
        model = CustomUser
        fields = ['weight', 'height', 'age']