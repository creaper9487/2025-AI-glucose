import { defineStore } from 'pinia'
import axiosInstance from '../components/axiosInstance'
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
    async fetchData(dataType) {
      try {
        const response = await axiosInstance.get('/api/user/' + dataType)
        return response.data
      } catch (error) {
        console.error('Error fetching data:', error)
      }
    },
    async fetchglucose() {
      try {
        const response = await this.fetchData('glucose')
        const glucoseData = response;
        console.log(glucoseData);
        const labels = [];
        const data = [];
        for (const key in glucoseData) {
          if (glucoseData.hasOwnProperty(key)) {
            labels.push(key);
            data.push(glucoseData[key]);
          }
        }
        this.glucoseCfg.labels = labels;
        this.glucoseCfg.datasets[0].data = data;
        console.log(this.glucoseCfg.labels, this.glucoseCfg.datasets[0].data);
      } catch (error) {
        console.error('Error fetching data:', error)
      }
    },
  },
})
