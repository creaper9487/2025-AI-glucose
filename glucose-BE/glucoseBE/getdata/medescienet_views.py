import base64
import datetime as dt
import hashlib
import json
import requests
from urllib.parse import urlparse

from django.conf import settings
from django.contrib.auth import get_user_model
from django.utils import timezone
from requests import RequestException
from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from .cap_client import issue_user_file_cap
from .models import BloodSugarRecord, MediSciNetUpload, MediSciNetSubscription
from .serializers import MediSciNetUploadSerializer, MediSciNetSubscriptionSerializer

User = get_user_model()

SIDECAR_BASE_URL = getattr(settings, 'CAP_ISSUER_BASE_URL', 'http://localhost:7777')
# How many epochs ahead before status becomes "expiring_soon"
EXPIRING_SOON_THRESHOLD = 2


def parse_date(value):
    if not value:
        return None
    return dt.date.fromisoformat(value)


def normalize_user_hash(user):
    salt = getattr(settings, 'MEDESCIENET_USER_ID_SALT', '')
    raw_value = f'{user.pk}:{salt}'.encode('utf-8')
    return hashlib.sha256(raw_value).hexdigest()


def build_record_payload(record, user_hash):
    meal_context = None
    if record.carbohydrate_intake is not None:
        meal_context = 'after_meal'
    elif record.insulin_injection is None and record.exercise_duration is None:
        meal_context = 'fasting'

    return {
        'timestamp': record.created_at.isoformat(),
        'glucose_mmol': round(record.blood_glucose / 18.0, 2),
        'meal_context': meal_context,
        'insulin_dose_units': record.insulin_injection,
        'exercise_minutes': record.exercise_duration,
        'notes': None,
    }


def derive_walrus_end_epoch(current_epoch, epochs):
    return current_epoch + epochs


def _call_sidecar(path, payload):
    """POST to the cap_issuer_sidecar and return the parsed JSON response."""
    url = f'{SIDECAR_BASE_URL.rstrip("/")}/{path.lstrip("/")}'
    resp = requests.post(url, json=payload, timeout=15)
    resp.raise_for_status()
    return resp.json()


def _compute_subscription_status(sub, current_epoch=None):
    """Return 'active', 'expiring_soon', or 'expired' for a MediSciNetSubscription."""
    until = sub.service_active_until_epoch
    if current_epoch is None:
        current_epoch = 0  # conservative default when epoch unknown
    if until <= current_epoch:
        return 'expired'
    if until <= current_epoch + EXPIRING_SOON_THRESHOLD:
        return 'expiring_soon'
    return 'active'


class MediSciNetExportPreviewView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        start_date = parse_date(request.query_params.get('start_date'))
        end_date = parse_date(request.query_params.get('end_date'))

        if not start_date or not end_date:
            return Response({'error': 'start_date and end_date are required'}, status=status.HTTP_400_BAD_REQUEST)
        if start_date > end_date:
            return Response({'error': 'start_date must be before or equal to end_date'}, status=status.HTTP_400_BAD_REQUEST)

        records = BloodSugarRecord.objects.filter(
            user=request.user,
            created_at__date__gte=start_date,
            created_at__date__lte=end_date,
        ).order_by('-created_at')

        user_hash = normalize_user_hash(request.user)
        sample = [build_record_payload(record, user_hash) for record in records[:3]]

        payload = {
            'schema_version': '1.0',
            'exported_at': timezone.now().isoformat(),
            'user_hash': user_hash,
            'records': [build_record_payload(record, user_hash) for record in records],
        }
        payload_bytes = json.dumps(payload, ensure_ascii=False, separators=(',', ':')).encode('utf-8')

        return Response(
            {
                'record_count': records.count(),
                'date_range': {'start': start_date.isoformat(), 'end': end_date.isoformat()},
                'estimated_size_bytes': len(payload_bytes),
                'sample': sample,
            },
            status=status.HTTP_200_OK,
        )


