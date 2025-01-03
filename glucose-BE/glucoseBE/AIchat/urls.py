from django.urls import path
from .views import TextPostView
urlpatterns = [
    path('chat/', TextPostView.as_view(), name='chat'),
]