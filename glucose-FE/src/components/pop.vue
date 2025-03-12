<script setup>
import { useChatStore } from '@/stores/chatStore';
import ModelTrainingProgress from '../components/ModelTrainingProgress.vue';
import { ref, watch } from 'vue';
const chatStore = useChatStore();
const isAnimating = ref(false);
const height = ref(chatStore.profile.height);
const weight = ref(chatStore.profile.weight);
const age = ref(chatStore.profile.age);
const useForTraining = ref(chatStore.consent)
const isSaving = ref(false);
const saveSuccess = ref(false);

// 監聽彈窗狀態變化以觸發動畫
watch(() => chatStore.chatWindow || chatStore.senseWindow || chatStore.testWindow || chatStore.profileWindow, (newVal) => {
  if (newVal) {
    isAnimating.value = true;
    setTimeout(() => {
      isAnimating.value = false;
    }, 300);

    // 如果是個人資料設定窗口被打開，嘗試獲取用戶數據
    if (chatStore.profileWindow) {
      loadUserData();
    }
  }
});

const loadUserData = async () => {
  try {
    await chatStore.fetchUserProfile();
    height.value = chatStore.profile?.height || '';
    weight.value = chatStore.profile?.weight || '';
    age.value = chatStore.profile?.age || '';
  } catch (error) {
    console.error('無法載入用戶資料', error);
  }
};
const saveUserData = async () => {
  isSaving.value = true;
  saveSuccess.value = false;

  try {
    chatStore.profile = {
      height: height.value,
      weight: weight.value,
      age: age.value
    };
    chatStore.consent = useForTraining
    await chatStore.updateUserProfile()

    saveSuccess.value = true;

    // 3秒後隱藏成功訊息
    setTimeout(() => {
      saveSuccess.value = false;
    }, 3000);
  } catch (error) {
    console.error('儲存資料失敗', error);
  } finally {
    isSaving.value = false;
  }
};


// 關閉彈窗
const closePopup = () => {
  chatStore.chatWindow = false;
  chatStore.senseWindow = false;
  chatStore.testWindow = false;
  chatStore.profileWindow = false;
};

</script>

