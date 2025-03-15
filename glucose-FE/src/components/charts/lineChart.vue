<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useDataStore } from '@/stores/dataStore'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'
import { Line } from 'vue-chartjs'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

const dataStore = useDataStore()
const isLoading = ref(true)
const showRange = ref('week') // 'day', 'week', 'month', 'year'

// 自定義血糖範圍
const normalRange = {
  min: 70,  // 正常血糖下限
  max: 140  // 正常血糖上限
}

// 圖表選項
const options = computed(() => {
  return {
    responsive: true,
    maintainAspectRatio: false,
    elements: {
      line: {
        tension: 0.4 // 使線條更平滑
      },
      point: {
        radius: 4,
        hoverRadius: 6
      }
    },
    interaction: {
      mode: 'index',
      intersect: false
    },
    scales: {
      y: {
        beginAtZero: false,
        min: Math.max(0, Math.min(...dataStore.glucoseCfg.datasets[0].data) - 20),
        max: Math.max(...dataStore.glucoseCfg.datasets[0].data) + 20,
        grid: {
          drawBorder: false,
          color: 'rgba(200, 200, 200, 0.2)'
        },
        ticks: {
          font: {
            size: 12
          },
          callback: function(value) {
            return value + ' mg/dL'
          }
        },
        title: {
          display: true,
          text: '血糖 (mg/dL)',
          font: {
            size: 14,
            weight: 'bold'
          }
        }
      },
      x: {
        grid: {
          drawBorder: false,
          display: false
        },
        ticks: {
          font: {
            size: 12
          },
          maxRotation: 45,
          minRotation: 45
        },
        title: {
          display: true,
          text: '日期',
          font: {
            size: 14,
            weight: 'bold'
          }
        }
      }
    },
    plugins: {
      legend: {
        position: 'top',
        labels: {
          font: {
            size: 14
          }
        }
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        titleFont: {
          size: 14
        },
        bodyFont: {
          size: 13
        },
        callbacks: {
          label: function(context) {
            return `血糖: ${context.parsed.y} mg/dL`
          }
        }
      }
    },
    animation: {
      duration: 1500,
      easing: 'easeOutQuart'
    }
  }
})

// 美化數據格式
const formattedData = computed(() => {
  const data = JSON.parse(JSON.stringify(dataStore.glucoseCfg))
  
  // 添加血糖範圍顯示
  data.datasets.push({
    label: '正常範圍',
    data: data.labels.map(() => normalRange.max),
    borderColor: 'rgba(75, 192, 192, 0.3)',
    backgroundColor: 'rgba(75, 192, 192, 0.1)',
    borderWidth: 1,
    borderDash: [5, 5],
    pointRadius: 0,
    fill: '+1'
  })
  
  data.datasets.push({
    label: '正常範圍下限',
    data: data.labels.map(() => normalRange.min),
    borderColor: 'rgba(75, 192, 192, 0.3)',
    backgroundColor: 'rgba(0, 0, 0, 0)',
    borderWidth: 1,
    borderDash: [5, 5],
    pointRadius: 0,
    fill: false
  })
  
  // 增強主血糖數據的視覺效果
  data.datasets[0].borderColor = 'rgba(54, 162, 235, 1)'
  data.datasets[0].backgroundColor = 'rgba(54, 162, 235, 0.1)'
  data.datasets[0].fill = true
  data.datasets[0].pointBackgroundColor = data.datasets[0].data.map(value => {
    if (value > normalRange.max) return 'rgba(255, 99, 132, 1)'
    if (value < normalRange.min) return 'rgba(255, 159, 64, 1)'
    return 'rgba(54, 162, 235, 1)'
  })
  
  return data
})

// 過濾數據範圍
const filterDataByRange = () => {
  // 在真實應用中，這裡可以根據 showRange 的值過濾數據
  // 目前只做示範，實際使用時需要更新
  dataStore.fetchGlucose()
  isLoading.value = false
}

onMounted(() => {
  filterDataByRange()
})

watch(showRange, () => {
  isLoading.value = true
  filterDataByRange()
})
</script>

<template>
  <div class="bg-white rounded-lg shadow-xl p-6 transition-all duration-300 hover:shadow-2xl h-full flex flex-col">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-xl font-bold text-gray-800">血糖趨勢圖</h2>
      
      <div class="flex space-x-2">
        <button 
          v-for="range in ['day', 'week', 'month', 'year']" 
          :key="range"
          @click="showRange = range"
          :class="[
            'px-3 py-1 text-sm rounded-full transition-colors duration-200',
            showRange === range 
              ? 'bg-blue-500 text-white' 
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          ]"
        >
          {{ range === 'day' ? '日' : range === 'week' ? '週' : range === 'month' ? '月' : '年' }}
        </button>
      </div>
    </div>
    
    <div class="flex-grow relative">
      <div v-if="isLoading" class="absolute inset-0 flex items-center justify-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>
      
      <Line v-else :data="formattedData" :options="options" class="h-full" />
    </div>
    
    <div class="mt-6 flex justify-between text-sm text-gray-600">
      <div class="flex items-center">
        <span class="w-3 h-3 inline-block rounded-full bg-red-500 mr-1"></span>
        <span>高血糖</span>
      </div>
      <div class="flex items-center">
        <span class="w-3 h-3 inline-block rounded-full bg-blue-500 mr-1"></span>
        <span>正常血糖</span>
      </div>
      <div class="flex items-center">
        <span class="w-3 h-3 inline-block rounded-full bg-orange-500 mr-1"></span>
        <span>低血糖</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 添加漸變效果 */
.line-chart-enter-active, .line-chart-leave-active {
  transition: opacity 0.5s ease;
}

.line-chart-enter-from, .line-chart-leave-to {
  opacity: 0;
}
</style>