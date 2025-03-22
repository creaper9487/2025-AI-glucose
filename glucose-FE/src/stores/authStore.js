//testuser
// 用戶名: default_user
// 密碼: defaultpassword123
// 電子郵件: default@example.com

import { defineStore } from 'pinia';
import axios from 'axios';
import axiosInstance from '@/components/axiosInstance';
export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: [],
        username: '',
        lang: ''
    }),

    actions: {
        async register(credentials) {
            try {
                console.log(credentials);
                const response = await axiosInstance.post('/api/register/', credentials);
                this.token = [ response.access,response.refresh];
                axios.defaults.headers.common['Authorization'] = `Bearer ${this.token[0]}`;
            } catch (error) {
                console.error(error);
            }
        },
        async login(credentials) {
            try {
                console.log(credentials);
                const response = await axiosInstance.post('/api/token/', credentials);
                this.token = [ response.data.access,response.data.refresh];
                axios.defaults.headers.common['Authorization'] = `Bearer ${this.token[0]}`;
            } catch (error) {
                console.error('Error logging in:', error);
            }
        },
        async refreshTokens(){
            try {
                const response = await axiosInstance.post('/api/token/refresh/', {"refresh": this.token[1]});
                this.token = [ response.data.access,response.data.refresh];
                // axios.defaults.headers.common['Authorization'] = `Bearer ${this.token[0]}`;
            } catch (error) {
                console.error('Error refreshing tokens:', error);
                this.token = [];
            }
        },
        logout() {
            this.token = null;
            this.username = '';
            this.lang = 'zh';
            delete axios.defaults.headers.common['Authorization'];
        }
    }
});