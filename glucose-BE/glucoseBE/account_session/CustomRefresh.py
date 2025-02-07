from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework.permissions import AllowAny
from rest_framework_simplejwt.tokens import RefreshToken, TokenError

class CustomRefreshView(APIView):
    # 允許未經驗證的請求以方便取得 token
    permission_classes = [AllowAny]

    def post(self, request):
        # 從 request 資料中取得 refresh token
        refresh_token = request.data.get('refresh', None)
        if not refresh_token:
            return Response(
                {'detail': '請提供 refresh token'},
                status=status.HTTP_400_BAD_REQUEST
            )
        try:
            # 根據傳入的 refresh token 建立 RefreshToken 物件
            refresh = RefreshToken(refresh_token)
            # 產生新的 access token
            access = refresh.access_token

            # 嘗試從 refresh token 中取得 username 資訊
            username = refresh.get('username', None)
            if username:
                # 將 username 加入 access token 的 payload
                access['username'] = username

            # 若有需要，也可視需求進行 refresh token 旋轉或更新

            return Response({
                'access': str(access),
                'refresh': str(refresh),
                'username': username,
                'message': 'token 刷新成功',
                'success': True
            }, status=status.HTTP_200_OK)
        except TokenError as e:
            # 若 refresh token 無效或過期，回傳錯誤訊息
            return Response(
                {'detail': '無效的 refresh token', 'errors': str(e)},
                status=status.HTTP_400_BAD_REQUEST
            )
