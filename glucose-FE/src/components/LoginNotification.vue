<template>
    <transition name="notification-fade">
      <div v-if="show" class="fixed top-20 left-1/2 transform -translate-x-1/2 z-50 max-w-md w-full">
        <div class="bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg shadow-2xl overflow-hidden">
          <div class="px-6 py-4 flex items-center">
            <div class="w-10 h-10 bg-white bg-opacity-20 rounded-full flex items-center justify-center mr-4">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
            </div>
            <div class="flex-1">
              <h3 class="text-lg font-bold text-white">需要登入</h3>
              <p class="text-blue-100">請先登入後才能訪問該頁面</p>
            </div>
            <button @click="closeNotification" class="text-white hover:text-gray-200 transition-colors duration-200">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div class="w-full bg-blue-800 h-1">
            <div class="bg-white h-1 animate-shrink"></div>
          </div>
        </div>
      </div>
    </transition>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue';
  
  const props = defineProps({
    autoClose: {
      type: Boolean,
      default: true
    },
    duration: {
      type: Number,
      default: 5000
    },
    initialState: {
      type: Boolean,
      default: true
    }
  });
  
  const show = ref(props.initialState);
  let timer = null;
  
  const closeNotification = () => {
    show.value = false;
  };
  
  onMounted(() => {
    if (props.autoClose) {
      timer = setTimeout(() => {
        closeNotification();
      }, props.duration);
    }
  });
  </script>
  
  <style scoped>
  .notification-fade-enter-active,
  .notification-fade-leave-active {
    transition: all 0.5s ease;
  }
  
  .notification-fade-enter-from,
  .notification-fade-leave-to {
    opacity: 0;
    transform: translate(-50%, -20px);
  }
  
  @keyframes shrink {
    0% {
      width: 100%;
    }
    100% {
      width: 0%;
    }
  }
  
  .animate-shrink {
    animation: shrink v-bind('props.duration + "ms"') linear forwards;
  }
  </style>