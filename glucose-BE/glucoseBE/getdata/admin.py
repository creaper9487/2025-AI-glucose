from django.contrib import admin
from .models import BloodSugarComparison, BloodSugarRecord

# Register your models here.

admin.site.register(BloodSugarRecord)
admin.site.register(BloodSugarComparison)
