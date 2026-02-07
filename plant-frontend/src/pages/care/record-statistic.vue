<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>养护数据统计</span>
          <el-radio-group v-model="days" @change="fetchData">
            <el-radio-button :label="30">近30天</el-radio-button>
            <el-radio-button :label="90">近90天</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div ref="chartRef" style="width: 100%; height: 400px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue'
import request from '@/api/request'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const chartRef = ref(null)
const days = ref(30)
let myChart = null

const fetchData = async () => {
  if (!myChart) return

  myChart.showLoading()
  try {
    const res = await request.get('/care/record/statistic', {
      params: { userId: userStore.userId, days: days.value }
    })

    // Backend returns Map<String, Object>: dates, water, fertilizer, totalRecords
    const dates = res.dates || []
    const waterData = res.water || []
    const fertilizerData = res.fertilizer || []

    const option = {
      title: { text: '养护频率趋势' },
      tooltip: { trigger: 'axis' },
      legend: { data: ['浇水', '施肥'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: [
        { name: '浇水', type: 'line', data: waterData, smooth: true, itemStyle: { color: '#409EFF' } },
        { name: '施肥', type: 'bar', data: fertilizerData, itemStyle: { color: '#67C23A' } }
      ]
    }

    myChart.setOption(option)
  } catch (e) {
    console.error(e)
  } finally {
    myChart.hideLoading()
  }
}

onMounted(() => {
  myChart = echarts.init(chartRef.value)
  fetchData()

  window.addEventListener('resize', () => {
    myChart.resize()
  })
})
</script>
