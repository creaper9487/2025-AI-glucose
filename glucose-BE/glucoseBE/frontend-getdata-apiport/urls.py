from django.urls import path
from .views import GetGlocose

urlpatterns = [
    path('user/glucose/', GetGlocose.as_view(), name='glucose-data'),
]