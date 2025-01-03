from django.shortcuts import render

# Create your views here.
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
import ollama
from rest_framework.permissions import AllowAny  # 引入 AllowAny
class TextPostView(APIView):
    permission_classes = [AllowAny]
    def post(self, request):
        # 若無需存儲，直接返回請求內容
        content = request.data.get('content', '')
        if not content:
            return Response({"error": "Content is required"}, status=status.HTTP_400_BAD_REQUEST)
        else:
            prompt = "你是一個富有經驗的醫師，你會幫病人解讀血糖資訊並且告訴病人目前的身體情況與接下來要怎麼保持身體健康"
            response = ollama.chat(
                model="llama3.2",
                messages=[
                    {"role": "user", "content": prompt+content}
                ]
            )
        return Response({"response":response}, status=status.HTTP_200_OK)
