<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();
const username_or_email = ref('');
const password = ref('');
const isLoading = ref(false);
const loginError = ref('');
const router = useRouter();
const handleLogin = async (event) => {
    event.preventDefault();
    loginError.value = '';
    isLoading.value = true;
    
    try {
        await authStore.login({
            "username_or_email": username_or_email.value,
            "password": password.value
        });
        
        // 檢查登入是否成功
        if (authStore.token && authStore.token.length > 0) {
            // 登入成功
            authStore.username = username_or_email.value;
            router.push('/');
        }
    } catch (error) {
        console.error('登入失敗:', error);
        loginError.value = '帳號或密碼不正確，請重試';
    } finally {
        isLoading.value = false;
    }
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
    <div class="w-full p-8 bg-white rounded-xl shadow-xl transition-all duration-300 hover:shadow-2xl transform hover:scale-102">

        <h2 class="text-center text-3xl font-extrabold text-gray-900 mb-2">歡迎回來</h2>
        <p class="text-center text-sm text-gray-600 mb-8">
            登入以管理血糖數據並獲取個人化建議
        </p>
        
        <form @submit="handleLogin" class="space-y-4 flex flex-col mx-auto">
            <div v-if="loginError" class="bg-red-50 border-l-4 border-red-500 p-4 mb-4">
            <div class="flex">
                <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-red-500" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                </svg>
                </div>
                <div class="ml-3">
                <p class="text-sm text-red-700">
                    {{ loginError }}
                </p>
                </div>
            </div>
            </div>
            
            <div class="text-center">
            <label for="username_or_email" class="block text-sm font-medium text-gray-700 text-left ml-1">帳號或電子郵件</label>
            <div class="mt-1 relative rounded-md shadow-sm">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                </svg>
                </div>
                <input
                v-model="username_or_email"
                id="username_or_email"
                name="username_or_email"
                type="text"
                required
                class="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-md shadow-sm placeholder-gray-400"
                placeholder="帳號或電子郵件"
                />
            </div>
            </div>

            <div class="text-center">
            <label for="password" class="block text-sm font-medium text-gray-700 text-left ml-1">密碼</label>
            <div class="mt-1 relative rounded-md shadow-sm">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
                </svg>
                </div>
                <input
                v-model="password"
                id="password"
                name="password"
                type="password"
                required
                class="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-md shadow-sm placeholder-gray-400"
                placeholder="密碼"
                />
            </div>
            </div>

            <div class="flex items-center justify-between">
            <div class="flex items-center">
                <input
                id="remember_me"
                name="remember_me"
                type="checkbox"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label for="remember_me" class="ml-2 block text-sm text-gray-900">
                記住我
                </label>
            </div>

            <div class="text-sm">
                <a href="#" class="font-medium text-blue-600 hover:text-blue-500">
                忘記密碼?
                </a>
            </div>
            </div>

            <div class="text-center">
            <button
                type="submit"
                :disabled="isLoading"
                class="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-500 hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
            >
                <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {{ isLoading ? '登入中...' : '登入' }}
            </button>
            </div>
        </form>

        <div class="mt-6">
            <div class="relative">
                <div class="absolute inset-0 flex items-center">
                    <div class="w-full border-t border-gray-300"></div>
                </div>
                <div class="relative flex justify-center text-sm">
                    <span class="px-2 bg-white text-gray-500">
                        或使用
                    </span>
                </div>
            </div>

            <div class="mt-6">
                <button
                    type="button"
                    @click="handleGoogleLogin"
                    class="w-full inline-flex justify-center py-3 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
                >
                    <svg class="h-5 w-5 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                        <path d="M12.545,10.239v3.821h5.445c-0.712,2.315-2.647,3.972-5.445,3.972c-3.332,0-6.033-2.701-6.033-6.032s2.701-6.032,6.033-6.032c1.498,0,2.866,0.549,3.921,1.453l2.814-2.814C17.503,2.988,15.139,2,12.545,2C7.021,2,2.543,6.477,2.543,12s4.478,10,10.002,10c8.396,0,10.249-7.85,9.426-11.748L12.545,10.239z" fill="#FFC107"/>
                        <path d="M12.545,10.239l9.426,0.013c0.823,3.898-1.03,11.748-9.426,11.748c-3.566,0-6.726-1.782-8.612-4.491l3.04-2.315C7.9,16.521,10.053,17.705,12.545,17.705c2.798,0,4.733-1.657,5.445-3.972h-5.445V10.239z" fill="#FF3D00"/>
                        <path d="M12.545,17.705c-2.492,0-4.645-1.184-6.072-3.067l-3.04,2.315c1.886,2.709,5.046,4.491,8.612,4.491c2.594,0,4.958-0.988,6.735-2.587l-2.814-2.814C14.91,16.989,13.77,17.705,12.545,17.705z" fill="#4CAF50"/>
                        <path d="M12.545,8.822c1.327,0,2.526,0.557,3.413,1.453l2.814-2.814C17.035,5.76,14.937,4.822,12.545,4.822c-4.558,0-8.282,3.724-8.282,8.282h3.04C7.303,9.634,9.673,8.822,12.545,8.822z" fill="#1976D2"/>
                    </svg>
                    使用 Google 帳號登入
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
input:focus {
    outline: none;
    transition: all 0.3s ease;
}

.hover\:scale-102:hover {
    --tw-scale-x: 1.02;
    --tw-scale-y: 1.02;
    transform: var(--tw-transform);
}
</style>