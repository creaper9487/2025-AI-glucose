from django.db import models
from ..account_session.models import CustomUser

# Create your models here.

class UserData(models.Model):

    user = models.ForeignKey(CustomUser, on_delete=models.CASCADE, related_name='user')

    birthday = models.DateField('生日')

    height = models.IntegerField('身高', max_length=250)

    weight = models.IntegerField('體重', max_length=750)

class UpdateData(models.Model):

    user = models.ForeignKey(CustomUser, on_delete=models.CASCADE, related_name='user')

    bloodsuger = models.IntegerField('血糖值')

    carbohydrate = models.IntegerField('碳水化合物攝取量')

    exercise = models.IntegerField('運動時長')

    insuiln = models.IntegerField('胰島素注射量')