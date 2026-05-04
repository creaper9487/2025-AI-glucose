from django.conf import settings
import requests


def issue_user_file_cap(wallet_address: str, walrus_end_epoch: int) -> dict:
    response = requests.post(
        f'{settings.CAP_ISSUER_BASE_URL}/issue-cap',
        json={
            'user_wallet_address': wallet_address,
            'walrus_end_epoch': walrus_end_epoch,
        },
        timeout=30,
    )
    response.raise_for_status()
    return response.json()
