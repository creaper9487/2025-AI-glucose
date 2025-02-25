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
                
                const response = await axios.post('/api/login/', credentials);
                this.token = [ response.data.access,response.data.refresh];
                axios.defaults.headers.common['Authorization'] = `Bearer ${this.token[0]}`;
            } catch (error) {
                console.error('Error logging in:', error);
            }
        },
        async refreshTokens(){
            try {
                const response = await axios.post('/api/refresh/', {refresh: this.token[1]});
                this.token = [ response.access,response.refresh];
                axios.defaults.headers.common['Authorization'] = `Bearer ${this.token[0]}`;
            } catch (error) {
                console.error('Error refreshing tokens:', error);
            }
        },
        async fetchUser(){
            try {
                const response = await axios.get('/api/user/');
                this.username = response.data.username;
            // eslint-disable-next-line no-unused-vars
            } catch (error) {
                this.refreshTokens();
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