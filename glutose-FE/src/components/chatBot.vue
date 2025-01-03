<template>
    <div>
      <!-- Button to open the floating window -->
      <button
        class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600"
        @click="openWindow"
      >
        Open Floating Window
      </button>
  
      <!-- Overlay (background dim) -->
      <div
        v-if="isWindowOpen"
        class="fixed inset-0 bg-black bg-opacity-50 z-10"
        @click="closeWindow"
      ></div>
  
      <!-- Floating window -->
      <div
        v-if="isWindowOpen"
        class="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-white shadow-lg rounded-lg p-6 z-20 w-96"
      >
        <h2 class="text-xl font-bold mb-4">Floating Window</h2>
        <p class="mb-4">This is a floating window with a dimmed background.</p>
        <button
          class="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600"
          @click="closeWindow"
        >
          Close
        </button>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        isWindowOpen: false, // State to toggle the floating window
      };
    },
    methods: {
      openWindow() {
        this.isWindowOpen = true;
      },
      closeWindow() {
        this.isWindowOpen = false;
      },
    },
  };
  </script>
  