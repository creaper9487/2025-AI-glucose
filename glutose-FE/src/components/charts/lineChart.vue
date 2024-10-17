<script setup>
const options = {
    responsive: true,
    maintainAspectRatio: true
}
import { useDataStore } from '@/stores/dataStore';
const dataStore = useDataStore()
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
} from 'chart.js'
import { Line } from 'vue-chartjs'
ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
)
const data = dataStore.glucoseCfg
</script>


<template>
    <Line :data="data" :options="options" />
</template>