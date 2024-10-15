<script setup>
import { ref } from 'vue';

const formRef = ref(null);

const handleSubmit = async (event) => {
    event.preventDefault();
    const formData = new FormData(formRef.value);
    const data = Object.fromEntries(formData.entries());

    try {
        const response = await fetch('http://localhost:8080/submit', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const result = await response.json();
        console.log('Success:', result);
    } catch (error) {
        console.error('Error:', error);
    }
};
</script>

<template>
    <div class="w-96 p-10 bg-gray-700 shadow-lg rounded-lg relative">
        <h2 class="text-center text-white text-2xl mb-8">血糖值</h2>
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