import { defineStore } from 'pinia';
import axios from 'axios';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: [],
        username: '',
        lang: ''
    }),
    actions: {
        async login(credentials) {
            try {
                const response = await axios.post('/api/login', credentials);
                this.token = response.data.token;
                axios.defaults.headers.common['Authorization'] = `Bearer ${this.token[0]}`;
            } catch (error) {
                console.error('Error logging in:', error);
            }
        },
        async fetchUserDetails() {
            try {
                const response = await axios.get('/api/user');
                this.username = response.data.username;
                this.lang = response.data.lang;
            } catch (error) {
                console.error('Error fetching user details:', error);
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