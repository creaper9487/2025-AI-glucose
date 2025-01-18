# 2025-AI-glucose

## Requirements
- [Node.js](https://nodejs.org/)
- [Python 3](https://www.python.org/)
- git clone the project

## Installation
- Windows: Run `batch\install.bat`
- Linux/macOS: Navigate to project root and run the following commands:

    ```sh
    npm install -g pnpm
    cd glucose-FE
    pnpm install
    cd ../glucose-BE
    pip install -r requirements.txt
    ```
    
    If you get an **externally-managed-environment** error when running `pip install -r requirements.txt`, fix it yourself.

## Run server
- Windows: Run `batch\server.bat`
- Linux/macOS: Navigate to `glucose-BE/glucoseBE` and run the following commands:

    ```sh
    python3 manage.py migrate
    python3 manage.py runserver 0.0.0.0:8000
    ```
    
## Run web demo
- Windows: Run `batch\web.bat`
- Linux/macOS: Navigate to `glucose-FE` and run the following command:

    ```sh
    pnpm dev --open
    ```

## Run Android demo

1. **Install APK**: Download and install APK from releases.

2. **USB tether**: Connect your phone to computer, and configure USB tethering. In macOS and Hackintosh, you need to install HoRNDIS Kext to support USB tethering.

3. **Check IPv4 address**: Check your phone's IPv4 address using terminal command `ipconfig` (Windows) or `ifconfig` (Unix-like) .

4. **Run server**

5. **Open the app**: Finally, open the app and enter IPv4 address in start-up AlertDialog.