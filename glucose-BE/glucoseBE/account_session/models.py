from django.db import models
import uuid
from django.contrib.auth.models import AbstractBaseUser,BaseUserManager
# Create your models here.

class UserManager(BaseUserManager):
    def create_user(self, email, username, password=None):
        if not email:
            raise ValueError("Users must input email address")
        if not username:
            raise ValueError("User must input username")
        
        user = self.model(
            email=self.normalize_email(email),
            username=username
        )
        user.set_password(password)
        user.user_id = uuid.uuid4()
        user.save(using=self._db)
        return user
class User(AbstractBaseUser):
    email = models.EmailField(max_length=1000, unique=True)
    username = models.CharField(max_length=100, unique=True)
    user_id = models.UUIDField(default=uuid.uuid4, editable=False, unique=True)
    is_active = models.BooleanField(default=True)
    is_active = models.BooleanField(default=False)
    
    objects = UserManager()

    USERNAME_FIELD = 'username'
    REQUIRED_FIELDS = ['email']