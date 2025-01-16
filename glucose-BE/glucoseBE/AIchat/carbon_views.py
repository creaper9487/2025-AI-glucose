from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.parsers import MultiPartParser, FormParser
from .models import UploadedImage
from torchvision import transforms
from PIL import Image
import torch
import torch.nn as nn
from torchvision import models

# 載入模型
hidden_units = 512
dropout_rate = 0.2
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

model = models.resnet18(pretrained=False)
model.fc = nn.Sequential(
    nn.Linear(model.fc.in_features, hidden_units),
    nn.ReLU(),
    nn.Dropout(dropout_rate),
    nn.Linear(hidden_units, 1)
)
model.load_state_dict(torch.load('AIchat/dish_model1.pth', map_location=device))
model = model.to(device)
model.eval()

# 定義轉換
transform = transforms.Compose([
    transforms.RandomResizedCrop(224, scale=(0.8, 1.0)),
    transforms.RandomHorizontalFlip(p=0.5),
    transforms.ColorJitter(brightness=0.4, contrast=0.4, saturation=0.4),
    transforms.ToTensor(),
    transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
])

class PredictView(APIView):
    parser_classes = (MultiPartParser, FormParser)

    def post(self, request, *args, **kwargs):
        # 獲取圖片
        image_file = request.FILES['image']
        uploaded_image = UploadedImage(image=image_file)
        uploaded_image.save()

        # 處理圖片
        image = Image.open(uploaded_image.image.path).convert('RGB')
        image = transform(image)
        image = image.unsqueeze(0).to(device)

        # 預測
        with torch.no_grad():
            output = model(image).item()

        return Response({'predicted_value': output})
