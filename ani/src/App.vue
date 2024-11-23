<script setup>
import inputForm from '@/components/inputForm.vue';
</script>

<template>
  <div class="h-screen flex items-center justify-center flex-col bg-gray-800">
    <inputForm 
      class="transition-all duration-1000 transform opacity-0 scale-95 animate-slide-drop rounded-lg shadow-lg" 
    />
    <p class="text-white font-electro text-6xl">track your glucose</p>
    <p class="text-white font-electro">control your glucose</p>
  </div>
</template>

<style scoped>
@keyframes slide-drop {
  0% {
    opacity: 0;
    transform: scale(0.8) translateX(0); /* Start smaller and centered */
  }

  100% {
    opacity: 1;
    transform: scale(1) translateX(-400px); /* Final left position */
  }
}

.animate-slide-drop {
  animation: slide-drop 1.5s cubic-bezier(0.25, 0.1, 0.25, 1) forwards;
}
</style>