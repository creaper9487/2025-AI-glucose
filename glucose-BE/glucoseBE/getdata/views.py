from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from django.utils import timezone
from datetime import timedelta
from . import jwtdecoder
from .models import BloodSugarRecord, get_time_slot
from .serializers import BloodSugarRecordSerializer
from django.contrib.auth import get_user_model


User = get_user_model()


class BloodSugarRecordAPIView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        """
        取得當前已驗證使用者的所有血糖紀錄
        """
        auth_header = request.headers.get('Authorization')
        if auth_header and auth_header.startswith('Bearer '):
            token = auth_header.split(' ')[1]
            usernameFromDecoder = jwtdecoder.jwtdecode(token)
        user_instance = User.objects.get(username=usernameFromDecoder)
        records = BloodSugarRecord.objects.filter(user=user_instance).order_by('-created_at')
        serializer = BloodSugarRecordSerializer(records, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    def post(self, request):
        """
        收到血糖相關數據的上傳：
         - 從 request.user 取得使用者
         - 自動計算該筆資料所屬的時間槽 (time_slot)
         - 若上傳資料中「胰島素注射量」為空，則檢查該 user 是否在相同 time_slot 內已有資料
             - 若有，則以新資料覆蓋（更新）該筆紀錄
             - 若無，則建立新紀錄
         - 若上傳資料中有胰島素注射量，則直接建立新紀錄（保留該筆記錄）
        """
        auth_header = request.headers.get('Authorization')
        if auth_header and auth_header.startswith('Bearer '):
            token = auth_header.split(' ')[1]
            usernameFromDecoder = jwtdecoder.jwtdecode(token)
        user_instance = User.objects.get(username=usernameFromDecoder)
        now = timezone.now()
        slot = get_time_slot(now)

        # 複製一份 request.data，以便加入系統欄位
        data = request.data.copy()
        # 設定 time_slot
        data['time_slot'] = slot.isoformat()

        # 判斷是否有提供胰島素注射量
        insulin_injection = data.get('insulin_injection')
        has_insulin = (insulin_injection not in [None, '', 'null'])

        # 若本次資料沒有胰島素注射，則檢查是否在該時間槽內已有資料（且該筆資料沒有胰島素注射記錄）
        if not has_insulin:
            existing_record = BloodSugarRecord.objects.filter(
                user=user_instance,
                time_slot=slot,
                insulin_injection__isnull=True
            ).first()
            if existing_record:
                serializer = BloodSugarRecordSerializer(existing_record, data=data, partial=True)
                if serializer.is_valid():
                    serializer.save()
                    return Response(serializer.data, status=status.HTTP_200_OK)
                else:
                    return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        # 若無可覆蓋的紀錄或本次資料有胰島素注射，則建立新紀錄
        print(user_instance)
        data['user'] = user_instance
        serializer = BloodSugarRecordSerializer(data=data)
        if serializer.is_valid():
            serializer.save(user=user_instance)
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)