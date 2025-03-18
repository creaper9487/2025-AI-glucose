import { defineStore } from 'pinia'
import axios from 'axios'
import { useAuthStore } from './authStore'
export const useChatStore = defineStore('ChatStore', {
  state: () => ({
    img:null,
    imgURL:null,
    chatContent: {content: "分析中..."},
    gram: 0,
    chatWindow: false,
    senseWindow: false,
    testWindow: false,
    // 新增預測相關狀態
    predictionWindow: false,
    predictionInput: {
      previous_blood_glucose: null,
      insulin_injection: 0,
      carbohydrate_intake: 0,
      time_interval: 0
    },
    predictionResult: null,
    healthData: {
      gender:null,
      age:null,
      hypertension:null,
      heart_disease:null,
      smoking_history:null,
      bmi:null,
      blood_glucose_level:null,
      HbA1c_level:null,
    },
    
    consent:null,
  }),
  actions: {
    async fetchChatContent() {
      const authStore = useAuthStore()
      axios.get('/api/chat/', { content: "hello"}).then((response) => {
        this.chatContent = response.data
        console.log(this.chatContent)
      }).catch((error) => {
        if (error.response && error.response.status === 401) {
          authStore.refreshTokens();
        } else {
          console.error('Error on chat', error);
        }      })
    },
    async SensePicture(imageFile) {
      const authStore = useAuthStore();
      console.log(imageFile);
      this.imgURL=URL.createObjectURL(imageFile);
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
        this.gram = (response.data.predicted_value).toFixed(2);
      } catch (error) {
        if (error.response && error.response.status === 401) {
          authStore.refreshTokens();
        } else {
          console.error('Error uploading image:', error);
        }
      }
    },
    async submitTest() {
      const authStore = useAuthStore();
      const formData = new FormData();
      // Append all health data fields to the form data
      for (const [key, value] of Object.entries(this.healthData)) {
        if (value !== null && value !== undefined) {
          formData.append(key, value);
        }
      }
      try {
        const response = await axios.post('/api/predictform/', formData, {
        });
        if(response.data==1){
          alert("您可能有糖尿病！請盡速至門診檢查")
        }else{
          alert("您的健康狀況良好，請繼續保持！")
        }
      } catch (error) {
        if (error.response && error.response.status === 401) {
          authStore.refreshTokens();
        } else {
          console.error('Error uploading image:', error);
        }
      }
    },
    // 新增血糖預測方法
    async predictBloodGlucose() {
      const authStore = useAuthStore();
      
      try {
        // 發送預測請求到後端API
        const response = await axios.post('/api/predict/personalized/', this.predictionInput);
        this.predictionResult = response.data;
        return this.predictionResult;
      } catch (error) {
        if (error.response && error.response.status === 401) {
          authStore.refreshTokens();
          return null;
        } else {
          console.error('血糖預測出錯:', error);
          if (error.response && error.response.data && error.response.data.error) {
            alert(`預測失敗: ${error.response.data.error}`);
          } else {
            alert('預測失敗，請確認您已經訓練了個人化模型');
          }
          return null;
        }
      }
    },
    // 獲取最新血糖記錄作為預測輸入的初始值
    async getLatestBloodGlucose() {
      const authStore = useAuthStore();
      
      try {
        const response = await axios.get('/api/records/');
        if (response.data && response.data.length > 0) {
          // 取得最新的血糖記錄
          const latestRecord = response.data[0];
          this.predictionInput.previous_blood_glucose = latestRecord.blood_glucose;
          return latestRecord.blood_glucose;
        }
        return null;
      } catch (error) {
        if (error.response && error.response.status === 401) {
          authStore.refreshTokens();
        } else {
          console.error('獲取最新血糖記錄出錯:', error);
        }
        return null;
      }
    },
  },
})