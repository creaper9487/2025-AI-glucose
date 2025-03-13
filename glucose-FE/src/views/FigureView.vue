<script setup>
import chatbot from '@/components/pop.vue';
import NavBar from '@/components/NavBar.vue';
import lineChart from '@/components/charts/lineChart.vue';
import { useDataStore } from '@/stores/dataStore';
import { useChatStore } from '@/stores/chatStore';

const dataStore = useDataStore();
const chatStore = useChatStore();
dataStore.fetchGlucose();

const handlePrediction = () => {
  // chatStore.profileWindow = true;
};
</script>

<template>
  <div class="bg-gray-800 min-h-screen flex flex-col">
    <NavBar class="fixed w-full top-0 z-10"></NavBar>
    <div class="flex-grow mt-16 px-5 py-4 flex flex-col w-full mx-auto">
      <div class="w-full flex justify-between items-center mb-4">
        <p class="px-5 text-2xl text-slate-200 hover:bg-slate-100 hover:text-slate-800 hover:cursor-default transition-colors duration-300">血糖軌跡紀錄</p>
        <button 
          @click="handlePrediction" 
          class="px-5 py-2 bg-teal-500 text-white rounded-md hover:bg-teal-600 transition-colors duration-300 flex items-center"
        >
          <span class="mr-2">✨ AI 血糖預測</span>
        </button>
      </div>
      <div class="flex-grow w-full h-full">
        <lineChart class="w-full h-full" />
      </div>
    </div>
  </div>
  <chatbot class="m-2" />
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