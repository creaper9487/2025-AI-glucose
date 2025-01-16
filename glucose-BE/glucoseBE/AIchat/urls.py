from django.urls import path
from .views import TextPostView
from .carbon_views import PredictView
urlpatterns = [
    path('chat/', TextPostView.as_view(), name='chat'),
    path('predict/', PredictView.as_view(), name='predict'),
]