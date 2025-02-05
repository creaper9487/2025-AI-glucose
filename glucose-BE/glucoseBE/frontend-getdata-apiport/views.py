from django.shortcuts import render, get_object_or_404
from django.http import JsonResponse
from django.views.generic import *
from .models import Data

from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework import status
from rest_framework.permissions import AllowAny  # 引入 AllowAny
# Create your views here.

class GetGlocose(APIView):
    """
    this is a test get method port for acquire glucose data
    """

    permission_classes = [AllowAny]
    def get(self, request, format=None):
        glucose_data = {
            '20241017060': 140,
            '20241017090': 135,
            '20241017120': 125,
            '20241017150': 130,
            '20241017180': 120
        }
        return Response(glucose_data, status=status.HTTP_200_OK)
                
from rest_framework.permissions import IsAuthenticated
from .models import UserData, UpdateData
from .serializers import UpdateDataSerializer, UserDataSerializer

'''
/dataForms
POST 年齡 身高 體重，為用戶基本資料 
/foodInput
POST 一張照片，丟給AI算，期望收到一個float的碳水化合物值
/inputForm
POST 血糖 碳水化合物 運動 胰島素
首頁那個畫面，會收集連續性的血糖資料追蹤 

目前沒有使用到後端數據？
'''


# /dataForms POST 年齡 身高 體重，為用戶基本資料 
class DataFormsView(APIView):
    def post(self, request):
        serializer = UserDataSerializer(data=request.data)

        if serializer.is_valid():
            serializer.save()
            return Response({"message": "基本資料已儲存"}, status=status.HTTP_201_CREATED)
        
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


# /inputForm POST 血糖 碳水化合物 運動 胰島素 

class InputFormView(APIView):
    def post(self, request):
        serializer = UpdateDataSerializer(data=request.data)

        if serializer.is_valid():
            serializer.save()
            return Response({"message": "數據已記錄"}, status=status.HTTP_201_CREATED)
        
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    