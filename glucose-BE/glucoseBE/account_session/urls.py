from django.urls import path
from .register_port import RegisterView
from rest_framework_simplejwt.views import TokenRefreshView
from .passtoken import CustomLoginView
from .GoogleLoginPort import GoogleLoginView
from .lang_view import UpdateLangView, GetLangView
from .Info_view import UpdateInfoView, GetInfoView
from .jwtinspect import list_jwt_token
urlpatterns = [
    path('lang/get', GetLangView.as_view(), name='get-lang'),
    path('lang/update', UpdateLangView.as_view(), name='update-lang'),
    path('auth/google/', GoogleLoginView.as_view(), name='google_login'),
    path('register/', RegisterView.as_view(), name='register'),
    path('token/', CustomLoginView.as_view(), name='token_obtain_pair'),  # 登入視圖
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('info/update/',UpdateInfoView.as_view(), name='update_info'),
    path('info/get', GetInfoView.as_view(), name='get_info'),  # 刷新 token
    path('get/jwt', list_jwt_token.as_view(), name='view_info')
]