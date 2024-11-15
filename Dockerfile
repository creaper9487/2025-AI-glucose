#使用 Docker Hub 上的官方 Python 映像
FROM python:3.9-slim as backend

#設定工作目錄
WORKDIR /app/backend

#複製需求檔案到容器中
COPY 2025-AI-glucose/glucose-BE/requirements.txt .

#安裝後端依賴項
RUN pip install --no-cache-dir -r requirements.txt

#複製後端源代碼到容器中
COPY 2025-AI-glucose/glucose-BE/glucoseBE ./glucoseBE

#暴露後端使用的端口
EXPOSE 8000

#使用 shell 指令執行遷移命令並啟動服務器
CMD ["sh", "-c", "cd glucoseBE && python manage.py migrate && python manage.py runserver 0.0.0.0:8000"]

# Use the official Node.js image from the Docker Hub
FROM node:18 as frontend

# Set the working directory for the frontend
WORKDIR /app/frontend


# Copy the frontend source code into the container
COPY glutose-FE/ ./


# Install pnpm
RUN npm install -g pnpm

# Install the frontend dependencies
RUN pynpm install

# Build the frontend app
RUN pnpm run build

# Use a lightweight web server to serve the frontend
FROM nginx:alpine

# Copy the built frontend app into the web server's root directory
COPY --from=frontend /app/frontend/dist /usr/share/nginx/html

# Expose the frontend port
EXPOSE 80

# Command to run the web server
CMD ["nginx", "-g", "daemon off;"]