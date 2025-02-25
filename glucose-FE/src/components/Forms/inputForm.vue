<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';
import { useDataStore } from '@/stores/dataStore';
const authStore = useAuthStore();
const formRef = ref(null);
const dataStore = useDataStore();
// Data properties for form inputs
const glucose = ref('');
const carbs = ref('');
const exercise = ref('');
const insulin_dose = ref('');

// Refs to track focus state for each input
const glucoseFocused = ref(false);
const carbsFocused = ref(false);
const exerciseFocused = ref(false);
const insulinDoseFocused = ref(false);

const handleSubmit = async (event) => {
    event.preventDefault();
        dataStore.postGlucose({
            blood_glucose: glucose.value,
            carbohydrate_intake: carbs.value,
            exercise_duration: exercise.value,
            insulin_dose: insulin_dose.value
        });
};
</script>

<template>
    <div class="w-96 p-10 bg-gray-700 shadow-lg rounded-lg relative">
        <h2 class="text-center text-white text-2xl mb-8">紀錄資料</h2>
        <p v-if="authStore.username" class="text-center text-slate-300 text-l mb-8">嗨， {{ authStore.username }}</p>
        <form ref="formRef" @submit="handleSubmit">
            <div class="relative mb-8">
                <input type="text" v-model="glucose" required @focus="glucoseFocused = true"
                    @blur="glucoseFocused = false"
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label :class="{ 'focused': glucoseFocused || glucose }"
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">血糖值</label>
            </div>
            <div class="relative mb-8">
                <input type="text" v-model="carbs" @focus="carbsFocused = true" @blur="carbsFocused = false"
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label :class="{ 'focused': carbsFocused || carbs }"
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">碳水化合物攝取量(克)</label>
            </div>
            <div class="relative mb-8">
                <input type="text" v-model="exercise" @focus="exerciseFocused = true"
                    @blur="exerciseFocused = false"
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label :class="{ 'focused': exerciseFocused || exercise }"
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">運動時長(分鐘)</label>
            </div>
            <div class="relative mb-8">
                <input type="text" v-model="insulin_dose" @focus="insulinDoseFocused = true"
                    @blur="insulinDoseFocused = false"
                    class="w-full p-2 text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
                <label :class="{ 'focused': insulinDoseFocused || insulin_dose }"
                    class="absolute top-0 left-0 p-2 text-white transition-all duration-500 pointer-events-none">胰島素劑量</label>
            </div>
            <button type="submit"
                class="w-full p-2 mt-4 text-slate-800 bg-slate-200 rounded-lg hover:bg-slate-300 focus:outline-none focus:ring-2 focus:ring-slate-400">
                送出
            </button>
        </form>
    </div>
</template>

<style scoped>
label {
    position: absolute;
    top: 0;
    left: 0;
    padding: 0.5rem;
    color: white;
    transition: all 0.5s ease;
    pointer-events: none;
}

input:focus,
input:not(:placeholder-shown) {
    border-color: #03e9f4;
}

label.focused {
    top: -20px;
    left: 0;
    color: #03e9f4;
    font-size: 12px;
}
</style>