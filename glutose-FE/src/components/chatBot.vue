<script setup>
import { useChatStore } from '@/stores/chatStore';
const chatStore = useChatStore();
</script>
<template>
    <div
      v-if="chatStore.chatWindow"
      class="fixed inset-0 bg-black bg-opacity-50 z-10"
      @click="chatStore.chatWindow = false"
    ></div>
    <div class="chat-window" v-if="chatStore.chatWindow">
        <div class="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-gray-500 shadow-lg rounded-lg p-6 z-20 w-96" key="chatStore.chatContent">
            {{ chatStore.chatContent }}
        </div>
    </div>
</template>