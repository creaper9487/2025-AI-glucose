from django.urls import path
from .views import BloodSugarRecordAPIView, BloodSugarComparisonAPIView, UserModelConsentView, UserModelTrainingView, PersonalizedPredictionView

urlpatterns = [
    path('records/', BloodSugarRecordAPIView.as_view(), name='bloodsugar-records'),
    path('comparisons/', BloodSugarComparisonAPIView.as_view(), name='bloodsugar-comparisons'),
    path('model/consent/', UserModelConsentView.as_view(), name='model-consent'),
    path('model/train/', UserModelTrainingView.as_view(), name='model-train'),
    path('predict/personalized/', PersonalizedPredictionView.as_view(), name='personalized-predict'),
]