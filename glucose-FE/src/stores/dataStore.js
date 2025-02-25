import { defineStore } from 'pinia'
import { useAuthStore } from './authStore'
import axios from 'axios'
export const useDataStore = defineStore('DataStore', {
  state: () => ({
    CH: [],
    exercise: [],
    insulin: [],
    glucoseCfg: {
      labels: [0, 1, 2, 3, 4, 5, 6],
      datasets: [
        {
          label: '血糖',
          backgroundColor: '#FF0000',
          borderColor: '#36A2EB',
          color: 'FFFFFF',
          data: [40, 39, 10, 40, 39, 80, 40]
        }
      ]
    }
  }),
  actions: {
    fetchGlucose() {
      axios.get('/api/records/').then((response) => {
        console.log(response.data)
      }).catch((error) => {
        if (error.response && error.response.status === 401) {
          useAuthStore.refreshTokens();
        } else {
          console.error('Error get glucose:', error);
        }
      })
    },
    async postGlucose(datoid){
      try {
          const response = await axios.post('/api/records/', datoid);
          if(response!=null)alert("上傳成功");
      } catch (error) {
          console.error('Error refreshing tokens:', error);
      }
  }
  },
})
