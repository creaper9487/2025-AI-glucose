// eslint-disable-next-line vue/multi-word-component-names
<script setup>
import { useChatStore } from '@/stores/chatStore';
const chatStore = useChatStore();
</script>
<template>
    <div v-if="chatStore.chatWindow || chatStore.senseWindow" class="fixed inset-0 bg-black bg-opacity-50 z-10"
        @click="chatStore.chatWindow = false;chatStore.senseWindow = false"></div>
    <div class="chat-window" v-if="chatStore.chatWindow">

        <div class="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 justify-center bg-gray-500 shadow-lg rounded-lg p-6 z-20 w-96"
            key="chatStore.chatContent">
            <div>
                <button @click="chatStore.sendAnalysisRequest" class="rounded bg-slate-200 p-2">✨ 讓 AI
                    再次以目前資料分析血糖狀況</button>
            </div>
            <div class="my-2 h-48 overflow-y-auto border bg-gray-300 rounded p-2">
                <p>{{ chatStore.chatContent }}</p>
            </div>
        </div>
    </div>
    <div class="chat-window" v-if="chatStore.senseWindow">

        <div class="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 justify-center bg-gray-500 shadow-lg rounded-lg p-6 z-20 w-96"
            key="chatStore.chatContent">
            <div class="my-2 h-48 overflow-y-auto border bg-gray-300 rounded p-2">
                <p> 預測該照片中的碳水化合物含量是 {{ chatStore.gram }} 克。</p>
            </div>
        </div>
    </div>
</template>