<script setup>
import { RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { useChatStore } from '@/stores/chatStore'

const authStore = useAuthStore()
const route = useRoute()
const chatStore = useChatStore()

const isActive = (path) => {
  return route.path === path
}
</script>

<template>
  <nav class="bg-gradient-to-r from-blue-600 to-teal-500 shadow-lg sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <div class="flex">
          <div class="flex-shrink-0 flex items-center">
            <RouterLink to="/" class="ml-2 text-white font-semibold text-lg">DiaBeats</RouterLink>
          </div>
          <div class="hidden sm:ml-6 sm:flex sm:space-x-8">
            <RouterLink to="/dashboard" 
              class="border-transparent text-white hover:border-white hover:text-white inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium transition-colors duration-200"
              :class="{ 'border-white font-bold': isActive('/dashboard') }">
              數據紀錄
            </RouterLink>
            <RouterLink to="/figure" 
              class="border-transparent text-white hover:border-white hover:text-white inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium transition-colors duration-200"
              :class="{ 'border-white font-bold': isActive('/figure') }">
              數據圖表
            </RouterLink>
          </div>
        </div>

        <!-- 右側下拉選單 -->
        <div class="hidden sm:ml-6 sm:flex sm:items-center">
          <div class="ml-3 relative group">
            <button class="text-white font-medium hover:text-teal-200 flex items-center focus:outline-none">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            <div class="absolute right-0 mt-2 w-48 bg-white text-gray-700 rounded-md shadow-lg hidden group-hover:block group-focus-within:block">
              <div v-if="authStore.username">
                <RouterLink to="/profile" class="block px-4 py-2 text-sm">
                  個人資料
                </RouterLink>
                <RouterLink to="/logout" class="block px-4 py-2 text-sm">
                  登出
                </RouterLink>
              </div>
              <RouterLink to="/login" class="block px-4 py-2 text-sm">
                登入/註冊
              </RouterLink>
              <button 
                @click="chatStore.profileWindow = true"
                class="block px-4 py-2 text-sm">
                資料輸入
              </button>
            </div>
          </div>
        </div>
        
        <!-- Mobile menu button -->
        <div class="-mr-2 flex items-center sm:hidden">
          <button type="button" class="inline-flex items-center justify-center p-2 rounded-md text-white hover:text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white">
            <svg class="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
/* 保留原有的動畫效果，但進行優化 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.router-link-active {
  animation: fadeIn 0.3s ease-in-out;
}

/* 讓下拉選單顯示時有動畫效果 */
.group:hover .group-hover\:block {
  display: block;
}

.group .group-hover\:block {
  display: none;
}

/* 使用 focus-within 保持下拉選單顯示 */
.group-focus-within\:block {
  display: block;
}
</style>
