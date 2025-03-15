from google.oauth2 import id_token
from google.auth.transport import requests
from django.contrib.auth import get_user_model
from rest_framework import serializers
from django.db import IntegrityError
from rest_framework_simplejwt.tokens import RefreshToken
import json
import requests
from urllib.parse import urlencode

redirect_url_val = "input the value"
client_id_val = "input the value"
client_secret_val = "input the value"

User = get_user_model()

class GoogleLoginSerializer(serializers.Serializer):
    
    def validate_token(self, token):
        try:
            access_token_uri = 'https://accounts.google.com/o/oauth2/token'
            redirect_uri = redirect_url_val
            params = urlencode({
                'code': token,
                'redirect_uri': redirect_uri,
                'client_id': client_id_val,
                'client_secret':client_secret_val ,
                'grant_type': 'authorization_code'
            })
            headers = {'content-type': 'application/x-www-form-urlencoded'}

            # 使用 requests.post 來發送請求
            response = requests.post(access_token_uri, data=params, headers=headers)
            token_data = response.json()
            print(token_data)
            # 獲取用戶資訊
            userinfo_uri = f"https://www.googleapis.com/oauth2/v1/userinfo?access_token={token_data['access_token']}"
            userinfo_response = requests.get(userinfo_uri)
            google_profile = userinfo_response.json()

            # 打印 Google 帳戶資料
            print(google_profile)
            return google_profile
        except KeyError as e:
            if str(e) == "'access_token'":
                return {"error":"the access_token(from google_oauth) is invalid"}

    def create(self, validated_data):
        idinfo = validated_data['idinfo']
        email = idinfo['email']
        #or email = idinfo.get('email')
        try:
            user = User.objects.get(email=email)
        except User.DoesNotExist:
            username = email.split('@')[0] + "unnamed"
            try:
                user = User.objects.create_user(email = email, username=username, password="can't_pass")
            except IntegrityError:
                pass
        
        refresh = RefreshToken.for_user(user)
        refresh['username'] = user.username
        return {
            'refresh': str(refresh),
            'access': str(refresh.access_token),
            'user': {
                'email': user.email,
                'username':user.username
            }
        }