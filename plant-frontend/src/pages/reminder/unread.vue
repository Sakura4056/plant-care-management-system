<template>
    <div class="app-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>未读消息</span>
                    <el-button link type="primary" @click="fetchAll">刷新</el-button>
                </div>
            </template>

            <el-tabs v-model="activeTab">
                <el-tab-pane label="养护提醒" name="care">
                    <template v-if="careReminders.length > 0">
                        <el-alert v-for="item in careReminders" :key="item.id" :title="item.title"
                            :description="item.content" type="warning" show-icon style="margin-bottom: 10px"
                            @close="markRead(item.id)" />
                    </template>
                    <el-empty v-else description="暂无养护提醒" />
                </el-tab-pane>

                <el-tab-pane label="系统通知" name="system">
                    <template v-if="systemReminders.length > 0">
                        <el-alert v-for="item in systemReminders" :key="item.id" :title="item.title"
                            :description="item.content" type="info" show-icon style="margin-bottom: 10px"
                            @close="markRead(item.id)" />
                    </template>
                    <el-empty v-else description="暂无系统通知" />
                </el-tab-pane>
            </el-tabs>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getUnread, markRead as markReadAPI } from '@/api/reminder'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const allReminders = ref([])
const activeTab = ref('care')

const careReminders = computed(() => allReminders.value.filter(r => r.type === 'CARE'))
const systemReminders = computed(() => allReminders.value.filter(r => r.type !== 'CARE'))

const fetchAll = async () => {
    try {
        const res = await getUnread(userStore.userId)
        // Backend returns: { totalUnread: X, details: { "careSchedule": [...], ... } }
        if (res && res.details) {
            let list = []
            if (res.details.careSchedule) {
                res.details.careSchedule.forEach(item => { item.type = 'CARE'; list.push(item) })
            }
            if (res.details.plantAudit) {
                res.details.plantAudit.forEach(item => { item.type = 'SYSTEM'; list.push(item) })
            }
            // Add other types if needed
            allReminders.value = list
        }
    } catch (e) {
        console.error(e)
    }
}

const markRead = async (id) => {
    try {
        await markReadAPI(id)
        // Optimistic remove
        allReminders.value = allReminders.value.filter(i => i.id !== id)
    } catch (e) { }
}

onMounted(() => {
    fetchAll()
})
</script>
