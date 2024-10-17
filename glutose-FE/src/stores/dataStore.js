import { defineStore } from 'pinia'
import axios from 'axios'
export const useDataStore = defineStore('DataStore',{
  state: () => ({
    CH: [],
    exercise: [],
    insulin: [],
    glucoseCfg:{
        labels: [0,1,2,3,4,5,6],
        datasets: [
          {
            label: 'Data One',
            backgroundColor: '#FF0000',
            borderColor: '#36A2EB',
            color: 'FFFFFF',
            data: [40, 39, 10, 40, 39, 80, 40]
          }
        ]    }
  }),
  actions: {
    async fetchData() {
      try {
        const response = await axios.get('/api/data')
        this.dataList = response.data
      } catch (error) {
        console.error('Error fetching data:', error)
      }
    },
  },
})
