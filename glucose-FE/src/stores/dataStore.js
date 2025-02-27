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
          data: [40, 39, 10, 40, 39, 80, 40],
        },
      ],
    },
  }),
  actions: {
    fetchGlucose() {
      axios
        .get('/api/records/')
        .then(response => {
          console.log(response.data)
        })
        .catch(error => {
          if (error.response && error.response.status === 401) {
            useAuthStore.refreshTokens()
          } else {
            console.error('Error get glucose:', error)
          }
        })
    },
    async postGlucose(datoid) {
      try {
        const response = await axios.post('/api/records/', datoid)
        if (response != null) alert('上傳成功')
      } catch (error) {
        console.error('Error refreshing tokens:', error)
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
      this.glucoseCfg.labels = data.map(item => item.created_at)
      this.glucoseCfg.datasets[0].data = data.map(item => item.blood_glucose)
        },
      },
    })
    },
  },
})
