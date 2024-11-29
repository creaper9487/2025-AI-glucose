from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from .models import CustomUser
from .Serializer import UpdateInfoSerializer
from . import jwtdecoder
from rest_framework.permissions import IsAuthenticated
class UpdateInfoView(APIView):
    def post(self, request):
        auth_header = request.headers.get('Authorization')
        if auth_header and auth_header.startswith('Bearer '):
            token = auth_header.split(' ')[1]
            usernameFromDecoder = jwtdecoder.jwtdecode(token)
        serializer = UpdateInfoSerializer(data=request.data)
        if serializer.is_valid():
            username = usernameFromDecoder
            weight = serializer.validated_data['weight']
            height = serializer.validated_data['height']
            age = serializer.validated_data['age']

            try:
                user = CustomUser.objects.get(username=username)
                user.weight = weight
                user.height = height
                user.age = age
                return Response({'message':'Info update successfully'}, status=status.HTTP_200_OK)
            except CustomUser.DoesNotExist:
                return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
            
class GetInfoView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request):
        try:
            auth_header = request.headers.get('Authorization')
            if auth_header and auth_header.startswith('Bearer '):
                token = auth_header.split(' ')[1]
                usernameFromDecoder = jwtdecoder.jwtdecode(token)
                user = CustomUser.objects.get(username=usernameFromDecoder)
            return Response({'username': usernameFromDecoder, 'weight': user.weight, 'height': user.height, 'age': user.age})
        except CustomUser.DoesNotExist:
            return Response({'error': 'User not found'}, status=status.HTTP_404_NOT_FOUND)    