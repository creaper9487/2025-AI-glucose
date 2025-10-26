<script setup>
import { ref, onMounted } from 'vue';
import NavBar from '@/components/NavBar.vue';
import ModelTrainingProgress from '@/components/ModelTrainingProgress.vue';
import { useDataStore } from '@/stores/dataStore';
const dataStore = useDataStore();
// 個人資料數據
const userData = ref({
    weight: dataStore.profile.weight || '',
    height: dataStore.profile.height || '',
    age: dataStore.profile.age || '',
    gender: '',
    diabetesType: '',
    activityLevel: '1'
});

// 保存結果消息
const saveMessage = ref('');
const showMessage = ref(false);


// 保存個人資料
const saveProfile = () => {
    // 這裡將來可以加入與後端API的連接來保存數據
    console.log('保存的使用者數據:', userData.value);
    dataStore.updateProfile(userData.value);
    // 顯示保存成功消息
    saveMessage.value = '個人資料已成功更新！';
    showMessage.value = true;

    // 3秒後隱藏消息
    setTimeout(() => {
        showMessage.value = false;
    }, 3000);
};

</script>

<template>
    <div class="min-h-screen bg-gray-800 text-white">
        <NavBar />

        <div class="pt-20 pb-12 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
            <h1
                class="text-3xl font-bold text-blue-400 mb-8">
                個人資料儀表板
            </h1>

            <!-- 主要內容區域 -->
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">

                <!-- 左側：個人資料表單 -->
                <div
                    class="col-span-2 bg-gray-800 bg-opacity-70 rounded-xl p-6 shadow-lg backdrop-blur-sm border border-gray-700">
                    <h2 class="text-xl font-semibold mb-6 flex items-center">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-blue-400" fill="none"
                            viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                        個人基本資料
                    </h2>

                    <!-- 保存成功消息 -->
                    <div v-if="showMessage"
                        class="mb-4 p-3 bg-green-500 bg-opacity-20 border border-green-500 rounded-lg text-green-400 flex items-center">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24"
                            stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                        </svg>
                        {{ saveMessage }}
                    </div>

                    <div class="grid md:grid-cols-2 gap-6">
                        <!-- 體重 -->
                        <div class="relative">
                            <label class="block text-gray-300 text-sm mb-2">體重 (公斤)</label>
                            <input v-model="userData.weight" type="number"
                                class="w-full px-4 py-2 bg-gray-700 rounded-lg border border-gray-600 text-white focus:outline-none focus:border-blue-500 transition-colors"
                                placeholder="請輸入體重" />
                        </div>

                        <!-- 身高 -->
                        <div class="relative">
                            <label class="block text-gray-300 text-sm mb-2">身高 (公分)</label>
                            <input v-model="userData.height" type="number"
                                class="w-full px-4 py-2 bg-gray-700 rounded-lg border border-gray-600 text-white focus:outline-none focus:border-blue-500 transition-colors"
                                placeholder="請輸入身高" />
                        </div>

                        <!-- 年齡 -->
                        <div class="relative">
                            <label class="block text-gray-300 text-sm mb-2">年齡</label>
                            <input v-model="userData.age" type="number"
                                class="w-full px-4 py-2 bg-gray-700 rounded-lg border border-gray-600 text-white focus:outline-none focus:border-blue-500 transition-colors"
                                placeholder="請輸入年齡" />
                        </div>
                    </div>

                    <!-- 保存按鈕 -->
                    <button @click="saveProfile"
                        class="mt-8 px-6 py-3 bg-blue-600 hover:bg-blue-700 rounded-lg shadow-lg hover:shadow-xl transform hover:-translate-y-1 transition-all duration-300 font-medium">
                        保存資料
                    </button>
                </div>

                <!-- 右側：儀表板卡片 -->
                <div class="col-span-1 space-y-6">
                    <!-- 模型訓練進度卡片 -->
                    <div
                        class="bg-gray-800 bg-opacity-70 rounded-xl p-6 shadow-lg backdrop-blur-sm border border-gray-700">
                        <h2 class="text-xl font-semibold mb-4 flex items-center">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 mr-2 text-blue-400" fill="none"
                                viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
                            </svg>
                            AI模型訓練進度
                        </h2>

                        <ModelTrainingProgress />

                        <p class="mt-4 text-sm text-gray-400">
                            系統正在根據您的數據訓練個人化血糖預測模型，以提供更準確的健康建議。
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
input:focus~label,
input:valid~label {
    top: -20px;
    left: 0;
    color: #03e9f4;
    font-size: 12px;
}

/* 輸入控件聚焦時的效果 */
input:focus,
select:focus {
    border-color: #03e9f4;
    box-shadow: 0 0 5px rgba(3, 233, 244, 0.5);
}

/* 數值輸入框的上下箭頭樣式 */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
    opacity: 1;
    background: #555;
    border-radius: 2px;
    height: 14px;
}
</style>