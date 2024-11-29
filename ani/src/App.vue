<script setup>
import inputForm from '@/components/inputForm.vue';
import { ref } from 'vue';
const animationEnded = ref(false);
const animationGrow = ref(false);
const handleAnimationEnd = () => {
  animationEnded.value = true;
  console.log('Animation ended');
};
const handleGrow = () => {
  animationGrow.value = true;
  console.log('Grow animation ended');
};
</script>

<template>
  <div class="h-screen flex items-center justify-center flex-row bg-greenScreen">
    <inputForm class="transition-all duration-1000 transform opacity-0 scale-95 animate-slide-drop rounded-lg shadow-lg"
      @animationend="handleAnimationEnd" v-if="!animationEnded" />
    <inputForm v-else class=""></inputForm>
    <div class="flex flex-col m-5 justify-start">
      <p v-if="animationEnded" class="text-white font-electro text-6xl  animate-fly-in">track your glucose</p>
      <p v-if="animationEnded" @animationend="handleGrow" class="text-white font-electro mt-3 animate-fly-in" :class="{ 'animate-grow':animationGrow }">control your glucose</p>
    </div>
  </div>
</template> 

<style scoped>
@keyframes slide-drop {
  0% {
    opacity: 0;
    transform: scale(0.8) translateX(0);
    /* Start smaller and centered */
  }

  100% {
    opacity: 1;
    transform: scale(1) translateX(-252px);
    /* Final left position */
  }
}

.animate-slide-drop {
  animation: slide-drop 1.5s cubic-bezier(0.25, 0.1, 0.25, 1) forwards;
}
@keyframes fly-in {
  0% {
    transform: translateX(100%);
    opacity: 0;
  }
  80% {
    transform: translateX(-10%);
    opacity: 1;
  }
  100% {
    transform: translateX(0);
    opacity: 1;
  }
}

.animate-fly-in {
  animation: fly-in 1s cubic-bezier(0.25, 1, 0.5, 1) forwards;
}
@keyframes grow {
  0% {
    transform: scale(1) translateX(0);
    color: white;
  }
  100% {
    transform: scale(3) translateX(0);
    transform-origin: left center;
    color: red;
  }
}

.animate-grow {
  animation: grow 0.5s ease-out forwards;
  animation-delay: .1s; /* Start growing after the fly-in finishes */
}
</style>