class MediSciNetExportPackageView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        start_date = parse_date(request.data.get('start_date'))
        end_date = parse_date(request.data.get('end_date'))

        if not start_date or not end_date:
            return Response({'error': 'start_date and end_date are required'}, status=status.HTTP_400_BAD_REQUEST)
        if start_date > end_date:
            return Response({'error': 'start_date must be before or equal to end_date'}, status=status.HTTP_400_BAD_REQUEST)

        records = BloodSugarRecord.objects.filter(
            user=request.user,
            created_at__date__gte=start_date,
            created_at__date__lte=end_date,
        ).order_by('created_at')

        user_hash = normalize_user_hash(request.user)
        export_payload = {
            'schema_version': '1.0',
            'exported_at': timezone.now().isoformat(),
            'user_hash': user_hash,
            'records': [build_record_payload(record, user_hash) for record in records],
        }
        json_bytes = json.dumps(export_payload, ensure_ascii=False, separators=(',', ':')).encode('utf-8')
        checksum = hashlib.sha256(json_bytes).hexdigest()
        encoded = base64.b64encode(json_bytes).decode('utf-8')

        return Response(
            {
                'data': encoded,
                'schema_version': '1.0',
                'record_count': records.count(),
                'checksum_sha256': checksum,
            },
            status=status.HTTP_200_OK,
        )


