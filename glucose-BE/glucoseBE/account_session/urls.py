from django.urls import path
from .register_port import RegisterView
from rest_framework_simplejwt.views import TokenRefreshView
from .passtoken import CustomLoginView
urlpatterns = [
    path('register/', RegisterView.as_view(), name='register'),
    path('token/', CustomLoginView.as_view(), name='token_obtain_pair'),  # 登入視圖
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),  # 刷新 token
]