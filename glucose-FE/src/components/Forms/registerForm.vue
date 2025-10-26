<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();
const router = useRouter();

const username = ref('');
const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const agreeTerms = ref(false);
const isLoading = ref(false);
const registerError = ref('');

// 表單驗證
const usernameError = ref('');
const emailError = ref('');
const passwordError = ref('');
const confirmPasswordError = ref('');

const validateUsername = () => {
    if (username.value.length < 3) {
        usernameError.value = '用戶名至少需要3個字母';
        return false;
    }
    usernameError.value = '';
    return true;
};

const validateEmail = () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email.value)) {
        emailError.value = '請輸入有效的電子郵件地址';
        return false;
    }
    emailError.value = '';
    return true;
};

const validatePassword = () => {
    if (password.value.length < 6) {
        passwordError.value = '密碼至少需要6個字母';
        return false;
    }
    passwordError.value = '';
    return true;
};

const validateConfirmPassword = () => {
    if (password.value !== confirmPassword.value) {
        confirmPasswordError.value = '密碼不相同';
        return false;
    }
    confirmPasswordError.value = '';
    return true;
};

const handleRegister = async (event) => {
    event.preventDefault();
    
    // 验证所有字段
    const isUsernameValid = validateUsername();
    const isEmailValid = validateEmail();
    const isPasswordValid = validatePassword();
    const isConfirmPasswordValid = validateConfirmPassword();
    
    if (!isUsernameValid || !isEmailValid || !isPasswordValid || !isConfirmPasswordValid || !agreeTerms.value) {
        if (!agreeTerms.value) {
            registerError.value = '請同意使用條款和隱私政策';
        }
        return;
    }
    
    registerError.value = '';
    isLoading.value = true;
    
    try {
        await authStore.register({
            "username": username.value,
            "password": password.value,
            "email": email.value
        });
        
        // 檢查是否註冊成功
        if (authStore.token && authStore.token.length > 0) {
            // 註冊成功，重新導向到首頁
            router.push('/');
        }
    } catch (error) {
        console.error('註冊失敗:', error);
        registerError.value = '註冊失敗，請稍後重試或使用其他用戶名';
    } finally {
        isLoading.value = false;
    }
};
</script>

<template>
    <div class="h-full w-full max-w-md p-4 bg-slate-100 rounded-xl shadow-xl transition-all duration-300 hover:shadow-2xl transform hover:scale-102">
        <div class="flex justify-center mb-6">
            <div class="w-16 h-16 bg-blue-500 rounded-full flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
                </svg>
            </div>
        </div>
        
        <h2 class="text-center text-3xl font-extrabold text-gray-900 mb-2">建立帳戶</h2>
        <p class="text-center text-sm text-gray-600 mb-8">
            註冊來追蹤血糖健康數據並獲取AI健康建議
        </p>
        
        <form @submit="handleRegister" class="space-y-2">
            <div v-if="registerError" class="bg-red-50 border-l-4 border-red-500 p-4 mb-4">
                <div class="flex">
                    <div class="flex-shrink-0">
                        <svg class="h-5 w-5 text-red-500" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                        </svg>
                    </div>
                    <div class="ml-3">
                        <p class="text-sm text-red-700">{{ registerError }}</p>
                    </div>
                </div>
            </div>
            
            <div>
                <label for="username" class="block text-sm font-medium text-gray-700">使用者名稱</label>
                <div class="mt-1 relative rounded-md shadow-sm">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg class="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                        </svg>
                    </div>
                    <input
                        v-model="username"
                        id="username"
                        name="username"
                        type="text"
                        required
                        @blur="validateUsername"
                        class="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-md shadow-sm placeholder-gray-400"
                        :class="{'border-red-300': usernameError}"
                        placeholder="你的使用者名稱"
                    />
                </div>
                <p v-if="usernameError" class="mt-1 text-sm text-red-600">{{ usernameError }}</p>
            </div>

            <div>
                <label for="email" class="block text-sm font-medium text-gray-700">電子郵件</label>
                <div class="mt-1 relative rounded-md shadow-sm">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg class="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                            <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                            <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
                        </svg>
                    </div>
                    <input
                        v-model="email"
                        id="email"
                        name="email"
                        type="email"
                        required
                        @blur="validateEmail"
                        class="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-md shadow-sm placeholder-gray-400"
                        :class="{'border-red-300': emailError}"
                        placeholder="你的電子郵件地址"
                    />
                </div>
                <p v-if="emailError" class="mt-1 text-sm text-red-600">{{ emailError }}</p>
            </div>

            <div>
                <label for="password" class="block text-sm font-medium text-gray-700">密碼</label>
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
                        @blur="validatePassword"
                        class="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-md shadow-sm placeholder-gray-400"
                        :class="{'border-red-300': passwordError}"
                        placeholder="設置一個強密碼"
                    />
                </div>
                <p v-if="passwordError" class="mt-1 text-sm text-red-600">{{ passwordError }}</p>
            </div>

            <div>
                <label for="confirm-password" class="block text-sm font-medium text-gray-700">確認密碼</label>
                <div class="mt-1 relative rounded-md shadow-sm">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg class="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                            <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
                        </svg>
                    </div>
                    <input
                        v-model="confirmPassword"
                        id="confirm-password"
                        name="confirm-password"
                        type="password"
                        required
                        @blur="validateConfirmPassword"
                        class="focus:ring-blue-500 focus:border-blue-500 block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-md shadow-sm placeholder-gray-400"
                        :class="{'border-red-300': confirmPasswordError}"
                        placeholder="再次輸入您的密碼"
                    />
                </div>
                <p v-if="confirmPasswordError" class="mt-1 text-sm text-red-600">{{ confirmPasswordError }}</p>
            </div>

            <div class="flex items-center">
                <input
                    v-model="agreeTerms"
                    id="agree-terms"
                    name="agree-terms"
                    type="checkbox"
                    required
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label for="agree-terms" class="ml-2 block text-sm text-gray-900">
                    我同意<a href="#" class="text-blue-600 hover:text-blue-500">使用條款</a>和<a href="#" class="text-blue-600 hover:text-blue-500">隱私政策</a>
                </label>
            </div>

            <div>
                <button
                    type="submit"
                    :disabled="isLoading"
                    class="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-500 hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200"
                >
                    <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    {{ isLoading ? '註冊中...' : '創建帳戶' }}
                </button>
            </div>
        </form>

        <div class="mt-6 text-center">
            <p class="text-sm text-gray-600">
                已有帳戶? 
                <RouterLink to="/login" class="font-medium text-blue-600 hover:text-blue-500">
                    立即登入
                </RouterLink>
            </p>
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