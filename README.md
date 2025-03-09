# 2025-AI-glucose

## Pre-installation
- [Node.js](https://nodejs.org/)
- [Python 3](https://www.python.org/)
- Git clone this project repo

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
    
    If you encounter an `externally-managed-environment` error during `pip install -r requirements.txt`, create a virtual environment **(venv)** and install the dependencies within it.

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

2. Make sure your phone and computer are on the same network.

3. Run the server.

4. Open the app and enter the IPv4 address shown in the server terminal into the start-up AlertDialog to connect.