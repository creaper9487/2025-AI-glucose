<script setup>
import { ref, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import NavBar from '@/components/NavBar.vue';
import aboutUsBtn from '@/components/aboutUsBtn.vue';

// 動畫效果控制
const isVisible = ref(false);
const features = ref([
  {
    icon: '<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" /></svg>',
    title: '智能數據記錄',
    description: '輕鬆記錄血糖、碳水攝取、運動時間和胰島素劑量，建立完整健康檔案。'
  },
  {
    icon: '<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>',
    title: '視覺化數據圖表',
    description: '直觀的數據圖表，一目了然地追蹤血糖趨勢，識別高低血糖模式。'
  },
  {
    icon: '<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" /></svg>',
    title: '食物碳水分析',
    description: '拍攝食物照片，AI自動估算碳水化合物含量，輔助餐前胰島素計算。'
  },
  {
    icon: '<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>',
    title: 'AI健康建議',
    description: '基於您的血糖數據，AI提供個人化的健康建議和生活方式調整。'
  },
  {
    icon: '<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>',
    title: '糖尿病風險評估',
    description: '通過填寫簡單問卷，評估您的糖尿病風險，及早發現潛在健康隱患。'
  },
  {
    icon: '<svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>',
    title: '資料安全保障',
    description: '所有健康數據安全加密存儲，保護您的隱私，符合健康數據保護標準。'
  }
]);

// 頁面載入後觸發動畫
onMounted(() => {
  setTimeout(() => {
    isVisible.value = true;
  }, 100);
});
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-gray-900 to-gray-800 text-white">
    <NavBar />
    
    <!-- 主視覺區塊 -->
    <section class="pt-20 pb-12 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
      <div class="flex flex-col md:flex-row items-center justify-between">
        <div class="md:w-1/2 mb-10 md:mb-0" :class="{ 'opacity-100 translate-x-0': isVisible, 'opacity-0 -translate-x-10': !isVisible }">
          <h1 class="text-4xl md:text-5xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-teal-400 mb-6 transition-all duration-1000">
            DiaBeats
          </h1>
          <h2 class="text-2xl md:text-3xl font-semibold text-white mb-6 transition-all duration-1000 delay-300">
            智能血糖管理的全新體驗
          </h2>
          <p class="text-gray-300 text-lg mb-8 transition-all duration-1000 delay-500">
            結合AI技術的智能血糖監測與管理工具，助您輕鬆掌握健康，預防糖尿病。無論您是糖尿病患者、高風險人群，還是注重健康的普通人，DiaBeats都是您的健康守護者。
          </p>
          <div class="flex flex-wrap gap-4 transition-all duration-1000 delay-700">
            <RouterLink to="/login" class="px-6 py-3 bg-gradient-to-r from-blue-600 to-teal-500 rounded-lg shadow-lg hover:shadow-xl transform hover:-translate-y-1 transition-all duration-300 font-medium">
              立即註冊使用
            </RouterLink>
            <RouterLink to="/" class="px-6 py-3 bg-gray-700 rounded-lg shadow-lg hover:shadow-xl hover:bg-gray-600 transform hover:-translate-y-1 transition-all duration-300 font-medium">
              了解更多
            </RouterLink>
          </div>
        </div>
        
        <!-- 右側圖片/動畫 -->
        <div class="md:w-1/2 flex justify-center" :class="{ 'opacity-100 translate-x-0': isVisible, 'opacity-0 translate-x-10': !isVisible }">
          <div class="relative w-full max-w-md transition-all duration-1000">
            <!-- 模擬手機屏幕 -->
            <div class="bg-gray-900 rounded-3xl shadow-2xl overflow-hidden border-4 border-gray-800">
              <div class="bg-gradient-to-r from-blue-600 to-teal-500 h-6 w-full flex items-center justify-center rounded-t-2xl">
                <div class="w-16 h-4 bg-gray-900 rounded-full"></div>
              </div>
              <div class="p-4 bg-gray-900">
                <!-- 模擬血糖圖表 -->
                <div class="bg-gray-800 rounded-xl p-4 mb-4">
                  <div class="flex justify-between items-center mb-4">
                    <h3 class="text-white font-semibold">血糖趨勢</h3>
                    <div class="flex space-x-2">
                      <span class="px-2 py-1 bg-blue-500 rounded-full text-xs text-white">週</span>
                    </div>
                  </div>
                  <div class="h-40 flex items-end space-x-2 mb-4">
                    <div v-for="height in [40, 60, 30, 80, 50, 70, 45]" :key="height" 
                        class="bg-gradient-to-t from-blue-600 to-teal-400 rounded-t-md w-full h-0" 
                        :style="`height: ${height}%`"
                        :class="{ 'animate-chart': isVisible }"></div>
                  </div>
                </div>
                
                <!-- 模擬記錄按鈕 -->
                <div class="bg-gray-800 rounded-xl p-4">
                  <div class="flex items-center mb-2">
                    <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center mr-2">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                      </svg>
                    </div>
                    <h3 class="text-white font-semibold">新增記錄</h3>
                  </div>
                  <div class="flex space-x-2 mt-2">
                    <div class="w-1/2 h-6 bg-gray-700 rounded-md animate-pulse"></div>
                    <div class="w-1/2 h-6 bg-gray-700 rounded-md animate-pulse delay-200"></div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 裝飾小點 -->
            <div class="absolute -top-4 -right-4 w-12 h-12 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full opacity-60 animate-blob"></div>
            <div class="absolute -bottom-4 -left-4 w-16 h-16 bg-gradient-to-r from-yellow-500 to-orange-500 rounded-full opacity-60 animate-blob animation-delay-2000"></div>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 功能特點區塊 -->
    <section class="py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
      <h2 class="text-3xl font-bold text-center mb-16 bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-teal-400">
        強大功能，智慧管理
      </h2>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        <div v-for="(feature, index) in features" :key="index" 
            class="bg-gray-800 bg-opacity-50 rounded-xl p-6 shadow-lg hover:shadow-xl transform hover:-translate-y-2 transition-all duration-300 backdrop-blur-sm border border-gray-700"
            :class="{ 'animate-fade-in-up': isVisible }"
            :style="`animation-delay: ${200 + index * 100}ms`">
          <div class="w-14 h-14 bg-gradient-to-r from-blue-600 to-teal-500 rounded-lg flex items-center justify-center mb-6 shadow-lg" v-html="feature.icon"></div>
          <h3 class="text-xl font-semibold text-white mb-3">{{ feature.title }}</h3>
          <p class="text-gray-300">{{ feature.description }}</p>
        </div>
      </div>
    </section>
    
    <!-- 使用流程區塊 -->
    <section class="py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto bg-gradient-to-r from-gray-800 to-gray-900 rounded-3xl">
      <h2 class="text-3xl font-bold text-center mb-16 bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-teal-400">
        三步驟開始智能血糖管理
      </h2>
      
      <div class="flex flex-col md:flex-row items-center justify-between gap-8">
        <div class="md:w-1/3 flex flex-col items-center text-center p-6">
          <div class="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center text-2xl font-bold mb-6">1</div>
          <h3 class="text-xl font-semibold mb-4">註冊帳號</h3>
          <p class="text-gray-300">簡單註冊，設置基本健康資料，開始您的健康管理旅程。</p>
        </div>
        
        <div class="hidden md:block w-12 h-2 bg-gradient-to-r from-blue-600 to-transparent rounded-full"></div>
        
        <div class="md:w-1/3 flex flex-col items-center text-center p-6">
          <div class="w-16 h-16 bg-teal-500 rounded-full flex items-center justify-center text-2xl font-bold mb-6">2</div>
          <h3 class="text-xl font-semibold mb-4">記錄數據</h3>
          <p class="text-gray-300">輕鬆記錄血糖、飲食、運動數據，或使用拍照功能分析食物碳水含量。</p>
        </div>
        
        <div class="hidden md:block w-12 h-2 bg-gradient-to-r from-transparent to-purple-600 rounded-full"></div>
        
        <div class="md:w-1/3 flex flex-col items-center text-center p-6">
          <div class="w-16 h-16 bg-purple-600 rounded-full flex items-center justify-center text-2xl font-bold mb-6">3</div>
          <h3 class="text-xl font-semibold mb-4">獲取洞察</h3>
          <p class="text-gray-300">查看趨勢圖表，接收AI健康建議，掌握血糖變化規律。</p>
        </div>
      </div>
    </section>
    
    <!-- 呼籲行動區塊 -->
    <section class="py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
      <div class="bg-gradient-to-r from-blue-900 to-teal-900 rounded-3xl p-10 md:p-16 shadow-2xl relative overflow-hidden">
        <!-- 背景裝飾 -->
        <div class="absolute top-0 left-0 w-full h-full overflow-hidden opacity-20">
          <div class="absolute top-0 left-0 w-40 h-40 bg-blue-500 rounded-full filter blur-3xl transform -translate-x-1/2 -translate-y-1/2"></div>
          <div class="absolute bottom-0 right-0 w-60 h-60 bg-teal-500 rounded-full filter blur-3xl transform translate-x-1/3 translate-y-1/3"></div>
        </div>
        
        <div class="relative z-10 text-center">
          <h2 class="text-3xl md:text-4xl font-bold mb-6">立即開始您的健康管理旅程</h2>
          <p class="text-xl text-gray-200 mb-10 max-w-2xl mx-auto">
            DiaBeats助您輕鬆掌握血糖變化，保持健康生活方式，預防糖尿病風險。
          </p>
          <RouterLink to="/register" class="inline-block px-8 py-4 bg-gradient-to-r from-blue-600 to-teal-500 rounded-lg shadow-lg hover:shadow-xl transform hover:-translate-y-1 transition-all duration-300 font-medium text-lg">
            免費註冊使用
          </RouterLink>
        </div>
      </div>
    </section>
    
    <!-- 頁腳區塊 -->
    <footer class="py-10 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto border-t border-gray-800">
      <div class="flex flex-col md:flex-row justify-between items-center">
        <div class="mb-6 md:mb-0">
          <h2 class="text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-teal-400">
            DiaBeats
          </h2>
          <p class="text-gray-400 mt-2">智能血糖管理的全新體驗</p>
        </div>
        
        <div class="flex space-x-8">
          <RouterLink to="/" class="text-gray-300 hover:text-white transition-colors">首頁</RouterLink>
          <RouterLink to="/figure" class="text-gray-300 hover:text-white transition-colors">數據圖表</RouterLink>
          <RouterLink to="/login" class="text-gray-300 hover:text-white transition-colors">登入/註冊</RouterLink>
        </div>
      </div>
      
      <div class="mt-8 pt-8 border-t border-gray-800 flex flex-col md:flex-row justify-between items-center">
        <p class="text-gray-400 text-sm mb-4 md:mb-0">© 2025 DiaBeats. 保留所有權利。</p>
        <aboutUsBtn />
      </div>
    </footer>
  </div>
</template>

<style scoped>
.animate-chart {
  animation: chart-grow 1.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

@keyframes chart-grow {
  from { height: 0; }
  to { height: var(--height); }
}

.animate-blob {
  animation: blob 7s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

@keyframes blob {
  0% {
    transform: scale(1);
  }
  33% {
    transform: scale(1.1);
  }
  66% {
    transform: scale(0.9);
  }
  100% {
    transform: scale(1);
  }
}

.animate-fade-in-up {
  animation: fade-in-up 0.8s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>