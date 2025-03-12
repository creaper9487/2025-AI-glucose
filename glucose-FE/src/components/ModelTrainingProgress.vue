<script setup>
import { ref, onMounted, computed } from 'vue';
import axiosInstance from './axiosInstance';

const comparisonCount = ref(0);
const isLoading = ref(true);
const error = ref(null);

// 計算進度百分比
const progressPercentage = computed(() => {
    // 如果比例超過100%，仍然顯示實際的進度
    return Math.round((comparisonCount.value / 20) * 100);
});

// 計算顏色，根據進度變化
const progressColor = computed(() => {
    if (progressPercentage.value < 30) return 'bg-red-500';
    if (progressPercentage.value < 70) return 'bg-yellow-500';
    return 'bg-green-500';
});

// 是否達到訓練條件
const isReady = computed(() => {
    return comparisonCount.value >= 20;
});

// 獲取用戶的訓練資料進度
const fetchTrainingProgress = async () => {
    isLoading.value = true;
    try {
        const response = await axiosInstance.get('/api/model/train/');
        comparisonCount.value = response.data.comparison_count || 0;
    } catch (err) {
        console.error('獲取訓練進度時發生錯誤:', err);
        error.value = '無法獲取訓練進度';
    } finally {
        isLoading.value = false;
    }
};

onMounted(fetchTrainingProgress);
</script>

<template>
    <div class="w-full mb-10">
        <h3 class="text-white text-lg mb-2">模型訓練進度</h3>
        
        <div v-if="isLoading" class="text-slate-300 text-sm mb-2">
            正在載入進度...
        </div>
        
        <div v-else-if="error" class="text-red-300 text-sm mb-2">
            {{ error }}
        </div>
        
        <div v-else>
            <!-- 進度說明 -->
            <div class="flex justify-between text-sm text-slate-300 mb-1">
                <span>收集資料: {{ comparisonCount }}/20</span>
                <span>{{ progressPercentage }}%</span>
            </div>
            
            <!-- 進度條 -->
            <div class="w-full bg-gray-600 rounded-full h-4 mb-2">
                <div :class="['h-4 rounded-full', progressColor]" :style="{ width: `${Math.min(progressPercentage, 100)}%` }"></div>
            </div>
            
            <!-- 進度說明文字 -->
            <p class="text-sm text-slate-300" v-if="!isReady">
                需要至少20筆資料才能開始訓練個人化血糖預測模型
            </p>
            <p class="text-sm text-green-300" v-else>
                已收集足夠資料，可以開始訓練您的個人化血糖預測模型！
            </p>
        </div>
    </div>
</template>