// eslint-disable-next-line vue/multi-word-component-names
<script setup>
import { useChatStore } from '@/stores/chatStore';
import VueMarked from 'vue-marked';
const chatStore = useChatStore();
</script>
<template>
    <div v-if="chatStore.chatWindow || chatStore.senseWindow || chatStore.testWindow"
        class="fixed inset-0 bg-black bg-opacity-50 z-10"
        @click="chatStore.chatWindow = false; chatStore.senseWindow = false"></div>
    <div class="chat-window" v-if="chatStore.chatWindow">

        <div class="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 justify-center bg-gray-500 shadow-lg rounded-lg p-6 z-20 w-96"
            key="chatStore.chatContent">
            <div>
                <button @click="chatStore.fetchChatContent" class="rounded bg-slate-200 p-2">✨ 讓 AI
                    再次以目前資料分析血糖狀況</button>
            </div>
            <div class="my-2 h-48 overflow-y-auto border bg-gray-300 rounded p-2" :key="chatStore.chatContent">
                <vue-marked :value="chatStore.chatContent.response.message.content" />
            </div>
        </div>
    </div>
    <div class="chat-window" v-if="chatStore.senseWindow">

        <div class="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 justify-center bg-gray-500 shadow-lg rounded-lg p-6 z-20 w-96"
            key="chatStore.chatContent">
            <div class="my-2 h-auto overflow-y-auto border bg-gray-300 rounded p-2">
                <p> 預測該照片中的碳水化合物含量是 {{ chatStore.gram }} 克。</p>
            </div>
            <img :src="chatStore.imgURL" alt="food" class="w-auto h-auto">
        </div>
    </div>
    <div class="chat-window" v-if="chatStore.testWindow">
        <div
            class="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 justify-center bg-gray-500 shadow-lg rounded-lg p-6 z-20 w-auto">
            <h2 class="text-xl font-bold mb-4 text-white">預測資料</h2>
            <form class="flex flex-row" @submit.prevent="chatStore.submitTest">
                <div class="mr-4">
                    <div class="mb-4">
                        <label class="block text-white text-sm font-medium mb-1">性別</label>
                        <div class="flex space-x-4">
                            <label class="inline-flex items-center">
                                <input type="radio" v-model="chatStore.healthData.gender" value="male"
                                    class="form-radio">
                                <span class="ml-2 text-white">男性</span>
                            </label>
                            <label class="inline-flex items-center">
                                <input type="radio" v-model="chatStore.healthData.gender" value="female"
                                    class="form-radio">
                                <span class="ml-2 text-white">女性</span>
                            </label>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label for="age" class="block text-white text-sm font-medium mb-1">年齡</label>
                        <input type="number" id="age" v-model="chatStore.healthData.age"
                            class="w-full px-3 py-2 bg-gray-200 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                    </div>

                    <div class="mb-4">
                        <label for="hypertension" class="block text-white text-sm font-medium mb-1">血壓指數</label>
                        <input type="number" id="hypertension" v-model="chatStore.healthData.hypertension" step="0.1"
                            class="w-full px-3 py-2 bg-gray-200 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                    </div>

                    <div class="mb-4">
                        <label class="block text-white text-sm font-medium mb-1">心臟疾病</label>
                        <div class="flex space-x-4">
                            <label class="inline-flex items-center">
                                <input type="radio" v-model="chatStore.healthData.heart_disease" :value="1"
                                    class="form-radio">
                                <span class="ml-2 text-white">是</span>
                            </label>
                            <label class="inline-flex items-center">
                                <input type="radio" v-model="chatStore.healthData.heart_disease" :value="0"
                                    class="form-radio">
                                <span class="ml-2 text-white">否</span>
                            </label>
                        </div>
                    </div>
                </div>
                <div class="ml-4">
                    <div class="mb-4">
                        <label for="smoking" class="block text-white text-sm font-medium mb-1">吸菸史</label>
                        <select id="smoking" v-model="chatStore.healthData.smoking_history"
                            class="w-full px-3 py-2 bg-gray-200 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                            <option value="never">從未吸菸</option>
                            <option value="former">曾經吸菸</option>
                            <option value="not current">目前不吸菸</option>
                            <option value="current">目前有吸菸</option>
                        </select>
                    </div>

                    <div class="mb-4">
                        <label for="bmi" class="block text-white text-sm font-medium mb-1">BMI 指數</label>
                        <input type="number" id="bmi" v-model="chatStore.healthData.bmi" step="0.1"
                            class="w-full px-3 py-2 bg-gray-200 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                    </div>

                    <div class="mb-4">
                        <label for="hba1c" class="block text-white text-sm font-medium mb-1">HbA1c 數值</label>
                        <input type="number" id="hba1c" v-model="chatStore.healthData.HbA1c_level" step="0.1"
                            class="w-full px-3 py-2 bg-gray-200 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                    </div>

                    <div class="mb-4">
                        <label for="bloodsugar" class="block text-white text-sm font-medium mb-1">血糖值</label>
                        <input type="number" id="bloodsugar" v-model="chatStore.healthData.bloodsugar"
                            class="w-full px-3 py-2 bg-gray-200 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                    </div>

                    <div class="flex justify-between">
                        <button type="submit" @click="chatStore.testWindow = false"
                            class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500">提交</button>
                        <button type="button" @click="chatStore.testWindow = false"
                            class="px-4 py-2 bg-gray-400 text-white rounded-md hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-500">取消</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
</template>