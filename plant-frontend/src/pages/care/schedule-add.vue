<template>
    <div class="app-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>{{ isEditMode ? '编辑养护计划' : '新建养护计划' }}</span>
                </div>
            </template>

            <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" style="max-width: 600px">
                <el-form-item label="添加模式">
                    <el-radio-group v-model="form.isNewPlant" :disabled="isEditMode">
                        <el-radio :label="false">选择现有</el-radio>
                        <el-radio :label="true">创建新植物</el-radio>
                    </el-radio-group>
                </el-form-item>

                <template v-if="!form.isNewPlant">
                    <el-form-item label="搜索植物" prop="selectedPlantKey" required>
                        <el-select v-model="form.selectedPlantKey" :disabled="isEditMode" filterable remote
                            reserve-keyword placeholder="请输入植物名称搜索 (官方/自定义)" :remote-method="handleSearch"
                            :loading="searchLoading" style="width: 100%" @change="handlePlantSelect">
                            <!-- We bind visible value to a temp var or just handle change manually, 
                         but element-plus select v-model binds to value. 
                         Our value is distinct string 'LOCAL_1'. 
                         Wait, form.plantId expects Long. 
                         The select value must be unique string. 
                         Let's bind select to a separate model 'selectedPlantKey' and update form on change. -->
                            <el-option v-for="item in plantOptions" :key="item.value" :label="item.label"
                                :value="item.value" />
                        </el-select>
                        <!-- Hidden inputs for validation binding if needed, or we adjust rules to check form.plantId -->
                    </el-form-item>
                </template>

                <template v-else>
                    <el-form-item label="植物名称" prop="plantName">
                        <el-input v-model="form.plantName" placeholder="例如：我的发财树" />
                    </el-form-item>
                    <el-form-item label="科/属" style="margin-bottom: 0">
                        <el-col :span="11">
                            <el-form-item prop="genus">
                                <el-input v-model="form.genus" placeholder="科" />
                            </el-form-item>
                        </el-col>
                        <el-col :span="2" class="text-center">-</el-col>
                        <el-col :span="11">
                            <el-form-item prop="species">
                                <el-input v-model="form.species" placeholder="属" />
                            </el-form-item>
                        </el-col>
                    </el-form-item>
                    <el-form-item label="描述" prop="description" style="margin-top: 18px">
                        <el-input v-model="form.description" type="textarea" placeholder="备注信息" />
                    </el-form-item>
                </template>

                <el-form-item label="任务类型">
                    <el-radio-group v-model="form.taskType">
                        <el-radio-button v-for="type in taskTypeOptions" :key="type" :label="type">{{ type
                        }}</el-radio-button>
                    </el-radio-group>
                </el-form-item>

                <el-form-item label="任务名称" prop="taskName">
                    <el-input v-model="form.taskName" placeholder="例如：浇水、施肥（可自动生成）" />
                </el-form-item>

                <el-form-item label="计划时间" prop="dueTime">
                    <el-date-picker v-model="form.dueTime" type="datetime" placeholder="选择日期时间"
                        value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>

                <!-- Simplified Reminder config -->

                <el-form-item>
                    <el-button type="primary" @click="handleSubmit" :loading="loading">{{ isEditMode ? '更新计划' : '创建计划'
                    }}</el-button>
                    <el-button @click="$router.back()">取消</el-button>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import request from '@/api/request'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { updateSchedule } from '@/api/care'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const isEditMode = ref(false)
const editId = ref(null)

const taskTypeOptions = ['浇水', '施肥', '修剪', '换盆', '除虫', '其他']

const form = reactive({
    userId: userStore.userId,
    plantId: '',
    plantSource: 'OFFICIAL',
    taskType: '', // Added taskType
    taskName: '',
    dueTime: '',
    reminderConfig: '{}',
    // Quick Add flags
    isNewPlant: false,
    plantName: '',
    genus: '',
    species: '',
    description: '',
    selectedPlantKey: ''
})

