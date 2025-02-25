import { defineStore } from 'pinia'
import axios from 'axios'
export const useChatStore = defineStore('ChatStore', {
  state: () => ({
    chatContent: [],
    gram: 0,
    chatWindow: false,
    senseWindow: false,
  }),
  actions: {
    async fetchChatContent() {
      axios.post('/api/chat/', { content: "hello"}).then((response) => {
        this.chatContent = response.data
      }).catch((error) => {
        console.error('Error fetching chat content:', error)
      })
    },
    async SensePicture(imageFile) {
      if (!imageFile) {
          console.error('No file provided.');
          return;
      }
  
      const formData = new FormData();
      formData.append('image', imageFile);
  
      try {
          const response = await axios.post('/api/predict/', formData, {
              headers: {
                  'Content-Type': 'multipart/form-data',
              },
          });
          this.gram = response.data.predicted_value;
      } catch (error) {
          console.error('Error uploading image:', error);
      }
  }
  ,
  },
})
