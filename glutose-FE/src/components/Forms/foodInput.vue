<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';
const authStore = useAuthStore();
const imageFile = ref(null);

const handleFileChange = (event) => {
    imageFile.value = event.target.files[0];
};

const sendImage = async () => {
    if (!imageFile.value) return;

    const formData = new FormData();
    formData.append('image', imageFile.value);

    try {
        const response = await axios.post('/api/sense', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        console.log(response.data);
    } catch (error) {
        console.error('Error uploading image:', error);
    }
};
const sendAnalysisRequest = async () => {
    try {
        const response = await axios.post('/api/analyse');
        console.log(response.data);
    } catch (error) {
        console.error('Error requesting analysis:', error);
    }
};
</script>
<template>
    <div class="flex flex-col">
        <div class="w-96 p-10 bg-gray-700 shadow-lg rounded-lg relative">
            <h2 class="text-center text-white text-xl mb-4">上傳圖片偵測碳水化合物</h2>
            <input type="file" @change="handleFileChange" accept="image/*" />
            <button @click="sendImage"
                class="w-full p-2 mt-4 text-slate-800 bg-slate-200 rounded-lg hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-slate-400">Send
                Image</button>
        </div>
        <div class="w-96 p-10 bg-gray-700 shadow-lg rounded-lg mt-4 relative">
            <h2 class="text-center text-white text-xl mb-4">✨針對血糖資料產生 AI 建議</h2>
            <button
                    class="w-full p-2 mt-4 text-slate-800 bg-slate-200 rounded-lg hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-slate-400 opacity-50"
                    v-if="!authStore.username"
                    >
                    登入後使用…
            </button>
            <button @click="sendAnalysisRequest"
                    class="w-full p-2 mt-4 text-slate-800 bg-slate-200 rounded-lg hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-slate-400"
                    v-if="authStore.username"
                    >
                    產生建議
            </button>

        </div>
    </div>
</template>
