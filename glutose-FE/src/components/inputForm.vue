<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axiosInstance from './axiosInstance';

const router = useRouter();
let currentPathObject = router.currentRoute.value;

const formRef = ref(null);

// Data properties for form inputs
const glucose = ref('');
const carbs = ref('');
const exercise = ref('');
const insulin = ref('');
const username = ref('');
const password = ref('');
const email = ref('');

const handleSubmit = async (event) => {
    event.preventDefault();
    try {
        const response = await axiosInstance.post('/api/submit/', {
            glucose: glucose.value,
            carbs: carbs.value,
            exercise: exercise.value,
            insulin: insulin.value
        });
        console.log(response.data);
    } catch (error) {
        console.error(error);
    }
};

const handleRegister = async (event) => {
    event.preventDefault();
    try {
        const response = await axiosInstance.post('/api/register/', {
            username: username.value,
            password: password.value,
            email: email.value
        });
        console.log(response.data);
    } catch (error) {
        console.error(error);
    }
};
</script>

<template>
    <div v-if="(currentPathObject.path == '/')" class="w-96 p-10 bg-gray-700 shadow-lg rounded-lg relative">
        <h2 class="text-center text-white text-2xl mb-8">紀錄資料</h2>
        <form ref="formRef" @submit="handleSubmit">
            <div class="relative mb-8">
                <input type="text" v-model="glucose" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">血糖值</label>
            </div>
            <div class="relative mb-8">
                <input type="text" v-model="carbs" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">碳水化合物攝取量(克)</label>
            </div>
            <div class="relative mb-8">
                <input type="text" v-model="exercise" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">運動時長(分鐘)</label>
            </div>
            <div class="relative mb-8">
                <input type="text" v-model="insulin" required
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
        <h2 class="text-center text-white text-2xl mb-1">註冊</h2>
        <p class="text-center text-slate-300 text-l mb-8">註冊帳號以紀錄血糖資料並取得AI建議
        </p>
        <form ref="formRef" @submit="handleRegister">
            <div class="relative mb-8">
                <input type="text" v-model="username" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">帳號</label>
            </div>
            <div class="relative mb-8">
                <input type="password" v-model="password" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">密碼</label>
            </div>
            <div class="relative mb-8">
                <input type="text" v-model="email" required
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">email</label>
            </div>
            <button type="submit" class="w-full p-2 mt-4 text-slate-800 bg-slate-200 rounded-lg hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-slate-400">
                註冊
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