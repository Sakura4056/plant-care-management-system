<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>添加养护记录</span>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 600px">
        <el-form-item label="任务信息" v-if="form.taskName">
            <el-tag>{{ form.taskName }}</el-tag>
            <span style="margin-left: 10px; color: #909399; font-size: 12px;">(来自养护计划)</span>
        </el-form-item>
        
        <el-form-item label="植物ID" prop="plantId">
          <el-input v-model="form.plantId" :disabled="!!route.query.plantId" />
        </el-form-item>
        
        <el-form-item label="养护操作">
            <el-space direction="vertical" alignment="start">
                <el-checkbox-group v-model="selectedOperations">
                    <el-checkbox label="WATERING">浇水</el-checkbox>
                    <el-checkbox label="FERTILIZING">施肥</el-checkbox>
                    <el-checkbox label="PRUNING">修剪</el-checkbox>
                    <el-checkbox label="PEST_CONTROL">除虫</el-checkbox>
                </el-checkbox-group>
            </el-space>
        </el-form-item>
        
        <!-- Detailed JSON input helper based on selection -->
        <el-form-item label="详细数据">
             <div v-if="selectedOperations.includes('WATERING')">
                <el-input v-model="operationDetails.waterAmount" placeholder="浇水量(ml)" style="width: 200px; margin-bottom: 5px;">
                    <template #prepend>浇水</template>
                    <template #append>ml</template>
                </el-input>
             </div>
             <div v-if="selectedOperations.includes('FERTILIZING')">
                <el-input v-model="operationDetails.fertilizerType" placeholder="肥料类型" style="width: 200px; margin-bottom: 5px;">
                    <template #prepend>施肥</template>
                </el-input>
             </div>
        </el-form-item>

        <el-form-item label="备注" prop="result">
          <el-input v-model="form.result" type="textarea" placeholder="例如：生长状况良好" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交记录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import request from '@/api/request'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const selectedOperations = ref([])
const operationDetails = reactive({
    waterAmount: '',
    fertilizerType: ''
})

const form = reactive({
  userId: userStore.userId,
  plantId: '',
  plantSource: 'OFFICIAL', // Default
  scheduleId: '',
  operationType: '',
  operationDetails: '',
  result: ''
})

const rules = {
  plantId: [{ required: true, message: '请输入植物ID', trigger: 'blur' }]
}

onMounted(() => {
    // Fill from query if available
    if (route.query.plantId) {
        form.plantId = route.query.plantId
        form.plantSource = route.query.plantSource || 'OFFICIAL'
        form.scheduleId = route.query.scheduleId
        form.taskName = route.query.taskName
    }
})

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      if (selectedOperations.value.length === 0) {
          ElMessage.warning('请至少选择一项养护操作')
          return
      }
      
      loading.value = true
      
      // Construct JSON for details
      const details = {}
      if (selectedOperations.value.includes('WATERING') && operationDetails.waterAmount) {
          details.waterAmount = operationDetails.waterAmount
      }
      if (selectedOperations.value.includes('FERTILIZING') && operationDetails.fertilizerType) {
          details.fertilizerType = operationDetails.fertilizerType
      }
      
      form.operationType = selectedOperations.value.join(',')
      form.operationDetails = JSON.stringify(details)
      
      try {
        await request.post('/care/record/add', form)
        ElMessage.success('记录添加成功')
        router.push('/care/schedule-list')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>
