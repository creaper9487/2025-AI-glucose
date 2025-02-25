from django.urls import path
from .views import BloodSugarRecordAPIView

urlpatterns = [
    # URL 中以 username 作為參數識別對象
    path('records/', BloodSugarRecordAPIView.as_view(), name='bloodsugar-records'),
]