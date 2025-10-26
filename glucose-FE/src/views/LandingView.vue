<script setup>
import NavBar from '@/components/NavBar.vue';
</script>

<template>
  <NavBar class="sticky top-0 z-50" />
  <div class="hero w-full h-screen flex flex-row">
    <div class="justify-center items-center flex flex-col flex-1 text-5xl font-bold">
      <p>DIA<span class=" text-slate-800/60">BEAT</span></p>
      <div class="text-3xl font-normal mt-8">打敗糖：人工智慧血糖管理工具</div>
    </div>
    <div class="relative flex flex-1 justify-center items-center">
      <img src="../assets/ggimg.png" />
      <div class="absolute inset-0 bg-black/60"></div>
    </div>    
  </div>
</template>