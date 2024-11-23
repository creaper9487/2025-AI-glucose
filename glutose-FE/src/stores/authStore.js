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
                const response = await axiosInstance.post('/api/register/', credentials);
                console.log(response.data);
            } catch (error) {
                console.error(error);
            }
        },
        async login(credentials) {
            try {
                const response = await axios.post('/api/login/', credentials);
                this.token = response.data.token;
                axios.defaults.headers.common['Authorization'] = `Bearer ${this.token[0]}`;
            } catch (error) {
                console.error('Error logging in:', error);
            }
        },
        async fetchUser(){
            try {
                const response = await axios.get('/api/user/');
                this.username = response.data.username;
            } catch (error) {
                console.error('Error fetching user:', error);
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