<template>
  <!-- 背景遮罩 -->
  <div v-if="chatStore.chatWindow || chatStore.senseWindow || chatStore.testWindow"
    class="fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm z-40 transition-opacity duration-300"
    :class="isAnimating ? 'opacity-0' : 'opacity-100'" @click="closePopup"></div>

  <!-- AI 分析彈窗 -->
  <transition name="modal">
    <div v-if="chatStore.chatWindow"
      class="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-50 w-full max-w-lg">
      <div class="bg-white rounded-xl shadow-2xl overflow-hidden">
        <!-- 彈窗頭部 -->
        <div class="bg-gradient-to-r from-blue-600 to-purple-600 px-6 py-4 flex justify-between items-center">
          <div class="flex items-center">
            <div class="w-10 h-10 bg-white bg-opacity-20 rounded-full flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <h3 class="ml-3 text-xl font-bold text-white">AI 健康建議</h3>
          </div>
          <button @click="closePopup" class="text-white hover:text-gray-200 transition-colors duration-200">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
              stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- 彈窗內容 -->
        <div class="p-6">
          <div class="flex justify-center mb-6">
            <button @click="chatStore.fetchChatContent"
              class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 shadow-lg transform transition-all duration-200 hover:scale-105">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
              </svg>
              重新分析血糖數據
            </button>
          </div>

          <div
            class="bg-gray-50 border border-gray-200 rounded-lg p-4 h-64 overflow-y-auto prose prose-blue max-w-none">
            <div
              v-if="chatStore.chatContent && chatStore.chatContent.response && chatStore.chatContent.response.message"
              class="transition-opacity duration-300"
              :class="{ 'opacity-0': chatStore.fetchingChat, 'opacity-100': !chatStore.fetchingChat }">
              <p v-html="chatStore.chatContent.response.message.content.replace(/\n/g, '<br>')"></p>
            </div>
            <div v-else class="flex flex-col items-center justify-center h-full">
              <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mb-4"></div>
              <p class="text-gray-500">AI 正在分析您的血糖數據...</p>
            </div>
          </div>
        </div>

        <!-- 彈窗底部 -->
        <div class="bg-gray-50 px-6 py-3 flex justify-end">
          <button @click="closePopup"
            class="inline-flex justify-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
            關閉
          </button>
        </div>
      </div>
    </div>
  </transition>

  <!-- 食物碳水化合物偵測結果 -->
  <transition name="modal">
    <div v-if="chatStore.senseWindow"
      class="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-50 w-full max-w-lg h-fit">
      <div class="bg-white rounded-xl shadow-2xl overflow-hidden">
        <!-- 彈窗頭部 -->
        <div class="bg-gradient-to-r from-green-600 to-teal-600 px-6 py-4 flex justify-between items-center">
          <div class="flex items-center">
            <div class="w-10 h-10 bg-white bg-opacity-20 rounded-full flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
              </svg>
            </div>
            <h3 class="ml-3 text-xl font-bold text-white">食物碳水分析結果</h3>
          </div>
          <button @click="closePopup" class="text-white hover:text-gray-200 transition-colors duration-200">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
              stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- 彈窗內容 -->
        <div class="p-6">
          <div class="flex flex-col items-center mb-6">
            <div class="w-16 h-16 mb-4 bg-green-100 rounded-full flex items-center justify-center">
              <span class="text-2xl font-bold text-green-600">{{ chatStore.gram }}</span>
            </div>
            <p class="text-center text-lg font-medium text-gray-800">
              預測該食物中的碳水化合物含量是 <span class="font-bold text-green-600">{{ chatStore.gram }}</span> 克
            </p>
          </div>

          <div class="mb-6 rounded-lg overflow-hidden shadow-md">
            <img :src="chatStore.imgURL" alt="食物圖片" class="w-full max-h-72 object-cover" />
          </div>

        </div>

        <!-- 彈窗底部 -->
        <div class="bg-gray-50 px-6 py-3 flex justify-end">
          <button @click="closePopup"
            class="inline-flex justify-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500">
            關閉
          </button>
        </div>
      </div>
    </div>
  </transition>

  <!-- 糖尿病測試表單 -->
  <transition name="modal">
    <div v-if="chatStore.testWindow"
      class="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-50 w-full max-w-4xl">
      <div class="bg-white rounded-xl shadow-2xl overflow-hidden">
        <!-- 彈窗頭部 -->
        <div class="bg-gradient-to-r from-blue-600 to-cyan-600 px-6 py-4 flex justify-between items-center">
          <div class="flex items-center">
            <div class="w-10 h-10 bg-white bg-opacity-20 rounded-full flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
              </svg>
            </div>
            <h3 class="ml-3 text-xl font-bold text-white">糖尿病風險評估</h3>
          </div>
          <button @click="closePopup" class="text-white hover:text-gray-200 transition-colors duration-200">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
              stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- 彈窗內容 -->
        <div class="p-6">
          <p class="text-gray-600 mb-6">填寫以下信息以評估您的糖尿病風險。此評估僅供參考，不能替代專業醫療診斷。</p>

          <form class="grid grid-cols-1 md:grid-cols-2 gap-6" @submit.prevent="chatStore.submitTest(); closePopup()">
            <!-- 左側列 -->
            <div class="space-y-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">性別</label>
                <div class="flex space-x-4">
                  <label class="inline-flex items-center">
                    <input type="radio" v-model="chatStore.healthData.gender" value="male"
                      class="form-radio h-4 w-4 text-blue-600 focus:ring-blue-500">
                    <span class="ml-2 text-gray-700">男性</span>
                  </label>
                  <label class="inline-flex items-center">
                    <input type="radio" v-model="chatStore.healthData.gender" value="female"
                      class="form-radio h-4 w-4 text-blue-600 focus:ring-blue-500">
                    <span class="ml-2 text-gray-700">女性</span>
                  </label>
                </div>
              </div>

              <div>
                <label for="age" class="block text-sm font-medium text-gray-700 mb-2">年齡</label>
                <div class="relative rounded-md shadow-sm">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none"
                      viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M21 15.546c-.523 0-1.046.151-1.5.454a2.704 2.704 0 01-3 0 2.704 2.704 0 00-3 0 2.704 2.704 0 01-3 0 2.704 2.704 0 00-3 0 2.704 2.704 0 01-3 0 2.701 2.701 0 00-1.5-.454M9 6v2m3-2v2m3-2v2" />
                    </svg>
                  </div>
                  <input type="number" id="age" v-model="chatStore.healthData.age"
                    class="pl-10 block w-full shadow-sm border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500">
                </div>
              </div>

              <div>
                <label for="hypertension" class="block text-sm font-medium text-gray-700 mb-2">血壓指數</label>
                <div class="relative rounded-md shadow-sm">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none"
                      viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M13 10V3L4 14h7v7l9-11h-7z" />
                    </svg>
                  </div>
                  <input type="number" id="hypertension" v-model="chatStore.healthData.hypertension" step="0.1"
                    class="pl-10 block w-full shadow-sm border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500">
                </div>
              </div>

              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">心臟疾病</label>
                <div class="flex space-x-4">
                  <label class="inline-flex items-center">
                    <input type="radio" v-model="chatStore.healthData.heart_disease" :value="1"
                      class="form-radio h-4 w-4 text-blue-600 focus:ring-blue-500">
                    <span class="ml-2 text-gray-700">是</span>
                  </label>
                  <label class="inline-flex items-center">
                    <input type="radio" v-model="chatStore.healthData.heart_disease" :value="0"
                      class="form-radio h-4 w-4 text-blue-600 focus:ring-blue-500">
                    <span class="ml-2 text-gray-700">否</span>
                  </label>
                </div>
              </div>
            </div>

            <!-- 右側列 -->
            <div class="space-y-6">
              <div>
                <label for="smoking" class="block text-sm font-medium text-gray-700 mb-2">吸菸史</label>
                <div class="relative rounded-md shadow-sm">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none"
                      viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
                    </svg>
                  </div>
                  <select id="smoking" v-model="chatStore.healthData.smoking_history"
                    class="pl-10 block w-full shadow-sm border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500">
                    <option value="never">從未吸菸</option>
                    <option value="former">曾經吸菸</option>
                    <option value="not current">目前不吸菸</option>
                    <option value="current">目前有吸菸</option>
                  </select>
                </div>
              </div>

              <div>
                <label for="bmi" class="block text-sm font-medium text-gray-700 mb-2">BMI 指數</label>
                <div class="relative rounded-md shadow-sm">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none"
                      viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M9 19l3 3m0 0l3-3m-3 3V10" />
                    </svg>
                  </div>
                  <input type="number" id="bmi" v-model="chatStore.healthData.bmi" step="0.1"
                    class="pl-10 block w-full shadow-sm border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500">
                </div>
              </div>

              <div>
                <label for="hba1c" class="block text-sm font-medium text-gray-700 mb-2">HbA1c 數值</label>
                <div class="relative rounded-md shadow-sm">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none"
                      viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                    </svg>
                  </div>
                  <input type="number" id="hba1c" v-model="chatStore.healthData.HbA1c_level" step="0.1"
                    class="pl-10 block w-full shadow-sm border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500">
                </div>
              </div>

              <div>
                <label for="bloodsugar" class="block text-sm font-medium text-gray-700 mb-2">血糖值</label>
                <div class="relative rounded-md shadow-sm">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none"
                      viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
                    </svg>
                  </div>
                  <input type="number" id="bloodsugar" v-model="chatStore.healthData.blood_glucose_level"
                    class="pl-10 block w-full shadow-sm border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500">
                </div>
              </div>
            </div>

            <!-- 提交按鈕 -->
            <div class="mt-6 flex justify-end col-span-1 md:col-span-2">
              <button type="button" @click="closePopup"
                class="mr-3 inline-flex justify-center py-2 px-4 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                取消
              </button>
              <button type="submit"
                class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                提交評估
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </transition>
  <transition name="modal">
    <div v-if="chatStore.profileWindow"
      class="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-50 w-full max-w-md">
      <div class="bg-white rounded-xl shadow-2xl overflow-hidden">
        <!-- 彈窗頭部 -->
        <div class="bg-gradient-to-r from-teal-500 to-cyan-500 px-6 py-4 flex justify-between items-center">
          <div class="flex items-center">
            <div class="w-10 h-10 bg-white bg-opacity-20 rounded-full flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24"
                stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <h3 class="ml-3 text-xl font-bold text-white">個人基本資料設定</h3>
          </div>
          <button @click="closePopup" class="text-white hover:text-gray-200 transition-colors duration-200">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24"
              stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- 彈窗內容 -->
        <div class="p-3 bg-gray-50">
          <div class="space-y-6">
            <div class="relative">
              <input type="number" id="height" v-model="height" required
                class="w-full bg-white text-gray-800 border-b-2 border-teal-500 py-3 px-4 focus:outline-none rounded-md" />
              <label for="height" class="absolute text-teal-500 duration-300"
                :class="{ 'top-[-20px] left-2 text-xs': height, 'top-3 left-4': !height }">
                身高 (cm)
              </label>
            </div>

            <div class="relative">
              <input type="number" id="weight" v-model="weight" required
                class="w-full bg-white text-gray-800 border-b-2 border-teal-500 py-3 px-4 focus:outline-none rounded-md" />
              <label for="weight" class="absolute text-teal-500 duration-300"
                :class="{ 'top-[-20px] left-2 text-xs': weight, 'top-3 left-4': !weight }">
                體重 (kg)
              </label>
            </div>

            <div class="relative">
              <input type="number" id="user-age" v-model="age" required
                class="w-full bg-white text-gray-800 border-b-2 border-teal-500 py-3 px-4 focus:outline-none rounded-md" />
              <label for="user-age" class="absolute text-teal-500 duration-300"
                :class="{ 'top-[-20px] left-2 text-xs': age, 'top-3 left-4': !age }">
                年齡
              </label>
            </div>
            <ModelTrainingProgress />
            <div v-if="saveSuccess" class="bg-green-100 text-green-800 px-4 py-3 rounded-md text-center">
              基本資料已成功儲存！
            </div>

            <div class="flex justify-center flex-col pt-2">
                <div class="flex items-center px-6 mt-2 py-3 bg-gray-100 rounded-md">
                <input type="checkbox" id="save-profile-checkbox" v-model="useForTraining"
                  class="form-checkbox h-5 w-5 text-teal-500 rounded focus:ring-teal-500 focus:ring-2" />
                <label for="save-profile-checkbox" class="ml-2 text-gray-700">
                  使用資料進行血糖預測訓練
                </label>
                </div>
              <button @click="saveUserData" :disabled="isSaving"
                class="px-6 py-3 mx-2 bg-gradient-to-r from-teal-500 to-cyan-500 text-white rounded-md shadow-md hover:from-teal-600 hover:to-cyan-600 transition-all duration-300 focus:outline-none transform hover:scale-105"
                :class="{ 'opacity-70 cursor-not-allowed': isSaving }">
                <span v-if="isSaving" class="flex items-center">
                  <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none"
                    viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                    </path>
                  </svg>
                  儲存中...
                </span>
                <span v-else>儲存基本資料</span>
              </button>

            </div>
          </div>
        </div>

        <!-- 彈窗底部 -->
        <div class="bg-gray-50 px-6 pb-2 flex justify-end">
          <button @click="closePopup"
            class="inline-flex justify-center px-4 py-4 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
            關閉
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.9);
}

.modal-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.9);
}

/* 確保背景正確覆蓋整個視窗 */
.backdrop-blur-sm {
  backdrop-filter: blur(4px);
}

input[type=number]::-webkit-inner-spin-button,
input[type=number]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type=number] {
  -moz-appearance: textfield;
}
</style>