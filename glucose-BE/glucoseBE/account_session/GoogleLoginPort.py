from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from .GoogleSerializer import GoogleLoginSerializer
from rest_framework.permissions import AllowAny  # 引入 AllowAny


class GoogleLoginView(APIView):
    permission_classes = [AllowAny]
    def post(self, request):
        serializer = GoogleLoginSerializer(data=request.data)
        if serializer.is_valid():
            validated_data = serializer.validate_token(request.data['code'])
            response_data = serializer.create({'idinfo': validated_data})
            return Response(response_data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)