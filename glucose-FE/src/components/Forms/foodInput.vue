<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { useDataStore } from '@/stores/dataStore';
const authStore = useAuthStore();
const imageFile = ref(null);
const chatStore = useChatStore();
const dataStore = useDataStore();
const handleFileChange = (event) => {
    imageFile.value = event.target.files[0];
};

const sendAnalysisRequest = async () => {
    chatStore.chatWindow = true;
    console.log(chatStore.chatWindow);
    chatStore.fetchChatContent();
};
const sendSense = async () => {
    chatStore.img = imageFile.value;
    chatStore.SensePicture(imageFile.value);
    chatStore.senseWindow = true;
};
const doTest = async () => {
    chatStore.testWindow = true;
};
</script>
<template>
    <div class="flex flex-col">
        <div class="flex flex-row">
            <div class="bg-white rounded-xl shadow-xl p-6 transform transition-all hover:scale-102 duration-300 mx-6">
                <div class="flex items-center mb-4">
                    <div class="w-10 h-10 bg-green-500 rounded-full flex items-center justify-center">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none"
                            viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                        </svg>
                    </div>
                    <h2 class="ml-3 text-xl font-bold text-gray-800">分析食物碳水化合物</h2>
                </div>

                <div @dragover="handleDragOver" @dragleave="handleDragLeave" @drop="handleDrop"
                    :class="{ 'bg-green-50 border-green-300': isDragging }"
                    class="mt-2 border-2 border-dashed border-gray-300 rounded-lg p-4 text-center transition-colors duration-200">

                    <div v-if="!previewUrl" class="space-y-2">
                        <svg xmlns="http://www.w3.org/2000/svg" class="mx-auto h-12 w-12 text-gray-400" fill="none"
                            viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        <p class="text-sm text-gray-600">拖放圖片或點擊上傳</p>
                        <p class="text-xs text-gray-500">支持 JPG, PNG 格式</p>
                    </div>

                    <div v-else class="relative">
                        <img :src="previewUrl" alt="食物預覽" class="mx-auto max-h-48 rounded-lg" />
                        <button @click="previewUrl = ''; imageFile = null"
                            class="absolute top-2 right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600 transition-colors duration-200">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24"
                                stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>

                    <input type="file" @change="handleFileChange" id="foodImage" accept="image/*" class="hidden" />
                    <label for="foodImage"
                        class="mt-4 inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-500 hover:bg-green-600 cursor-pointer transition-colors duration-200">
                        選擇圖片
                    </label>
                </div>

                <button @click="sendSense" :disabled="!imageFile || isUploading"
                    :class="{ 'opacity-50 cursor-not-allowed': !imageFile || isUploading }"
                    class="mt-4 w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-500 hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition-all duration-200">
                    <svg v-if="isUploading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                        xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4">
                        </circle>
                        <path class="opacity-75" fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                        </path>
                    </svg>
                    <span>{{ isUploading ? '分析中...' : '分析碳水含量' }}</span>
                </button>
            </div>
            <div class="bg-white rounded-xl shadow-xl p-2 transform transition-all hover:scale-102 duration-300">
                <div class="flex items-center mb-4">
                    <div class="w-10 h-10 bg-purple-500 rounded-full flex items-center justify-center">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none"
                            viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
                        </svg>
                    </div>
                    <h2 class="ml-3 text-xl font-bold text-gray-800">✨ AI 健康建議</h2>
                </div>

                <p class="text-gray-600 text-sm mb-4">
                    基於血糖數據，讓 AI 提供個人化的健康建議。
                </p>

                <button @click="sendAnalysisRequest" 
                    :class="{ 'opacity-50 cursor-not-allowed': !authStore.username }"
                    class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-purple-500 hover:bg-purple-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-all duration-200">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24"
                        stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M13 10V3L4 14h7v7l9-11h-7z" />
                    </svg>
                    {{ authStore.username ? '生成 AI 建議':'登入後使用'  }}
                </button>
            </div>
        </div>
        <div class="bg-white rounded-xl ml-6 mt-6 w-2/3 shadow-xl p-3 transform transition-all hover:scale-102 duration-300">
            <div class="flex items-center mb-4">
                <div class="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                </div>
                <h2 class="ml-3 text-xl font-bold text-gray-800">✨ 糖尿病風險評估</h2>
            </div>
            
            <p class="text-gray-600 text-sm mb-4">
                通過填寫簡單的健康數據，評估您的糖尿病風險水平。
            </p>
            
            <button 
                @click="doTest"
                :class="{'opacity-50 cursor-not-allowed': !authStore.username}"
                class="w-1/2 flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-500 hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-200">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
                {{ authStore.username ? '開始評估':'登入後使用'  }}
            </button>
        </div>
    </div>
</template>
