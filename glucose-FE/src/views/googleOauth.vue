<script setup>
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

onMounted(async () => {
    const code = route.query.code;
    if (code) {
        const decodedCode = decodeURIComponent(code);
        try {
            // 發送授權碼到後端
            const response = await axios.post('/api/auth/google/', { code: decodedCode });
            
            // 從回應中獲取令牌並設置到authStore中
            if (response.data && response.data.access && response.data.refresh) {
                // 設置身份驗證令牌
                authStore.token = [response.data.access, response.data.refresh];
                
                // 設置用戶名
                if (response.data.user && response.data.user.username) {
                    authStore.username = response.data.user.username;
                } else if (response.data.user && response.data.user.email) {
                    // 如果沒有用戶名，可以使用電子郵件作為備用
                    authStore.username = response.data.user.email.split('@')[0];
                }
                
                // 設置axios默認請求頭
                axios.defaults.headers.common['Authorization'] = `Bearer ${authStore.token[0]}`;
                
                // 登入成功後重定向到首頁或儀表板
                router.push('/dashboard');
            } else {
                console.error('登入回應缺少令牌數據', response.data);
                router.push('/login');
            }
        } catch (error) {
            console.error('Google OAuth登入過程中出錯:', error);
            router.push('/login');
        }
    } else {
        // 如果沒有授權碼，重定向回登入頁面
        router.push('/login');
    }
});
</script>

<template>
    <div class="flex flex-col items-center justify-center h-screen">
        <div class="text-center">
            <h2 class="text-xl mb-4">處理Google登入中...</h2>
            <div class="w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
        </div>
    </div>
</template>