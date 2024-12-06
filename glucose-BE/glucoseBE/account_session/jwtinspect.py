from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAdminUser
from rest_framework.response import Response
from rest_framework_simplejwt.token_blacklist.models import OutstandingToken
from rest_framework.permissions import AllowAny  # 引入 AllowAny
from rest_framework.views import APIView
class list_jwt_token(APIView):
    permission_classes = [AllowAny]
    def get(self, request):
        tokens = OutstandingToken.objects.all()
        response = []
        for token in tokens:
            response.append({
                "id": token.id,
                "user": token.user.username,
                "token": token.token,
                "created_at": token.created_at,
                "expires_at": token.expires_at,
            })
        return Response(response)