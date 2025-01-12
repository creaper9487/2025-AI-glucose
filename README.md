# 2025-AI-glucose

To see the demo, follow these steps:

1. **Install Node.js**: If you don't have Node.js installed, download and install it from [nodejs.org](https://nodejs.org/).

2. **Install pnpm**: Once Node.js is installed, you can install `pnpm` by running the following command in your terminal:

    ```sh
    2025-AI-glucose>            cd glucose-FE
    2025-AI-glucose/glucose-FE> npm install -g pnpm
    2025-AI-glucose/glucose-FE> pnpm install
    ```

3. **Open Backend Server**: Install the required packages and start the server:

    ```sh
    2025-AI-glucose/glucose-FE> cd ../glucose-BE
    2025-AI-glucose/glucose-BE> pip install -r requirements.txt

    2025-AI-glucose/glucose-BE>           cd glucoseBE
    2025-AI-glucose/glucose-BE/glucoseBE> python manage.py migrate
    2025-AI-glucose/glucose-BE/glucoseBE> python manage.py runserver
    ```

4. **Run the demo**: Open a new terminal and run the demo using the following command:

    ```sh
    2025-AI-glucose/glucose-BE/glucoseBE> cd ../../glucose-FE
    2025-AI-glucose/glucose-FE>           pnpm dev
    ```
    
To see the Android demo, follow these steps:

1. **Install APK**: Download and install APK from releases.

2. **USB tether**: Connect your phone to computer, and configure USB tethering. In macOS and Hackintosh, you need to install HoRNDIS Kext to support USB tethering.

3. **Check IPv4 address**: Check your phone's IPv4 address using terminal command `ipconfig` (Windows) or `ifconfig` (Unix-like) .

4. **Open backend server**

5. **Open the app**: Finally, open the app and enter IPv4 address in start-up AlertDialog.