from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from .models import CustomUser
from .Serializer import UpdateLangSerializer
from rest_framework.permissions import IsAuthenticated
from rest_framework_simplejwt.tokens import UntypedToken
from . import jwtdecoder
class UpdateLangView(APIView):
    permission_classes = [IsAuthenticated]
    def post(self, request):
        auth_header = request.headers.get('Authorization')
        if auth_header and auth_header.startswith('Bearer '):
            token = auth_header.split(' ')[1]
            usernameFromDecoder = jwtdecoder.jwtdecode(token)
        serializer = UpdateLangSerializer(data=request.data)
        if serializer.is_valid():
            username = usernameFromDecoder
            lang = serializer.validated_data['lang']
            try:
                user = CustomUser.objects.get(username=username)
                user.lang = lang
                user.save()
                return Response({'message': 'Language update successfully'}, status=status.HTTP_200_OK)
            except CustomUser.DoesNotExist:
                return Response({'error':'User not found'}, status=status.HTTP_404_NOT_FOUND)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
class GetLangView(APIView):
    permission_classes = [IsAuthenticated]
    def get(self, request):
        try:
            auth_header = request.headers.get('Authorization')
            if auth_header and auth_header.startswith('Bearer '):
                token = auth_header.split(' ')[1]
                username = jwtdecoder.jwtdecode(token)
                user = CustomUser.objects.get(username=username)
            return Response({'username': username, 'lang':user.lang}, status=status.HTTP_200_OK)
        except CustomUser.DoesNotExist:
            return Response({'error': 'User not found'}, status=status.HTTP_404_NOT_FOUND)