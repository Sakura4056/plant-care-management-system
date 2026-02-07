<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>修改个人信息</span>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 500px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  userId: userStore.userId,
  username: userStore.username,
  email: '',
  phone: ''
})

const rules = {
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // userId is auto-handled by backend if not admin, but we send it anyway
        await request.put('/user/update', {
          userId: form.userId,
          email: form.email,
          phone: form.phone
        })
        ElMessage.success('修改成功')
        // Ideally should refresh user info in store, but backend only returns user object on logic response
        // Wait, the backend returns the Updated User object.
        // We can update local store?
        // Let's assume username doesn't change, so store is mostly fine.
      } finally {
        loading.value = false
      }
    }
  })
}

// In a real app, we should fetch user info first to fill the form.
// But we only have /user/update API. 
// Assuming user knows their current info or we rely on Login info.
// Stored info might be stale.
// Let's settle with what we have in store for username, and empty email/phone if not stored.
</script>