class MediSciNetUploadListCreateView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        uploads = MediSciNetUpload.objects.filter(user=request.user).order_by('-uploaded_at')
        serializer = MediSciNetUploadSerializer(uploads, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    def post(self, request):
        serializer = MediSciNetUploadSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        upload = serializer.save(user=request.user)
        return Response(MediSciNetUploadSerializer(upload).data, status=status.HTTP_201_CREATED)


class MediSciNetUploadDetailView(APIView):
    permission_classes = [IsAuthenticated]

    ALLOWED_PATCH_FIELDS = {
        'blob_id', 'seal_id', 'walrus_epoch', 'file_size_bytes',
        'sub_state_id', 'upload_cost_mist', 'status',
    }

    def patch(self, request, pk):
        try:
            upload = MediSciNetUpload.objects.get(pk=pk, user=request.user)
        except MediSciNetUpload.DoesNotExist:
            return Response({'error': 'Upload not found'}, status=status.HTTP_404_NOT_FOUND)

        incoming_fields = set(request.data.keys())
        unexpected_fields = incoming_fields - self.ALLOWED_PATCH_FIELDS
        if unexpected_fields:
            return Response(
                {'error': f'Unsupported fields for upload update: {", ".join(sorted(unexpected_fields))}'},
                status=status.HTTP_400_BAD_REQUEST,
            )

        for field in self.ALLOWED_PATCH_FIELDS:
            if field in request.data:
                setattr(upload, field, request.data[field])

        # If all confirmation fields are present, mark as confirmed
        if {'blob_id', 'seal_id', 'walrus_epoch'} <= incoming_fields and 'status' not in request.data:
            upload.status = 'confirmed'
            incoming_fields.add('status')

        save_fields = sorted(incoming_fields)
        upload.save(update_fields=save_fields)

        return Response(MediSciNetUploadSerializer(upload).data, status=status.HTTP_200_OK)

    def delete(self, request, pk):
        try:
            upload = MediSciNetUpload.objects.get(pk=pk, user=request.user)
        except MediSciNetUpload.DoesNotExist:
            return Response({'error': 'Upload not found'}, status=status.HTTP_404_NOT_FOUND)

        upload.delete()
        return Response(status=status.HTTP_204_NO_CONTENT)


class MediSciNetRequestUploadCapView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user
        wallet_address = getattr(user, 'sui_wallet_address', None)

        if not wallet_address:
            return Response(
                {'error': 'Please link a Sui wallet before requesting an upload cap'},
                status=status.HTTP_403_FORBIDDEN,
            )

        try:
            epochs = int(request.data.get('epochs', 0))
        except (TypeError, ValueError):
            epochs = 0

        if epochs <= 0:
            return Response({'error': 'epochs must be a positive integer'}, status=status.HTTP_400_BAD_REQUEST)

        walrus_end_epoch = request.data.get('walrus_end_epoch')
        try:
            walrus_end_epoch = int(walrus_end_epoch) if walrus_end_epoch is not None else None
        except (TypeError, ValueError):
            return Response({'error': 'walrus_end_epoch must be an integer'}, status=status.HTTP_400_BAD_REQUEST)

        if walrus_end_epoch is None:
            current_epoch = request.data.get('current_walrus_epoch')
            try:
                current_epoch = int(current_epoch)
            except (TypeError, ValueError):
                return Response(
                    {'error': 'current_walrus_epoch is required when walrus_end_epoch is not provided'},
                    status=status.HTTP_400_BAD_REQUEST,
                )
            walrus_end_epoch = derive_walrus_end_epoch(current_epoch, epochs)

        # Ensure subscription record exists; create on-chain if first time
        sub, sub_created = MediSciNetSubscription.objects.get_or_create(
            user=user,
            defaults={'sub_state_id': ''},
        )

        try:
            cap_result = issue_user_file_cap(wallet_address, walrus_end_epoch)
        except RequestException as exc:
            detail = str(exc)
            response = getattr(exc, 'response', None)
            if response is not None:
                try:
                    payload = response.json()
                    detail = payload.get('error') or payload.get('message') or detail
                except ValueError:
                    detail = response.text or detail
            return Response({'error': f'Cap issuer request failed: {detail}'}, status=status.HTTP_502_BAD_GATEWAY)

        # Update subscription record with new sub_state_id if returned
        sub_state_id = cap_result.get('sub_state_id', '')
        if sub_state_id:
            sub.sub_state_id = sub_state_id
            sub.save(update_fields=['sub_state_id'])

        # Vault prerequisite check — if no vault_id stored yet, instruct frontend
        if not sub.vault_id:
            return Response(
                {
                    'requires_vault_creation': True,
                    'cap_object_id': cap_result.get('cap_object_id', ''),
                    'sub_state_id': sub_state_id,
                    'tx_digest': cap_result.get('tx_digest', ''),
                    'walrus_end_epoch': cap_result.get('walrus_end_epoch', walrus_end_epoch),
                },
                status=status.HTTP_202_ACCEPTED,
            )

        # Update linked upload record if provided
        upload_id = request.data.get('upload_id')
        if upload_id:
            try:
                upload = MediSciNetUpload.objects.get(pk=upload_id, user=user)
                upload.user_file_cap_id = cap_result.get('cap_object_id', '')
                upload.sub_state_id = sub_state_id
                upload.save(update_fields=['user_file_cap_id', 'sub_state_id'])
            except MediSciNetUpload.DoesNotExist:
                pass

        return Response(
            {
                'cap_object_id': cap_result.get('cap_object_id', ''),
                'sub_state_id': sub_state_id,
                'tx_digest': cap_result.get('tx_digest', ''),
                'walrus_end_epoch': cap_result.get('walrus_end_epoch', walrus_end_epoch),
                'service_active_until_epoch': sub.service_active_until_epoch,
                'projected_service_end_epoch': sub.projected_service_end_epoch,
                'issuer_url': urlparse(SIDECAR_BASE_URL).netloc,
            },
            status=status.HTTP_200_OK,
        )


class MediSciNetSubscriptionView(APIView):
    """
    GET  — return cached subscription status for the authenticated user.
    POST — create or update vault_id association (called after frontend creates vault).
    """
    permission_classes = [IsAuthenticated]

    def get(self, request):
        try:
            sub = MediSciNetSubscription.objects.get(user=request.user)
        except MediSciNetSubscription.DoesNotExist:
            return Response({'detail': 'No subscription record found.'}, status=status.HTTP_404_NOT_FOUND)

        current_epoch = int(request.query_params.get('current_epoch', 0))
        sub_status = _compute_subscription_status(sub, current_epoch)

        wallet_address = getattr(request.user, 'sui_wallet_address', None)
        cap_obj = MediSciNetUpload.objects.filter(
            user=request.user, user_file_cap_id__gt='',
        ).order_by('-uploaded_at').first()

        return Response(
            {
                'wallet_address': wallet_address,
                'vault_id': sub.vault_id,
                'sub_state_id': sub.sub_state_id,
                'service_active_until_epoch': sub.service_active_until_epoch,
                'projected_service_end_epoch': sub.projected_service_end_epoch,
                'vault_balance_mist': str(sub.vault_balance_mist),
                'service_credit_mist': str(sub.service_credit_mist),
                'cap_object_id': cap_obj.user_file_cap_id if cap_obj else None,
                'cap_walrus_end_epoch': cap_obj.walrus_epoch if cap_obj else None,
                'status': sub_status,
                'last_synced_at': sub.last_synced_at.isoformat() if sub.last_synced_at else None,
            },
            status=status.HTTP_200_OK,
        )

    def post(self, request):
        """Associate a vault_id with the user's subscription record."""
        vault_id = request.data.get('vault_id')
        if not vault_id:
            return Response({'error': 'vault_id is required'}, status=status.HTTP_400_BAD_REQUEST)

        sub, _ = MediSciNetSubscription.objects.get_or_create(
            user=request.user,
            defaults={'sub_state_id': ''},
        )
        sub.vault_id = vault_id
        sub.save(update_fields=['vault_id'])
        return Response({'ok': True, 'vault_id': vault_id}, status=status.HTTP_200_OK)


class MediSciNetSubscriptionApproveSettlementView(APIView):
    """
    POST — store a ServiceSettlementApproval object ID after user signs it on-chain.
    """
    permission_classes = [IsAuthenticated]

    def post(self, request):
        approval_object_id = request.data.get('approval_object_id')
        if not approval_object_id:
            return Response({'error': 'approval_object_id is required'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            sub = MediSciNetSubscription.objects.get(user=request.user)
        except MediSciNetSubscription.DoesNotExist:
            return Response({'error': 'No subscription record found'}, status=status.HTTP_404_NOT_FOUND)

        sub.settlement_approval_id = approval_object_id
        sub.save(update_fields=['settlement_approval_id'])
        return Response({'ok': True}, status=status.HTTP_200_OK)


class MediSciNetSyncSubscriptionView(APIView):
    """
    POST — trigger sidecar to read on-chain state and update cached subscription fields.
    """
    permission_classes = [IsAuthenticated]

    def post(self, request):
        try:
            sub = MediSciNetSubscription.objects.get(user=request.user)
        except MediSciNetSubscription.DoesNotExist:
            return Response({'error': 'No subscription record found'}, status=status.HTTP_404_NOT_FOUND)

        if not sub.vault_id:
            return Response({'error': 'vault_id not set — create a vault first'}, status=status.HTTP_400_BAD_REQUEST)

        wallet_address = getattr(request.user, 'sui_wallet_address', None)
        if not wallet_address:
            return Response({'error': 'Wallet not linked'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            result = _call_sidecar('sync-subscription-state', {
                'vault_id': sub.vault_id,
                'user_wallet': wallet_address,
            })
        except (RequestException, ValueError) as exc:
            return Response(
                {'error': f'Sidecar sync failed: {exc}'},
                status=status.HTTP_502_BAD_GATEWAY,
            )

        sub.service_active_until_epoch = result.get('service_active_until_epoch', sub.service_active_until_epoch)
        sub.projected_service_end_epoch = result.get('projected_service_end_epoch', sub.projected_service_end_epoch)
        sub.vault_balance_mist = int(result.get('vault_balance_mist', sub.vault_balance_mist))
        sub.service_credit_mist = int(result.get('service_credit_mist', sub.service_credit_mist))
        sub.save(update_fields=[
            'service_active_until_epoch', 'projected_service_end_epoch',
            'vault_balance_mist', 'service_credit_mist',
        ])

        current_epoch = int(request.data.get('current_epoch', 0))
        return Response(
            {
                'service_active_until_epoch': sub.service_active_until_epoch,
                'projected_service_end_epoch': sub.projected_service_end_epoch,
                'vault_balance_mist': str(sub.vault_balance_mist),
                'service_credit_mist': str(sub.service_credit_mist),
                'status': _compute_subscription_status(sub, current_epoch),
            },
            status=status.HTTP_200_OK,
        )
