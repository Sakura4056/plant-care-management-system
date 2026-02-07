<template>
    <div class="app-container">
        <!-- Weather Card -->
        <el-card shadow="hover" style="margin-bottom: 20px">
            <div class="weather-container">
                <div class="weather-info">
                    <div class="city-switch">
                        <el-icon>
                            <Location />
                        </el-icon>
                        <span v-if="!isEditingCity" @click="isEditingCity = true" class="city-name">{{ weather.city ||
                            '定位中...' }}</span>
                        <el-input v-else v-model="inputCity" placeholder="输入城市名(如北京)" size="small" style="width: 150px"
                            @blur="handleCitySubmit" @keyup.enter="handleCitySubmit" ref="cityInputRef" />
                    </div>
                    <div class="weather-detail" v-if="weather.weather">
                        <span class="temp">{{ weather.temperature }}°C</span>
                        <span class="cond">{{ weather.weather }}</span>
                        <span class="humid">湿度 {{ weather.humidity }}%</span>
                        <span class="time">更新于 {{ weather.reportTime }}</span>
                    </div>
                    <div v-else class="weather-detail">
                        <span>{{ weatherStatus }}</span>
                    </div>
                </div>
                <div class="weather-tips">
                    {{ getWeatherTip(weather.weather) }}
                </div>
            </div>
        </el-card>

        <!-- Top Cards -->
        <el-row :gutter="20">
            <el-col :span="8">
                <el-card shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <span>我的植物</span>
                        </div>
                    </template>
                    <div class="card-body">
                        <el-icon :size="40" color="#67C23A">
                            <Pear />
                        </el-icon>
                        <span class="count">{{ stats.plantCount || 0 }}</span>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <span>待办任务</span>
                        </div>
                    </template>
                    <div class="card-body">
                        <el-icon :size="40" color="#E6A23C">
                            <List />
                        </el-icon>
                        <span class="count">{{ stats.pendingTaskCount || 0 }}</span>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <span>今日提醒</span>
                        </div>
                    </template>
                    <div class="card-body">
                        <el-icon :size="40" color="#F56C6C">
                            <Bell />
                        </el-icon>
                        <span class="count">{{ stats.todayReminderCount || 0 }}</span>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <!-- Quick Actions & Recent -->
        <el-row :gutter="20" style="margin-top: 20px">
            <el-col :span="16">
                <el-card header="通过快捷方式快速开始">
                    <div class="action-buttons">
                        <el-button type="primary" size="large" icon="Plus"
                            @click="$router.push('/care/schedule-add')">新建计划</el-button>
                        <el-button type="success" size="large" icon="Upload"
                            @click="$router.push('/photo/upload')">上传照片</el-button>
                        <el-button type="info" size="large" icon="Search"
                            @click="$router.push('/plant/official')">查询百科</el-button>
                        <el-button type="warning" size="large" icon="DataLine"
                            @click="$router.push('/care/statistic')">查看统计</el-button>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card header="最近的未完成任务">
                    <el-table :data="recentTasks" style="width: 100%" :show-header="false">
                        <el-table-column prop="taskName" />
                        <el-table-column prop="dueTime" align="right">
                            <template #default="{ row }">
                                <span style="color: #909399; font-size: 12px">{{ formatTime(row.dueTime) }}</span>
                            </template>
                        </el-table-column>
                    </el-table>
                    <div style="margin-top: 10px; text-align: center">
                        <el-button link type="primary" @click="$router.push('/care/schedule-list')">查看全部</el-button>
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, onMounted, reactive, nextTick } from 'vue'
import { getLocalPlantList } from '@/api/plant'
import { getScheduleList } from '@/api/care'
import { getUnread } from '@/api/reminder'
import { Pear, List, Bell, Plus, Upload, Search, DataLine, Location } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

// --- Data ---
const stats = reactive({
    plantCount: 0,
    pendingTaskCount: 0,
    todayReminderCount: 0
})
const recentTasks = ref([])

// Weather Data
const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || ''
const weather = reactive({
    city: '',
    adcode: '',
    weather: '',
    temperature: '',
    humidity: '',
    reportTime: ''
})
const weatherStatus = ref('正在获取位置...')
const isEditingCity = ref(false)
const inputCity = ref('')
const cityInputRef = ref(null)


// --- Methods ---

const formatTime = (arr) => {
    if (Array.isArray(arr)) return `${arr[1]}-${arr[2]} ${arr[3]}:${String(arr[4]).padStart(2, '0')}`
    return arr
}

