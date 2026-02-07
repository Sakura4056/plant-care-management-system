<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>成长相册</span>
          <el-button type="primary" icon="Upload" @click="$router.push('/photo/upload')">上传照片</el-button>
        </div>
      </template>

      <el-timeline>
        <el-timeline-item v-for="(activity, index) in photoList" :key="index" :timestamp="activity.createTime"
          placement="top">
          <el-card>
            <div style="display: flex; justify-content: space-between; align-items: start;">
              <h4>{{ activity.plantName ? `${activity.plantName} (ID: ${activity.plantId})` : `植物ID:
                ${activity.plantId}` }}
              </h4>
              <el-button type="danger" link icon="Delete" @click="handleDelete(activity.id)">删除</el-button>
            </div>
            <p>{{ activity.remarks }}</p>
            <el-image style="width: 200px; height: 200px; border-radius: 4px;" :src="getImageUrl(activity.filePath)"
              :preview-src-list="[getImageUrl(activity.filePath)]" fit="cover" />
          </el-card>
        </el-timeline-item>
        <el-timeline-item v-if="photoList.length === 0" timestamp="暂无数据" placement="top">
          <p>还没有上传过照片哦</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { queryPhotos, getPhotoViewUrl, deletePhoto } from '@/api/photo'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const photoList = ref([])

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除这张照片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePhoto(id)
      ElMessage.success('删除成功')
      fetchPhotos()
    } catch (e) { }
  })
}

const getImageUrl = (path) => {
  return getPhotoViewUrl(path)
}

const fetchPhotos = async () => {
  try {
    const res = await queryPhotos({ userId: userStore.userId })
    photoList.value = res.records || res
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchPhotos()
})
</script>