const rules = computed(() => {
    const baseRules = {
        taskName: [{ required: true, message: '请输入或生成任务名称', trigger: 'blur' }],
        dueTime: [{ required: true, message: '请选择时间', trigger: 'change' }]
    }
    if (form.isNewPlant) {
        return {
            ...baseRules,
            plantName: [{ required: true, message: '请输入植物名称', trigger: 'blur' }]
        }
    } else {
        return {
            ...baseRules,
            selectedPlantKey: [{ required: true, message: '请选择植物', trigger: 'change' }]
        }
    }
})

// Search Logic
const plantOptions = ref([])
const searchLoading = ref(false)

// Watchers for Auto-Name Generation
// We want to generate "TaskType - PlantName"
// 1. Get current Plant Name
const currentPlantName = computed(() => {
    if (form.isNewPlant) {
        return form.plantName
    } else {
        const selected = plantOptions.value.find(item => item.value === form.selectedPlantKey)
        // If we are in edit mode and didn't load options, we might miss the name,
        // but for now let's rely on what we have.
        // If selected is found, use label (stripped of suffix) or just the name from details
        if (selected && selected.details) return selected.details.name
        return ''
    }
})

// 2. Watch dependencies
watch([() => form.taskType, currentPlantName], ([newType, newPlantName]) => {
    if (newType && newPlantName) {
        // Only auto-fill if taskName is empty OR it matches a pattern we previously generated?
        // Simple approach: Auto-fill if user hasn't manually radically changed it, 
        // or just overwrite if it looks like a generated name.
        // Let's just Overwrite for convenience, or check if empty.
        // Better: Overwrite. The user can edit AFTER selecting type.
        form.taskName = `${newType} - ${newPlantName}`
    } else if (newType) {
        form.taskName = `${newType}`
    }
})


const handleSearch = async (query) => {
    // Keep existing search logic, but maybe trigger it on mount if we have a plantId?
    // For now, keep as is.
    if (query !== '') {
        searchLoading.value = true
        try {
            const [officialRes, localRes] = await Promise.all([
                request.get(`/plant/official/query?keyword=${query}&pageSize=20`),
                request.get(`/plant/local/list?keyword=${query}`)
            ])

            const options = []

            if (localRes && Array.isArray(localRes)) {
                localRes.forEach(item => {
                    options.push({
                        value: `LOCAL_${item.id}`,
                        label: `${item.name} (自定义)`,
                        plantId: item.id,
                        plantSource: 'LOCAL',
                        details: item
                    })
                })
            }

            if (officialRes && officialRes.records) {
                officialRes.records.forEach(item => {
                    options.push({
                        value: `OFFICIAL_${item.id}`,
                        label: `${item.name} (官方)`,
                        plantId: item.id,
                        plantSource: 'OFFICIAL',
                        details: item
                    })
                })
            }

            plantOptions.value = options
        } catch (e) {
            console.error(e)
        } finally {
            searchLoading.value = false
        }
    } else {
        plantOptions.value = []
    }
}

const handlePlantSelect = (val) => {
    const selected = plantOptions.value.find(item => item.value === val)
    if (selected) {
        form.plantId = selected.plantId
        form.plantSource = selected.plantSource
    }
}

const handleSubmit = () => {
    formRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                if (isEditMode.value) {
                    const updateParams = {
                        id: editId.value,
                        taskName: form.taskName,
                        dueTime: form.dueTime,
                        reminderConfig: form.reminderConfig
                    }
                    await updateSchedule(editId.value, updateParams)
                    ElMessage.success('更新成功')
                } else {
                    const params = { ...form }
                    // Clean up temporary fields
                    delete params.taskType
                    delete params.selectedPlantKey

                    if (form.isNewPlant) {
                        delete params.plantId
                        delete params.plantSource
                    } else {
                        delete params.plantName
                        delete params.genus
                        delete params.species
                        delete params.description
                    }
                    await request.post('/care/schedule/add', params)
                    ElMessage.success('创建成功')
                }
                router.push('/care/schedule-list')
            } finally {
                loading.value = false
            }
        }
    })
}

onMounted(() => {
    if (route.query.id) {
        isEditMode.value = true
        editId.value = route.query.id
        form.taskName = route.query.taskName
        form.dueTime = route.query.dueTime

        // In edit mode for now, we don't try to reverse-engineer the Task Type
        form.selectedPlantKey = 'CURRENT_PLANT'
    }
})
</script>
