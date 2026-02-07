<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>添加自定义植物</span>
        </div>
      </template>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 600px">
        <el-form-item label="植物名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入植物名称" />
        </el-form-item>
        <el-form-item label="属" prop="genus">
          <el-input v-model="form.genus" placeholder="例如：蔷薇属" />
        </el-form-item>
        <el-form-item label="种" prop="species">
          <el-input v-model="form.species" placeholder="例如：月季" />
        </el-form-item>
         <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入描述信息" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">保存并提交</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  name: '',
  genus: '',
  species: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入植物名称', trigger: 'blur' }]
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.post('/plant/local/add', form)
        // res is the localPlant object
        
        ElMessageBox.confirm(
          '植物添加成功！是否立即提交审核？只有审核通过后才可在官方库中搜索到。',
          '提示',
          {
            confirmButtonText: '立即提交',
            cancelButtonText: '稍后',
            type: 'success',
          }
        )
        .then(() => {
            submitAudit(res.id)
        })
        .catch(() => {
           resetForm()
        })
        
      } finally {
        loading.value = false
      }
    }
  })
}

const submitAudit = async (id) => {
    try {
        await request.put(`/plant/local/submit-audit/${id}`)
        ElMessage.success('提交审核成功')
        resetForm()
    } catch(e) {}
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
}
</script>
