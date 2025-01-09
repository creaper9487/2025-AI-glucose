# 2025-AI-glucose

To see the demo, follow these steps:

1. **Install Node.js**: If you don't have Node.js installed, download and install it from [nodejs.org](https://nodejs.org/).
2. **Get into glucose-FE**:

    ```sh
    cd glucose-FE
    ```

3. **Install pnpm**: Once Node.js is installed, you can install `pnpm` by running the following command in your terminal:

    ```sh
    npm install -g pnpm
    pnpm install
    ```

4. **Open Backend Server**: Install the required packages and start the server:

    ```sh
    pip install -r requirements.txt
    python manage.py migrate
    python manage.py runserver
    ```

5. **Run the demo**: Open a new terminal and run the demo using the following command:

    ```sh
    pnpm dev
    ```

To see the Android demo, follow these steps:

1. **Install APK**: Download and install APK from [THERE](https://github.com/creaper9487/2025-AI-glucose/raw/refs/heads/main/glucose-mobi/Android/app/build/outputs/apk/debug/app-debug.apk).

2. **USB tether**: Connect your phone to computer, and configure USB tethering. In macOS and Hackintosh, you need to install HoRNDIS Kext to support USB tethering.

3. **Check IPv4 address**: Check your phone's IPv4 address using terminal command `ipconfig` (Windows) or `ifconfig` (Unix-like) .

4. **Open backend server**

5. **Open the app**: Finally, open the app and enter IPv4 address in start-up AlertDialog.