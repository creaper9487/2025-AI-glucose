import base64
import datetime as dt
import hashlib
import json
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
from .models import BloodSugarRecord, MediSciNetUpload
from .serializers import MediSciNetUploadSerializer

User = get_user_model()


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

    def patch(self, request, pk):
        try:
            upload = MediSciNetUpload.objects.get(pk=pk, user=request.user)
        except MediSciNetUpload.DoesNotExist:
            return Response({'error': 'Upload not found'}, status=status.HTTP_404_NOT_FOUND)

        serializer = MediSciNetUploadSerializer(upload, data=request.data, partial=True)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        serializer.save()
        return Response(serializer.data, status=status.HTTP_200_OK)

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
            return Response({'error': 'Please link a Sui wallet before requesting an upload cap'}, status=status.HTTP_400_BAD_REQUEST)

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
                current_epoch = 0
            walrus_end_epoch = derive_walrus_end_epoch(current_epoch, epochs)

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

        upload_id = request.data.get('upload_id')
        if upload_id:
            try:
                upload = MediSciNetUpload.objects.get(pk=upload_id, user=user)
                upload.user_file_cap_id = cap_result.get('cap_object_id', '')
                upload.save(update_fields=['user_file_cap_id'])
            except MediSciNetUpload.DoesNotExist:
                pass

        return Response(
            {
                'cap_object_id': cap_result.get('cap_object_id', ''),
                'tx_digest': cap_result.get('tx_digest', ''),
                'walrus_end_epoch': cap_result.get('walrus_end_epoch', walrus_end_epoch),
                'issuer_url': urlparse(settings.CAP_ISSUER_BASE_URL).netloc,
            },
            status=status.HTTP_200_OK,
        )
