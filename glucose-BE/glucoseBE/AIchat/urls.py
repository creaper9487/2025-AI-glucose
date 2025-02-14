from django.urls import path
from .views import TextPostView
from .carbon_views import PredictView
from .predictdiabete import predict_diabetes
urlpatterns = [
    path('chat/', TextPostView.as_view(), name='chat'),
    path('predict/', PredictView.as_view(), name='predict'),
    path('predictform/', predict_diabetes, name='predictform'),
]