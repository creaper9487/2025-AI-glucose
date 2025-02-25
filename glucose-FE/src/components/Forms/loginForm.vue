<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
const authStore = useAuthStore();
const username_or_email = ref('');
const password = ref('');
const handleRegister = async (event) => {
    event.preventDefault(); 
    authStore.login({username_or_email:username_or_email.value, password:password.value});
};
const handleGoogleLogin = async () => {
    const clientId = '887111954782-4ak0653kmakl505jo33d7c23tpps1rge.apps.googleusercontent.com';
    const redirectUri = 'http://localhost:5173/oauth-callback';
    const scope = [
        'https://www.googleapis.com/auth/userinfo.profile',
        'https://www.googleapis.com/auth/userinfo.email'
    ].join(' ');

    const authUrl = new URL('https://accounts.google.com/o/oauth2/auth');
    authUrl.searchParams.set('response_type', 'code');
    authUrl.searchParams.set('client_id', clientId);
    authUrl.searchParams.set('redirect_uri', redirectUri);
    authUrl.searchParams.set('scope', scope);

    window.location.href = authUrl.toString();
};
</script>
<template>
<div class="w-96 p-10 bg-gray-700 shadow-lg rounded-lg relative">
    <h2 class="text-center text-white text-2xl mb-1">登入</h2>
    <p class="text-center text-slate-300 text-l mb-8">登入查看你的血糖健康紀錄
    </p>
    <form ref="formRef" @submit="handleRegister">
        <div class="relative mb-8">
            <input type="text" v-model="username_or_email" required
                class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
            <label
                class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">帳號</label>
        </div>
        <div class="relative mb-8">
            <input type="password" v-model="password" required
                class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
            <label
                class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">密碼</label>
        </div>
        <button type="submit" class="w-full p-2 mt-4 text-slate-800 bg-slate-200 rounded-lg hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-slate-400">
            登入
        </button>
        <button type="button" @click="handleGoogleLogin" class="w-full p-2 mt-4 text-slate-800 bg-slate-200 rounded-lg hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-slate-400">
            估狗登入
        </button>
    </form>
</div>
</template>

<style scoped>
input:focus ~ label,
input:valid ~ label {
  top: -20px;
  left: 0;
  color: #03e9f4;
  font-size: 12px;
}
</style>