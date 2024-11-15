# Use the official Python image from the Docker Hub
FROM python:3.9-slim as backend

# Set the working directory for the backend
WORKDIR /app/backend

# Copy the backend requirements file into the container
COPY backend/requirements.txt .

# Install the backend dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the backend source code into the container
COPY backend/ .

# Expose the backend port
EXPOSE 8000

# Command to run the backend
CMD ["python", "app.py"] 

# Use the official Node.js image from the Docker Hub
FROM node:18 as frontend

# Set the working directory for the frontend
WORKDIR /app/frontend


# Copy the frontend source code into the container
COPY glutose-FE/ ./

# Install the frontend dependencies
RUN npm install


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