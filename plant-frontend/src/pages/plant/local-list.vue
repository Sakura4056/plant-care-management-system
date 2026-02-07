<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的自定义植物</span>
          <el-button type="primary" @click="$router.push('/plant/local-add')">添加植物</el-button>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="植物名称" />
        <el-table-column prop="genus" label="科" />
        <el-table-column prop="species" label="属" />
        <el-table-column prop="auditStatus" label="状态">
             <template #default="scope">
                <el-tag v-if="scope.row.auditStatus === 'LOCAL' || scope.row.auditStatus === 'UNSUBMITTED'" type="success">无需审核</el-tag>
                <el-tag v-else-if="scope.row.auditStatus === 'PENDING'" type="warning">审核中</el-tag>
                <el-tag v-else-if="scope.row.auditStatus === 'APPROVED'" type="success">审核通过</el-tag>
                <el-tag v-else type="info">{{ scope.row.auditStatus }}</el-tag>
             </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="180">
            <template #default="scope">
                <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])

const fetchList = async () => {
    loading.value = true
    try {
        const res = await request.get('/plant/local/list')
        tableData.value = res
    } catch(e) {
    } finally {
        loading.value = false
    }
}

const handleDelete = (row) => {
    ElMessageBox.confirm(
        '删除植物将同时删除该植物下所有未完成的养护计划，是否确认？',
        '警告',
        {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(async () => {
        try {
            await request.delete(`/plant/local/delete/${row.id}`)
            ElMessage.success('删除成功')
            fetchList()
        } catch(e) {}
    })
}

onMounted(() => {
    fetchList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
