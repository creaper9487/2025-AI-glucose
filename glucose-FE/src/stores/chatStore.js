import { defineStore } from 'pinia'
import axiosInstance from '../components/axiosInstance'
export const useChatStore = defineStore('ChatStore', {
  state: () => ({
    chatContent: [],
    gram: 0,
    chatWindow: false,
    senseWindow: false,
  }),
  actions: {
    async fetchChatContent() {
      axiosInstance.post('/api/chat/', { content: "hello"}).then((response) => {
        this.chatContent = response.data
      }).catch((error) => {
        console.error('Error fetching chat content:', error)
      })
    },
    async SensePicture(data) {
      try {
        const response = await axiosInstance.post('/api/predict/', data)
        this.gram = response.data.predicted_value
      } catch (error) {
        console.error('Error fetching data:', error)
      }
    },
  },
})
