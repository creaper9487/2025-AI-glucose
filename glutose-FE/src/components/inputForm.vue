<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router'

const router = useRouter()

let currentPathObject = router.currentRoute.value; 
const formRef = ref(null);

import axiosInstance from './axiosInstance';

const handleSubmit = async (event) => {
    event.preventDefault();
    const formData = new FormData(formRef.value);
    const data = Object.fromEntries(formData.entries());

    try {
        const response = await axiosInstance.post('http://localhost:8080/submit', data, {
            headers: {
                'Content-Type': 'application/json',
            },
        });

        console.log('Success:', response.data);
    } catch (error) {
        console.error('Error:', error);
    }
};
</script>

<template>
    <div v-if="(currentPathObject.path == '/')" class="w-96 p-10 bg-gray-700 shadow-lg rounded-lg relative">
        <h2 class="text-center text-white text-2xl mb-8">紀錄資料</h2>
        <form ref="formRef" @submit="handleSubmit">
            <div class="relative mb-8">
                <input type="text" name="glucose" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">血糖值</label>
            </div>
            <div class="relative mb-8">
                <input type="text" name="carbs" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">碳水化合物攝取量(克)</label>
            </div>
            <div class="relative mb-8">
                <input type="text" name="exercise" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">運動時長(分鐘)</label>
            </div>
            <div class="relative mb-8">
                <input type="text" name="insulin" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">胰島素注射量</label>
            </div>
            <button type="submit" class="w-full p-2 mt-4 text-slate-800 bg-slate-200 rounded-lg hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-slate-400">
                提交
            </button>
        </form>
    </div>
    <div v-if="(currentPathObject.path == '/login')" class="w-96 p-10 bg-gray-700 shadow-lg rounded-lg relative">
        <h2 class="text-center text-white text-2xl mb-1">登入</h2>
        <p class="text-center text-slate-300 text-l mb-8">登入以查詢歷史血糖資料並取得AI建議
        </p>
        <form ref="formRef" @submit="handleSubmit">
            <div class="relative mb-8">
                <input type="text" name="username_or_email" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">帳號</label>
            </div>
            <div class="relative mb-8">
                <input type="text" name="password" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">密碼</label>
            </div>
            <button type="submit" class="w-full p-2 mt-4 text-slate-800 bg-slate-200 rounded-lg hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-slate-400">
                登入
            </button>
        </form>
        <RouterLink to="/">
            <button class="w-full p-2 mt-4 text-slate-800 bg-slate-200 rounded-lg hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-slate-400">
            註冊
            </button>
        </RouterLink>
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