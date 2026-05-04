from django.contrib import admin
from .models import BloodSugarComparison, BloodSugarRecord, MediSciNetUpload

# Register your models here.

admin.site.register(BloodSugarRecord)
admin.site.register(BloodSugarComparison)
admin.site.register(MediSciNetUpload)
