<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>提醒推送配置</span>
        </div>
      </template>

      <el-form :model="form" label-width="120px" style="max-width: 500px">
        <el-form-item label="接收邮箱">
          <el-input v-model="form.email" placeholder="接收通知的邮箱" />
        </el-form-item>
        <el-form-item label="接收手机">
          <el-input v-model="form.phone" placeholder="接收通知的手机号" />
        </el-form-item>

        <el-form-item label="推送渠道">
          <el-checkbox v-model="form.channels.popup">网页弹窗</el-checkbox>
          <el-checkbox v-model="form.channels.email">邮件通知</el-checkbox>
        </el-form-item>

        <el-form-item label="打扰模式">
          <el-switch v-model="form.doNotDisturb" active-text="免打扰 (22:00-08:00)" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="loading">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { updateConfig } from '@/api/reminder' // Use API module
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  email: '',
  phone: '', // Keep phone in UI but maybe disable/hide if SMS is gone? User authorized removing SMS function.
  // Actually backend still has `phone` field in ReminderConfig entity, just SMS service removed.
  // But for UI clarity, I should probably hide "SMS Channel" checkbox.
  channels: {
    popup: true,
    email: false
    // sms removed
  },
  doNotDisturb: false
})

const handleSave = async () => {
  loading.value = true
  try {
    const payload = {
      email: form.email,
      phone: form.phone,
      popupEnabled: form.channels.popup ? 1 : 0,
      bellEnabled: 1, // Defaulting bell to 1 or add UI for it? 
      // Current UI has "channels.email/sms/popup". 
      // Backend has `popupEnabled` and `bellEnabled`. 
      // Let's map `channels.popup` to `popupEnabled`.
      // And maybe add `channels.bell`? Or just ignore bell for now.
      // Let's assume `popup` covers on-site notifications.
      sceneConfig: JSON.stringify({ doNotDisturb: form.doNotDisturb }) // Store DnD in sceneConfig JSON
    }

    await updateReminderConfig(payload)
    ElMessage.success('配置已更新')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // Ideally fetch config here.
  // Since we don't have a GET endpoint, we start blank or rely on user re-entering.
  // Or we could populate email from UserStore if available.
  if (userStore.userInfo && userStore.userInfo.email) {
    form.email = userStore.userInfo.email
  }
})
</script>
