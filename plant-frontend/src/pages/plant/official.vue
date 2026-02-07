<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>官方植物库查询</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="植物名称/科属" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="plantList" border style="width: 100%">
        <el-table-column prop="name" label="名称" width="180" />
        <el-table-column prop="genus" label="属" width="120" />
        <el-table-column prop="species" label="种" width="120" />
        <el-table-column label="操作" width="100">
             <template #default="scope">
                 <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
             </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
      
      <el-dialog v-model="dialogVisible" title="植物详情">
         <p v-if="currentPlant">
            <strong>名称:</strong> {{ currentPlant.name }}<br/>
            <strong>科属:</strong> {{ currentPlant.genus }} {{ currentPlant.species }}<br/><br/>
            <strong>描述:</strong><br/>
            {{ currentPlant.description }}
         </p>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'

const loading = ref(false)
const plantList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentPlant = ref(null)

const handleDetail = (row) => {
    currentPlant.value = row
    dialogVisible.value = true
}

const queryParams = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 10
})

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await request.get('/plant/official/query', { params: queryParams })
    plantList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.pageNum = 1
  handleQuery()
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