const fetchDashboardData = async () => {
    try {
        const [plantRes, scheduleRes, reminderRes] = await Promise.all([
            getLocalPlantList({ userId: userStore.userId }),
            getScheduleList({ userId: userStore.userId, status: 0, pageSize: 3 }),
            getUnread(userStore.userId)
        ])

        if (Array.isArray(plantRes)) {
            stats.plantCount = plantRes.length
        }

        if (scheduleRes && scheduleRes.records) {
            stats.pendingTaskCount = scheduleRes.total
            recentTasks.value = scheduleRes.records
        }

        if (reminderRes) {
            stats.todayReminderCount = reminderRes.totalUnread
        }
    } catch (e) {
        console.error(e)
    }
}

// Weather Logic
const loadWeather = async (cityCode = null) => {
    if (!AMAP_KEY) {
        weatherStatus.value = '请配置高德地图 Key'
        return
    }

    // 1. If no cityCode, IP Locate
    if (!cityCode) {
        try {
            const ipRes = await fetch(`https://restapi.amap.com/v3/ip?key=${AMAP_KEY}`)
            const ipData = await ipRes.json()
            if (ipData.status === '1') {
                cityCode = ipData.adcode
                weather.city = ipData.city
            } else {
                weatherStatus.value = '定位失败，请手动选择'
                return
            }
        } catch (e) {
            weatherStatus.value = '定位服务异常'
            return
        }
    }

    // 2. Get Weather
    if (cityCode) {
        try {
            const wRes = await fetch(`https://restapi.amap.com/v3/weather/weatherInfo?key=${AMAP_KEY}&city=${cityCode}`)
            const wData = await wRes.json()
            if (wData.status === '1' && wData.lives && wData.lives.length > 0) {
                const live = wData.lives[0]
                weather.city = live.city
                weather.weather = live.weather
                weather.temperature = live.temperature
                weather.humidity = live.humidity
                weather.reportTime = live.reporttime
            } else {
                weatherStatus.value = '天气数据不可用'
            }
        } catch (e) {
            weatherStatus.value = '获取天气失败'
        }
    }
}

const handleCitySubmit = async () => {
    if (!inputCity.value) {
        isEditingCity.value = false
        return
    }

    // Simple Geocode by name to get adcode is ideal, but for simplicity we rely on manual input matching strictly or search API.
    // Gaode doesn't allow "fuzzy search" in weather API directly usually.
    // Better workflow: Call Geocoding API to get adcode from address name.
    // https://restapi.amap.com/v3/geocode/geo?address=北京市&key=<key>

    try {
        const geoRes = await fetch(`https://restapi.amap.com/v3/geocode/geo?address=${inputCity.value}&key=${AMAP_KEY}`)
        const geoData = await geoRes.json()
        if (geoData.status === '1' && geoData.geocodes && geoData.geocodes.length > 0) {
            const adcode = geoData.geocodes[0].adcode
            await loadWeather(adcode)
            isEditingCity.value = false
        } else {
            ElMessage.error('找不到该城市，请尝试输入完整的城市名（如：北京市）')
        }
    } catch (e) {
        ElMessage.error('网络错误')
    }
}

const getWeatherTip = (weatherText) => {
    if (!weatherText) return '今天也要记得照顾好植物哦~'
    if (weatherText.includes('雨')) return '今天下雨，户外植物记得避雨，室内注意防霉哦~'
    if (weatherText.includes('晴') || weatherText.includes('多云')) return '天气不错，适合给植物晒晒太阳！'
    if (weatherText.includes('雪')) return '下雪啦，注意给植物保暖。'
    if (weatherText.includes('阴')) return '阴天光照不足，喜阳植物可能没精神。'
    return '根据天气调整植物养护计划吧！'
}

// Watch editor open to focus
import { watch } from 'vue'
watch(isEditingCity, (val) => {
    if (val) {
        inputCity.value = weather.city
        nextTick(() => {
            cityInputRef.value?.focus()
        })
    }
})

onMounted(() => {
    fetchDashboardData()
    loadWeather()
})
</script>

<style scoped>
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-body {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
}

.count {
    font-size: 28px;
    font-weight: bold;
    color: #303133;
}

.action-buttons {
    display: flex;
    gap: 15px;
    flex-wrap: wrap;
    justify-content: center;
    padding: 20px 0;
}

/* Weather Styles */
.weather-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.weather-info {
    display: flex;
    align-items: baseline;
    gap: 20px;
}

.city-switch {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 18px;
    font-weight: bold;
    color: #409EFF;
    cursor: pointer;
}

.city-name:hover {
    text-decoration: underline;
}

.weather-detail {
    display: flex;
    align-items: baseline;
    gap: 15px;
}

.temp {
    font-size: 32px;
    font-weight: bold;
    color: #303133;
}

.cond {
    font-size: 20px;
    color: #606266;
}

.humid,
.time {
    font-size: 14px;
    color: #909399;
}

.weather-tips {
    font-size: 14px;
    color: #67C23A;
    font-style: italic;
}
</style>
