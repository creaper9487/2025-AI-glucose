<script setup>
import { ref } from 'vue';

const isHovered = ref(false);
</script>

<template>
    <a
        href="https://github.com/creaper9487/2025-AI-glucose"
        target="_blank"
        class="inline-flex items-center px-4 py-2 rounded-full shadow-lg transition-all duration-300 transform hover:scale-105"
        :class="{ 
            'bg-gradient-to-r from-blue-600 to-teal-500 text-white shadow-blue-500/50': isHovered,
            'bg-white text-gray-800': !isHovered
        }"
        @mouseenter="isHovered = true"
        @mouseleave="isHovered = false"
    >
        <svg 
            class="h-5 w-5 mr-2 transition-all duration-300" 
            :class="{ 'animate-pulse': isHovered }"
            fill="currentColor" 
            viewBox="0 0 24 24" 
            aria-hidden="true">
            <path fill-rule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clip-rule="evenodd" />
        </svg>
        <span :class="{ 'font-medium': isHovered }">關於我們</span>
        <svg 
            v-if="isHovered"
            xmlns="http://www.w3.org/2000/svg" 
            class="h-4 w-4 ml-1 transition-transform duration-300 transform translate-x-0"
            :class="{ 'translate-x-1': isHovered }"
            fill="none" 
            viewBox="0 0 24 24" 
            stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
        </svg>
    </a>
</template>

<style scoped>
@keyframes pulse {
    0%, 100% {
        opacity: 1;
    }
    50% {
        opacity: 0.7;
    }
}

.animate-pulse {
    animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>