from django.urls import path
from .views import *

urlpatterns = [
    path('user/glucose/', GetGlocose.as_view(), name='glucose-data'),
    path('user/dataForms/', DataFormsView.as_view(), name='data-forms'), # 基本資料輸入
    path('user/foodInput/', FoodInputView.as_view(), name='food-input'), # 碳水化合物攝取量
    path('user/inputForm/', InputFormView.as_view(), name='input-form'), # 血糖 碳水化合物 運動 胰島素
    
]