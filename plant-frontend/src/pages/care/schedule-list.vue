<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>养护计划列表</span>
          <el-button type="primary" icon="Plus" @click="$router.push('/care/schedule-add')">新增计划</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="未完成" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="逾期" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="scheduleList" border style="width: 100%">
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="plantSource" label="植物来源" width="100">
             <template #default="scope">
                <el-tag type="info">{{ scope.row.plantSource }}</el-tag>
             </template>
        </el-table-column>
        <el-table-column prop="dueTime" label="截止时间" width="180">
            <template #default="scope">
                {{ formatTime(scope.row.dueTime) }}
            </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0">未完成</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">已完成</el-tag>
            <el-tag v-else type="danger">逾期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button v-if="scope.row.status === 0" type="primary" link @click="completeTask(scope.row)">打卡</el-button>
            <el-button v-if="scope.row.status === 0" type="primary" link @click="editTask(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
       <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="handleQuery"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const scheduleList = ref([])
const total = ref(0)

const queryParams = reactive({
  userId: userStore.userId,
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

const formatTime = (arr) => {
    if(Array.isArray(arr)) {
        // [2024, 1, 1, 10, 0]
        return `${arr[0]}-${String(arr[1]).padStart(2,'0')}-${String(arr[2]).padStart(2,'0')} ${String(arr[3]).padStart(2,'0')}:${String(arr[4]).padStart(2,'0')}`
    }
    return arr
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await request.get('/care/schedule/query', { params: queryParams })
    scheduleList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const completeTask = (row) => {
    // Redirect to Record Add page with pre-filled info
    router.push({
        path: '/care/record-add',
        query: {
            scheduleId: row.id,
            plantId: row.plantId,
            plantSource: row.plantSource,
            taskName: row.taskName
        }
    })
}

const editTask = (row) => {
    router.push({
        path: '/care/schedule-add',
        query: {
            id: row.id,
            plantId: row.plantId,
            plantSource: row.plantSource,
            taskName: row.taskName,
            dueTime: row.dueTime,
            // Pass other necessary fields or just ID and let add page fetch?
            // Passing basic info is faster.
            // Let's pass ID and let add page handle "Edit Mode" distinction
        }
    })
}

const handleDelete = (row) => {
    ElMessageBox.confirm(
        '确认删除该养护计划吗？',
        '警告',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(async () => {
        try {
            await request.delete(`/care/schedule/delete/${row.id}`)
            ElMessage.success('删除成功')
            handleQuery()
        } catch(e) {}
    })
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
