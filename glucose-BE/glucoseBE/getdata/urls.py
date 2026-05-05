from django.urls import path
from .views import BloodSugarRecordAPIView, BloodSugarComparisonAPIView, UserModelConsentView, UserModelTrainingView, PersonalizedPredictionView
from .medescienet_views import (
    MediSciNetExportPreviewView,
    MediSciNetExportPackageView,
    MediSciNetUploadListCreateView,
    MediSciNetUploadDetailView,
    MediSciNetRequestUploadCapView,
    MediSciNetSubscriptionView,
    MediSciNetSubscriptionApproveSettlementView,
    MediSciNetSyncSubscriptionView,
)

urlpatterns = [
    path('records/', BloodSugarRecordAPIView.as_view(), name='bloodsugar-records'),
    path('comparisons/', BloodSugarComparisonAPIView.as_view(), name='bloodsugar-comparisons'),
    path('model/consent/', UserModelConsentView.as_view(), name='model-consent'),
    path('model/train/', UserModelTrainingView.as_view(), name='model-train'),
    path('predict/personalized/', PersonalizedPredictionView.as_view(), name='personalized-predict'),
    path('medescienet/export/preview/', MediSciNetExportPreviewView.as_view(), name='medescienet-export-preview'),
    path('medescienet/export/package/', MediSciNetExportPackageView.as_view(), name='medescienet-export-package'),
    path('medescienet/uploads/', MediSciNetUploadListCreateView.as_view(), name='medescienet-uploads'),
    path('medescienet/uploads/<int:pk>/', MediSciNetUploadDetailView.as_view(), name='medescienet-upload-detail'),
    path('medescienet/request-upload-cap/', MediSciNetRequestUploadCapView.as_view(), name='medescienet-request-upload-cap'),
    path('medescienet/subscription/', MediSciNetSubscriptionView.as_view(), name='medescienet-subscription'),
    path('medescienet/subscription/approve-settlement/', MediSciNetSubscriptionApproveSettlementView.as_view(), name='medescienet-approve-settlement'),
    path('medescienet/sync-subscription/', MediSciNetSyncSubscriptionView.as_view(), name='medescienet-sync-subscription'),
]
