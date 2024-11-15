from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from .models import CustomUser
from .Serializer import UpdateLangSerializer

class UpdateLangView(APIView):
    def post(self, request):
        serializer = UpdateLangSerializer(data=request.data)
        if serializer.is_valid():
            username = serializer.validated_data['username']
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
    def get(self, request, username):
        try:
            user = CustomUser.objects.get(username=username)
            return Response({'username': username, 'lang':user.lang}, status=status.HTTP_200_OK)
        except CustomUser.DoesNotExist:
            return Response({'error': 'User not found'}, status=status.HTTP_404_NOT_FOUND)