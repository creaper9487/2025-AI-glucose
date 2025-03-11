import { defineStore } from 'pinia'
import { useAuthStore } from './authStore'
import axios from 'axios'

// Configure axios with base URL
axios.defaults.baseURL = 'http://localhost:8000' // Change this to match your backend server URL

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
          data: [40, 39, 10, 40, 39, 80, 40],
        },
      ],
    },
  }),
  actions: {
    fetchGlucose() {
      const authStore = useAuthStore()

      axios
        .get('/api/records/')
        .then(response => {
          console.log(response.data)
          this.dataProc(response.data)
          this.glucoseCfg.labels = response.data.map(item => this.formatDate(item.created_at))
          this.glucoseCfg.datasets[0].data = response.data.map(item => item.blood_glucose)
          console.log(this.glucoseCfg.labels)
          console.log(this.glucoseCfg.datasets[0].data)
        })
        .catch(error => {
          if (error.response && error.response.status === 401) {
            authStore.refreshTokens()
          } else {
            console.error('Error get glucose:', error)
          }
        })
    },
    async postGlucose(datoid) {
      const authStore = useAuthStore()

      try {
        const response = await axios.post('/api/records/', datoid)
        if (response != null) alert('上傳成功')
      } catch (error) {
        if (error.response && error.response.status === 401) {
          authStore.refreshTokens()
        } else {
          console.error('Error pst glucose:', error)
        }
      }
    },
    dataProc(data) {
      // Assuming your data is stored in a variable called 'data'
      data.sort((a, b) => {
        // Convert the timestamps to Date objects for comparison
        const dateA = new Date(a.created_at)
        const dateB = new Date(b.created_at)

        // Sort in ascending order (oldest to newest)
        return dateA - dateB

        // For descending order (newest to oldest), use:
        // return dateB - dateA;
      })
    },
    formatDate(dateString) {
      const date = new Date(dateString)
      
      // 格式化為 MM/DD HH:MM
      const month = date.getMonth() + 1
      const day = date.getDate()
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      
      return `${month}/${day} ${hours}:${minutes}`
    }
  },
})