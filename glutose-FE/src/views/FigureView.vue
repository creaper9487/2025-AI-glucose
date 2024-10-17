<script setup>
import NavBar from '@/components/NavBar.vue';
import lineChart from '@/components/charts/lineChart.vue';
import inputForm from '@/components/inputForm.vue';
</script>

<template>
  <div class="bg-gray-800">
    <NavBar class="fixed w-screen top-0"></NavBar>
    <div class="mt-16 w-4/5 h-4/5 px-5 flex items-center self-center flex-col mx-auto">
      <p class="mt-3 px-5 text-2xl text-slate-200 hover:bg-slate-100 hover:text-slate-800 hover:cursor-default transition-colors duration-300">血糖軌跡紀錄</p>
      <lineChart class="w-full h-full" />
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
</style>
