<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axiosInstance from '../axiosInstance';

const formRef = ref(null);

// Data properties for form inputs
const age = ref('');
const height = ref('');
const weight = ref('');
const handleSubmit = async (event) => {
    event.preventDefault();
    try {
        const response = await axiosInstance.post('/api/setup/', {
            age: age.value,
            height: height.value,
            weight: weight.value
        });
        console.log(response.data);
    } catch (error) {
        console.error(error);
    }
};
</script>

<template>
    <div class="w-96 p-10 bg-gray-700 shadow-lg rounded-lg relative">
        <h2 class="text-center text-white text-2xl mb-1">基本資料</h2>
    <p class="text-center text-slate-300 text-l mb-8">更精準的預測你的血糖狀況</p>
        <form ref="formRef" @submit="handleSubmit">
            <div class="relative mb-8">
                <input type="text" v-model="age" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">年齡</label>
            </div>
            <div class="relative mb-8">
                <input type="text" v-model="height" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">身高(公分)</label>
            </div>
            <div class="relative mb-8">
                <input type="text" v-model="weight" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">體重(公斤)</label>
            </div>
            <button type="submit" class="w-full p-2 mt-4 text-slate-800 bg-slate-200 rounded-lg hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-slate-400">
                送出
            </button>
        </form>
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
</style>
