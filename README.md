# 2025-AI-glucose

## Pre-installation
- [Node.js](https://nodejs.org/)
- [Python 3](https://www.python.org/)
- Git clone the project repo

## Installation
- Windows: Run `batch\install.bat`.
- Linux/macOS: From the project root directory, run:

    ```sh
    npm install -g pnpm
    cd glucose-FE
    pnpm install
    cd ../glucose-BE
    pip install -r requirements.txt
    ```
    
    If you encounter an **externally-managed-environment** error during `pip install -r requirements.txt`, create a  virtual environment **(venv)** and install the dependencies within it.

## Run server
- Windows: Run `batch\server.bat`.
- Linux/macOS: From the `glucose-BE/glucoseBE` directory, run:

    ```sh
    python3 manage.py migrate
    python3 manage.py runserver 0.0.0.0:8000
    ```

## Test server
This process will delete the existing database and restart the server.

- Windows: Run `batch\test-server.bat`.
- Linux/macOS: From the `glucose-BE/glucoseBE` directory, run:

    ```sh
    rm db.sqlite3
    python3 manage.py migrate
    python3 manage.py runserver 0.0.0.0:8000
    ```

## Run web demo
- Windows: Run `batch\web.bat`.
- Linux/macOS: From the `glucose-FE` directory, run:

    ```sh
    pnpm dev --open
    ```

## Run Android demo

1. Download the APK from [releases](https://github.com/creaper9487/2025-AI-glucose/releases) and install it.

2. Connect your phone to the computer, and configure **USB tethering**. On macOS and Hackintosh, you need to install **HoRNDIS Kext** to support USB tethering.

3. Use the following commands to check the IPv4 address assigned to your computer by your phone. This address is required to connect the app to the server.
- Windows:

    ```sh
    ipconfig
    ```

- Linux/macOS:

    ```sh
    ifconfig
    ```

4. Run the server.

5. Open the app and enter the IPv4 address you found in step 3 into the start-up AlertDialog to connect to the server.