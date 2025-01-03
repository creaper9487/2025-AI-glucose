import { defineStore } from 'pinia'
import axiosInstance from '../components/axiosInstance'
export const useChatStore = defineStore('ChatStore', {
  state: () => ({
    chatContent: [],
    chatWindow: false,
  }),
  actions: {
    fetchChatContent() {
      axiosInstance.post('/api/chat/', { content: "hello"}).then((response) => {
        this.chatContent = response.data
      }).catch((error) => {
        console.error('Error fetching chat content:', error)
      })
    }
  },
})
