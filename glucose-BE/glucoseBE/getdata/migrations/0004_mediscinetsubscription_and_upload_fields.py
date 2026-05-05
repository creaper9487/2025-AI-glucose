from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('getdata', '0003_medisci_net_upload'),
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        # Add new fields to MediSciNetUpload
        migrations.AddField(
            model_name='mediscinetupload',
            name='sub_state_id',
            field=models.CharField(blank=True, max_length=100, null=True),
        ),
        migrations.AddField(
            model_name='mediscinetupload',
            name='upload_cost_mist',
            field=models.BigIntegerField(blank=True, null=True),
        ),
        migrations.AddField(
            model_name='mediscinetupload',
            name='service_active_until_epoch',
            field=models.BigIntegerField(blank=True, null=True),
        ),
        # Expand STATUS_CHOICES on MediSciNetUpload
        migrations.AlterField(
            model_name='mediscinetupload',
            name='status',
            field=models.CharField(
                choices=[
                    ('pending', 'Pending'),
                    ('vault_ready', 'Vault Ready'),
                    ('encrypting', 'Encrypting'),
                    ('uploading', 'Uploading'),
                    ('certifying', 'Certifying'),
                    ('confirmed', 'Confirmed'),
                    ('failed', 'Failed'),
                    ('expired', 'Expired'),
                ],
                default='pending',
                max_length=20,
            ),
        ),
        # Create MediSciNetSubscription table
        migrations.CreateModel(
            name='MediSciNetSubscription',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('sub_state_id', models.CharField(max_length=100, unique=True)),
                ('vault_id', models.CharField(blank=True, max_length=100, null=True)),
                ('service_active_until_epoch', models.BigIntegerField(default=0)),
                ('projected_service_end_epoch', models.BigIntegerField(default=0)),
                ('vault_balance_mist', models.BigIntegerField(default=0)),
                ('service_credit_mist', models.BigIntegerField(default=0)),
                ('settlement_approval_id', models.CharField(blank=True, max_length=100, null=True)),
                ('last_synced_at', models.DateTimeField(auto_now=True)),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('user', models.OneToOneField(
                    on_delete=django.db.models.deletion.CASCADE,
                    related_name='medescienet_subscription',
                    to=settings.AUTH_USER_MODEL,
                )),
            ],
        ),
    ]
