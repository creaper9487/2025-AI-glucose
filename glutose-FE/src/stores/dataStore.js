import { defineStore } from 'pinia'
import axiosInstance from '../components/axiosInstance'
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
    async fetchData(dataType) {
      try {
        console.log('/api/user/'+ dataType)
        const response = await axiosInstance.get('/api/user/'+ dataType)
        console.log(response)
        return response.data
      } catch (error) {
        console.error('Error fetching data:', error)
      }
    },
    async fetchglucose() {
      try {
        const response = await this.fetchData('glucose')
        console.log(response)
        const glucoseData = response;
        const labels = [];
        const data = [];
        for (let i = 0; i < glucoseData.length; i++) {
          const [x, y] = glucoseData[i].split(':').map(Number);
          labels.push(x);
          data.push(y);
        }
        this.glucoseCfg.labels = labels;
        this.glucoseCfg.datasets[0].data = data;
      } catch (error) {
        console.error('Error fetching data:', error)
      }
    },
  },
})
