<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useDataStore } from '@/stores/dataStore';

const authStore = useAuthStore();
const formRef = ref(null);
const dataStore = useDataStore();

// 表單輸入數據
const glucose = ref('');
const carbs = ref('');
const exercise = ref('');
const insulin_dose = ref('');

// 追蹤每個輸入的焦點狀態
const glucoseFocused = ref(false);
const carbsFocused = ref(false);
const exerciseFocused = ref(false);
const insulinDoseFocused = ref(false);

// 錯誤提示信息
const glucoseError = ref('');

// 驗證血糖值是否有效
const validateGlucose = () => {
    if (glucose.value < 0) {
        glucoseError.value = '血糖值不能小於0';
        return false;
    }
    glucoseError.value = '';
    return true;
};

// 提交表單
const handleSubmit = async (event) => {
    event.preventDefault();
    
    // 驗證血糖值
    if (!validateGlucose()) {
        return;
    }
    
    dataStore.postGlucose({
        blood_glucose: glucose.value,
        carbohydrate_intake: carbs.value,
        exercise_duration: exercise.value,
        insulin_dose: insulin_dose.value
    });
};

// 添加時間選擇
const recordTime = ref(new Date().toISOString().substr(0, 10));
</script>

<template>
    <div class="w-full max-w-md p-8 bg-white rounded-xl shadow-xl transform transition-all hover:scale-102 duration-300">
        <div class="flex items-center justify-center mb-6">
            <div class="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
            </div>
            <h2 class="ml-3 text-2xl font-bold text-gray-800">血糖數據紀錄</h2>
        </div>
        
        <div v-if="authStore.username" class="mb-6 flex items-center justify-center">
            <span class="inline-flex items-center px-4 py-2 rounded-full text-sm font-medium bg-teal-100 text-teal-800">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                歡迎回來，{{ authStore.username }}
            </span>
        </div>

        <form ref="formRef" @submit="handleSubmit" class="space-y-6">
            <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                    </svg>
                </div>
                <input type="number" v-model="glucose" required min="0" @focus="glucoseFocused = true" @blur="glucoseFocused = false; validateGlucose()"
                    class="block w-full pl-10 pr-3 py-3 border-b-2 border-gray-300 text-gray-900 focus:border-blue-500 focus:outline-none rounded-md transition-all duration-200"
                    placeholder="">
                <label :class="{ 'transform -translate-y-5 scale-75 text-blue-500': glucoseFocused || glucose }"
                    class="absolute top-3 left-10 text-gray-500 transform origin-left transition-all duration-200 pointer-events-none">
                    血糖值 (mg/dL)
                </label>
                <div v-if="glucoseError" class="text-red-500 text-xs mt-1">{{ glucoseError }}</div>
            </div>

            <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-orange-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                    </svg>
                </div>
                <input type="number" v-model="carbs" @focus="carbsFocused = true" @blur="carbsFocused = false"
                    class="block w-full pl-10 pr-3 py-3 border-b-2 border-gray-300 text-gray-900 focus:border-blue-500 focus:outline-none rounded-md transition-all duration-200"
                    placeholder="">
                <label :class="{ 'transform -translate-y-5 scale-75 text-blue-500': carbsFocused || carbs }"
                    class="absolute top-3 left-10 text-gray-500 transform origin-left transition-all duration-200 pointer-events-none">
                    碳水化合物攝取量 (克)
                </label>
            </div>

            <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                </div>
                <input type="number" v-model="exercise" @focus="exerciseFocused = true" @blur="exerciseFocused = false"
                    class="block w-full pl-10 pr-3 py-3 border-b-2 border-gray-300 text-gray-900 focus:border-blue-500 focus:outline-none rounded-md transition-all duration-200"
                    placeholder="">
                <label :class="{ 'transform -translate-y-5 scale-75 text-blue-500': exerciseFocused || exercise }"
                    class="absolute top-3 left-10 text-gray-500 transform origin-left transition-all duration-200 pointer-events-none">
                    運動時長 (分鐘)
                </label>
            </div>

            <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
                    </svg>
                </div>
                <input type="number" v-model="insulin_dose" @focus="insulinDoseFocused = true" @blur="insulinDoseFocused = false"
                    class="block w-full pl-10 pr-3 py-3 border-b-2 border-gray-300 text-gray-900 focus:border-blue-500 focus:outline-none rounded-md transition-all duration-200"
                    placeholder="">
                <label :class="{ 'transform -translate-y-5 scale-75 text-blue-500': insulinDoseFocused || insulin_dose }"
                    class="absolute top-3 left-10 text-gray-500 transform origin-left transition-all duration-200 pointer-events-none">
                    胰島素劑量 (單位)
                </label>
            </div>

            <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                </div>
                <input type="date" v-model="recordTime"
                    class="block w-full pl-10 pr-3 py-3 border-b-2 border-gray-300 text-gray-900 focus:border-blue-500 focus:outline-none rounded-md transition-all duration-200">
                <label class="absolute top-3 left-10 text-gray-500 transform origin-left transition-all duration-200 pointer-events-none transform -translate-y-5 scale-75 text-blue-500">
                    紀錄日期
                </label>
            </div>

            <button type="submit"
                class="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-gradient-to-r from-blue-500 to-teal-400 hover:from-blue-600 hover:to-teal-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transform transition-all duration-200 hover:scale-105">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" />
                </svg>
                提交數據
            </button>
            
            <div class="text-center mt-4">
                <p class="text-xs text-gray-500">
                    數據將安全存儲並用於分析您的血糖趨勢和健康狀況
                </p>
            </div>
        </form>
    </div>
</template>

<style scoped>
/* 輸入框聚焦效果 */
input:focus {
    box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

/* 懸停效果 */
.hover\:scale-102:hover {
    --tw-scale-x: 1.02;
    --tw-scale-y: 1.02;
    transform: var(--tw-transform);
}
</style>