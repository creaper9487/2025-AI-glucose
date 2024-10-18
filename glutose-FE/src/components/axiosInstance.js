import axios from 'axios';

const axiosInstance = axios.create({
    baseURL: '/', // Replace with your base URL
    // timeout: 5000, // Set a timeout in milliseconds (optional)
    headers: {
        'Content-Type': 'application/json',
    }
});

export default axiosInstance;