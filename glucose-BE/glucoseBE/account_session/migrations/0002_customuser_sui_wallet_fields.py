from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('account_session', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='customuser',
            name='sui_wallet_address',
            field=models.CharField(blank=True, max_length=66, null=True, unique=True),
        ),
        migrations.AddField(
            model_name='customuser',
            name='sui_wallet_linked_at',
            field=models.DateTimeField(blank=True, null=True),
        ),
    ]
