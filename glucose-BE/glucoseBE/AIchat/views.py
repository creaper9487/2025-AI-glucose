from django.shortcuts import render

# Create your views here.
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
import ollama
from rest_framework.permissions import IsAuthenticated  # 只有登入用戶可以存取
from getdata.models import BloodSugarRecord  # 引入血糖紀錄模型
from datetime import timedelta
from django.utils import timezone
from . import jwtdecoder
from django.contrib.auth import get_user_model


User = get_user_model()

class TextPostView(APIView):
    
    permission_classes = [IsAuthenticated]  # 限制為登入用戶才能存取
    def get(self, request):
        auth_header = request.headers.get('Authorization')
        if auth_header and auth_header.startswith('Bearer '):
            token = auth_header.split(' ')[1]
            usernameFromDecoder = jwtdecoder.jwtdecode(token)
        user_instance = User.objects.get(username=usernameFromDecoder)
        # 若無需存儲，直接返回請求內容
        seven_days_ago = timezone.now() - timedelta(days=7)
        records = BloodSugarRecord.objects.filter(user=user_instance, created_at__gte=seven_days_ago).order_by('-created_at')
        if not records.exists():
            user_data = "目前沒有可用的血糖紀錄。\n"
        else:
            # 整理數據以利 AI 解析
            user_data = "過去 7 天的血糖紀錄如下：\n"
            for record in records:
                user_data += (
                    f"- 時間: {record.created_at.strftime('%Y-%m-%d %H:%M')}, "
                    f"血糖: {record.blood_glucose} mg/dL, "
                    f"碳水: {record.carbohydrate_intake or '無'} g, "
                    f"運動: {record.exercise_duration or '無'} 分鐘, "
                    f"胰島素: {record.insulin_injection or '無'} 單位\n"
                )

        # 設定 AI 角色與問題
        prompt = (
            "你是一位經驗豐富的醫師，以下是病人的血糖紀錄，"
            "請根據數據分析病人的身體狀況，並提供健康建議。\n\n"
            f"{user_data}\n"
            "請詳細解釋病人目前的血糖狀況，並給予健康建議。"
        )

        # 呼叫 Ollama 進行對話
        response = ollama.chat(
            model="llama3.2",
            messages=[{"role": "user", "content": prompt}]
        )

        return Response({"response": response}, status=status.HTTP_200_OK)