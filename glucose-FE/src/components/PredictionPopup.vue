<script setup>
import { ref, onMounted } from 'vue';
import { useChatStore } from '@/stores/chatStore';

const chatStore = useChatStore();
const loading = ref(false);
const resultShow = ref(false);

onMounted(async () => {
  // 初始化表單時獲取最新血糖記錄
  await chatStore.getLatestBloodGlucose();
});

const handleClose = () => {
  chatStore.predictionWindow = false;
  resultShow.value = false;
  chatStore.predictionResult = null;
};

const handleSubmit = async () => {
  if (!chatStore.predictionInput.previous_blood_glucose) {
    alert('請輸入上次血糖值');
    return;
  }
  
  loading.value = true;
  const result = await chatStore.predictBloodGlucose();
  loading.value = false;
  
  if (result) {
    resultShow.value = true;
  }
};
</script>

<template>
  <div v-if="chatStore.predictionWindow" class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
    <div class="bg-gray-800 rounded-lg p-6 max-w-md w-full">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl text-white font-semibold">AI 血糖預測</h2>
        <button @click="handleClose" class="text-gray-400 hover:text-white">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
      
      <div v-if="!resultShow">
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-300">上次血糖 (mg/dL)</label>
            <input 
              v-model="chatStore.predictionInput.previous_blood_glucose" 
              type="number" 
              class="w-full bg-gray-700 text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-300">胰島素注射量 (單位)</label>
            <input 
              v-model="chatStore.predictionInput.insulin_injection" 
              type="number" 
              class="w-full bg-gray-700 text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              min="0"
            />
          </div>
          
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-300">碳水攝取量 (克)</label>
            <input 
              v-model="chatStore.predictionInput.carbohydrate_intake" 
              type="number" 
              class="w-full bg-gray-700 text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              min="0"
            />
          </div>
          
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-300">時間間隔 (小時)</label>
            <input 
              v-model="chatStore.predictionInput.time_interval" 
              type="number" 
              class="w-full bg-gray-700 text-white rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              min="0.5" 
              step="0.5"
            />
          </div>
          
          <div class="pt-2">
            <button 
              type="submit" 
              class="w-full bg-teal-500 text-white rounded-md py-2 hover:bg-teal-600 transition-colors duration-300 flex items-center justify-center"
              :disabled="loading"
            >
              <span v-if="loading">預測中...</span>
              <span v-else>開始預測</span>
            </button>
          </div>
        </form>
      </div>
      
      <div v-else class="text-white">
        <div class="bg-gray-700 rounded-lg p-4 mb-4">
          <h3 class="text-lg font-semibold mb-2">預測結果</h3>
          <div class="space-y-2">
            <p class="flex justify-between">
              <span>預測血糖值:</span>
              <span class="font-bold text-teal-400">{{ chatStore.predictionResult.predicted_blood_glucose.toFixed(1) }} mg/dL</span>
            </p>
            <p class="flex justify-between">
              <span>預測時間點:</span>
              <span>{{ chatStore.predictionResult.predicted_time.toFixed(1) }} 小時後</span>
            </p>
          </div>
        </div>
        
        <div class="bg-gray-700 rounded-lg p-4">
          <h4 class="text-sm font-medium text-gray-300 mb-2">模型資訊</h4>
          <div class="text-xs text-gray-400 space-y-1">
            <p>模型版本: {{ chatStore.predictionResult.model_version }}</p>
            <p>上次訓練時間: {{ new Date(chatStore.predictionResult.last_trained).toLocaleString() }}</p>
            <p>模型性能: {{ chatStore.predictionResult.model_performance }}</p>
          </div>
        </div>
        
        <div class="mt-4">
          <button 
            @click="resultShow = false" 
            class="w-full bg-gray-600 text-white rounded-md py-2 hover:bg-gray-500 transition-colors duration-300"
          >
            重新預測
          </button>
        </div>
      </div>
    </div>
  </div>
</template>