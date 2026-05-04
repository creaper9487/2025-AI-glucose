import base64
import binascii
import re
import uuid

from django.core.cache import cache
from django.utils import timezone
from nacl.exceptions import BadSignatureError
from nacl.signing import VerifyKey
from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import CustomUser


WALLET_NONCE_CACHE_PREFIX = 'medescienet:wallet_nonce:'
WALLET_NONCE_TTL_SECONDS = 300
SUI_MESSAGE_PREFIX = b'\x19Sui Signed Message:\n'


def normalize_wallet_address(wallet_address):
    if not isinstance(wallet_address, str):
        return None
    normalized = wallet_address.strip().lower()
    if not normalized:
        return None
    if not normalized.startswith('0x'):
        normalized = f'0x{normalized}'
    return normalized


def decode_material(value):
    if isinstance(value, bytes):
        return value
    if not isinstance(value, str):
        raise ValueError('Expected a string value')

    candidate = value.strip()
    if candidate.startswith('0x'):
        candidate = candidate[2:]

    if re.fullmatch(r'[0-9a-fA-F]+', candidate) and len(candidate) % 2 == 0:
        return bytes.fromhex(candidate)

    try:
        return base64.b64decode(candidate, validate=True)
    except (binascii.Error, ValueError):
        return candidate.encode('utf-8')


def wallet_nonce_cache_key(wallet_address):
    return f'{WALLET_NONCE_CACHE_PREFIX}{wallet_address}'


class WalletNonceView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        wallet_address = normalize_wallet_address(request.data.get('wallet_address'))
        if not wallet_address:
            return Response({'error': 'wallet_address is required'}, status=status.HTTP_400_BAD_REQUEST)

        nonce = f'glucose-link-{uuid.uuid4()}'
        cache.set(wallet_nonce_cache_key(wallet_address), nonce, timeout=WALLET_NONCE_TTL_SECONDS)
        return Response({'nonce': nonce}, status=status.HTTP_200_OK)


class WalletLinkView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        wallet_address = normalize_wallet_address(request.data.get('wallet_address'))
        signature = request.data.get('signature')
        public_key = request.data.get('public_key')

        if not wallet_address or not signature or not public_key:
            return Response(
                {'error': 'wallet_address, signature, and public_key are required'},
                status=status.HTTP_400_BAD_REQUEST,
            )

        nonce = cache.get(wallet_nonce_cache_key(wallet_address))
        if not nonce:
            return Response({'error': 'Nonce expired or not found'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            public_key_bytes = decode_material(public_key)
            signature_bytes = decode_material(signature)
            verify_key = VerifyKey(public_key_bytes)
            verify_key.verify(SUI_MESSAGE_PREFIX + nonce.encode('utf-8'), signature_bytes)
        except (BadSignatureError, ValueError, TypeError):
            return Response({'error': 'Invalid signature'}, status=status.HTTP_400_BAD_REQUEST)

        if CustomUser.objects.filter(sui_wallet_address=wallet_address).exclude(pk=request.user.pk).exists():
            return Response({'error': 'This wallet address is already linked to another user'}, status=status.HTTP_400_BAD_REQUEST)

        user = request.user
        user.sui_wallet_address = wallet_address
        user.sui_wallet_linked_at = timezone.now()
        user.save(update_fields=['sui_wallet_address', 'sui_wallet_linked_at'])
        cache.delete(wallet_nonce_cache_key(wallet_address))

        return Response({'success': True, 'wallet_address': wallet_address}, status=status.HTTP_200_OK)


class WalletStatusView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        return Response(
            {
                'linked': bool(user.sui_wallet_address),
                'wallet_address': user.sui_wallet_address,
                'linked_at': user.sui_wallet_linked_at,
            },
            status=status.HTTP_200_OK,
        )


class WalletUnlinkView(APIView):
    permission_classes = [IsAuthenticated]

    def delete(self, request):
        user = request.user
        wallet_address = user.sui_wallet_address
        if wallet_address:
            cache.delete(wallet_nonce_cache_key(wallet_address))
        user.sui_wallet_address = None
        user.sui_wallet_linked_at = None
        user.save(update_fields=['sui_wallet_address', 'sui_wallet_linked_at'])
        return Response({'success': True, 'wallet_address': None}, status=status.HTTP_200_OK)