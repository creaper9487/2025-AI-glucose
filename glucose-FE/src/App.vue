<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import NavBar from '@/components/NavBar.vue';
import InputForm from '@/components/Forms/inputForm.vue';
import FoodInput from '@/components/Forms/foodInput.vue';
import ChatBot from '@/components/pop.vue';

const authStore = useAuthStore();
const showWelcome = ref(true);
const showAnimation = ref(true);

onMounted(() => {
  // 初次進入頁面顯示歡迎信息
  setTimeout(() => {
    showAnimation.value = false;
  }, 1500);
  
  setTimeout(() => {
    showWelcome.value = false;
  }, 3000);
});

const closeWelcome = () => {
  showWelcome.value = false;
};
</script>

<template>
  <!-- 頁面容器 -->
  <div class="min-h-screen bg-gradient-to-b from-gray-100 to-blue-50">
    <!-- 導航欄 -->
    <NavBar />
    
    <!-- 歡迎橫幅 -->
    <transition name="welcome-fade">
      <div v-if="showWelcome" class="fixed top-16 inset-x-0 z-20 bg-blue-600 text-white py-3 px-6 shadow-lg">
        <div class="max-w-7xl mx-auto flex justify-between items-center">
          <div class="flex items-center">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 mr-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
            <div>
              <h3 class="text-lg font-bold">歡迎使用血糖管理系統</h3>
              <p class="text-sm text-blue-100">記錄您的血糖數據，獲取 AI 個性化健康建議</p>
            </div>
          </div>
          <button @click="closeWelcome" class="text-white hover:text-blue-200 transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>
    </transition>
    
    <!-- 主要內容 -->
    <div class="max-w-7xl mx-auto pt-24 pb-12 px-4 sm:px-6 lg:px-8">
      <!-- 頁面標題 -->
      <div class="text-center mb-12" :class="{ 'animate-welcome': showAnimation }">
        <h1 class="text-3xl font-extrabold text-gray-900 sm:text-4xl">
          <span class="block">血糖管理</span>
          <span class="block text-blue-600">智能健康助手</span>
        </h1>
        <p class="mt-3 max-w-md mx-auto text-base text-gray-500 sm:text-lg md:mt-5 md:text-xl md:max-w-3xl">
          輕鬆追蹤血糖、飲食和運動數據，獲取 AI 個性化健康建議，助您保持健康生活。
        </p>
      </div>
      
      <!-- 用戶歡迎卡片 -->
      <div v-if="authStore.username" class="mb-10 mx-auto max-w-lg">
        <div class="bg-white rounded-lg shadow-xl overflow-hidden transform transition-all duration-300 hover:shadow-2xl">
          <div class="bg-gradient-to-r from-blue-500 to-purple-600 px-6 py-8 sm:p-10 sm:pb-6">
            <div class="flex items-center justify-center">
              <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center shadow-lg">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
            <div class="mt-5 text-center">
              <h3 class="text-2xl font-extrabold text-white tracking-tight">
                歡迎回來，{{ authStore.username }}
              </h3>
              <p class="mt-2 text-base text-purple-100">
                今天的血糖狀況如何？記錄您的數據以獲取更準確的健康建議。
              </p>
            </div>
          </div>
          <div class="pb-8 px-6 bg-white sm:p-10 sm:pt-6">
            <div class="mt-6 grid grid-cols-2 gap-4">
              <a href="#record" class="flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-500 hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200">
                紀錄數據
              </a>
              <a href="/figure" class="flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors duration-200">
                查看圖表
              </a>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 主要功能區 -->
      <div class="mt-10 grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-2 place-items-center">
        <!-- 血糖數據記錄表單 -->
        <div id="record" class="w-full flex justify-center">
          <InputForm />
        </div>
        
        <!-- 圖片分析和 AI 建議 -->
        <div class="w-full flex justify-center">
          <FoodInput />
        </div>
      </div>
    </div>
    
    <!-- 頁腳信息 -->
    <footer class="bg-white shadow-inner mt-10">
      <div class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
        <div class="flex flex-col items-center md:flex-row md:justify-between text-sm text-gray-500">
          <div class="flex items-center mb-4 md:mb-0">
            <img src="@/assets/logo.svg" alt="Logo" class="h-8 w-auto mr-2" />
            <span>GlucTrack 血糖管理 © {{ new Date().getFullYear() }}</span>
          </div>
          <div class="flex space-x-6">
            <a href="https://github.com/creaper9487/2025-AI-glucose" target="_blank" class="hover:text-gray-900 flex items-center">
              <svg class="h-6 w-6 mr-1" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                <path fill-rule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clip-rule="evenodd" />
              </svg>
              關於我們
            </a>
            <a href="#" class="hover:text-gray-900">隱私政策</a>
            <a href="#" class="hover:text-gray-900">使用條款</a>
          </div>
        </div>
      </div>
    </footer>
    
    <!-- 彈出窗口組件 -->
    <ChatBot />
  </div>
</template>

<style scoped>
.welcome-fade-enter-active,
.welcome-fade-leave-active {
  transition: all 0.5s ease;
}

.welcome-fade-enter-from,
.welcome-fade-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translate3d(0, 40px, 0);
  }
  to {
    opacity: 1;
    transform: translate3d(0, 0, 0);
  }
}

.animate-welcome {
  animation: fadeInUp 1s ease-out;
}
</style>