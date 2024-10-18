from django.shortcuts import render
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
                