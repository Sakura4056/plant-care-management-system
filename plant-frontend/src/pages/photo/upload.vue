<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>上传成长照片</span>
        </div>
      </template>

      <el-form :model="form" ref="formRef" label-width="100px" style="max-width: 600px">
        <el-form-item label="选择植物" required>
            <el-select 
                v-model="form.plantId" 
                placeholder="请选择植物" 
                style="width: 100%"
                :loading="loadingPlants"
                @change="handlePlantChange"
            >
                <el-option
                    v-for="item in plantOptions"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                />
            </el-select>
        </el-form-item>
        
        <el-form-item label="照片备注">
            <el-input v-model="form.remarks" type="textarea" />
        </el-form-item>
        
         <el-form-item label="图片文件" required>
            <el-upload
                class="upload-demo"
                drag
                :action="uploadUrl"
                :headers="headers"
                :data="extraData"
                :before-upload="beforeUpload"
                :on-success="handleSuccess"
                :on-error="handleError"
            >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                    拖拽文件到此处 或 <em>点击上传</em>
                </div>
                <template #tip>
                    <div class="el-upload__tip">
                        只能上传 jpg/png 文件，且不超过 5MB
                    </div>
                </template>
            </el-upload>
        </el-form-item>
        
        <el-form-item>
            <el-button @click="$router.back()">返回列表</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getLocalPlantList } from '@/api/plant'

const router = useRouter()
const userStore = useUserStore()
const uploadUrl = `${import.meta.env.VITE_API_BASE_URL}/photo/upload`

const form = reactive({
    plantId: '',
    plantSource: 'LOCAL', // Default to LOCAL now as we fetch local plants
    plantName: '',
    remarks: ''
})

const loadingPlants = ref(false)
const plantOptions = ref([])

const headers = computed(() => ({
    'Authorization': `Bearer ${userStore.token}`
}))

// Extra data to send with the file
const extraData = computed(() => ({
    userId: userStore.userId,
    plantId: form.plantId,
    plantSource: form.plantSource,
    remarks: form.remarks,
    isPublic: 0 // Default private
}))

const loadPlants = async () => {
    loadingPlants.value = true
    try {
        const res = await getLocalPlantList({ userId: userStore.userId })
        plantOptions.value = res || []
    } catch (e) {
        ElMessage.error('无法加载植物列表')
    } finally {
        loadingPlants.value = false
    }
}

const handlePlantChange = (val) => {
    // Determine source if we mix sources later. For now assume LOCAL.
    // Ideally we might store source in the option value or object.
    const selected = plantOptions.value.find(p => p.id === val)
    if (selected) {
        form.plantName = selected.name
        // form.plantSource = selected.source || 'LOCAL' // Future proofing
    }
}

const beforeUpload = (rawFile) => {
    if (!form.plantId) {
        ElMessage.warning('请先选择植物')
        return false
    }
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
        ElMessage.error('只能上传 JPG/PNG 格式的图片!')
        return false
    }
    if (rawFile.size / 1024 / 1024 > 5) {
        ElMessage.error('图片大小不能超过 5MB!')
        return false
    }
    return true
}

const handleSuccess = (res) => {
    if (res.code === 200) {
        ElMessage.success('上传成功')
        router.push('/photo/list')
    } else {
        ElMessage.error(res.message || '上传失败')
    }
}

const handleError = () => {
    ElMessage.error('上传出错')
}

onMounted(() => {
    loadPlants()
})
</script>
