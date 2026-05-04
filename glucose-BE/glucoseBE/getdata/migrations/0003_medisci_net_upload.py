from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('getdata', '0002_bloodsugarcomparison_usermodelconsent_and_more'),
    ]

    operations = [
        migrations.CreateModel(
            name='MediSciNetUpload',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('blob_id', models.CharField(blank=True, max_length=128)),
                ('seal_id', models.CharField(blank=True, max_length=128)),
                ('walrus_epoch', models.IntegerField(blank=True, null=True)),
                ('walrus_epoch_end', models.DateTimeField(blank=True, null=True)),
                ('file_size_bytes', models.IntegerField(default=0)),
                ('record_count', models.IntegerField(default=0)),
                ('date_range_start', models.DateField()),
                ('date_range_end', models.DateField()),
                ('schema_version', models.CharField(default='1.0', max_length=10)),
                ('checksum_sha256', models.CharField(blank=True, max_length=64)),
                ('uploaded_at', models.DateTimeField(auto_now_add=True)),
                ('status', models.CharField(choices=[('pending', 'Pending'), ('confirmed', 'Confirmed'), ('expired', 'Expired')], default='pending', max_length=20)),
                ('user_file_cap_id', models.CharField(blank=True, max_length=128)),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='account_session.customuser')),
            ],
        ),
    ]