<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="200px" class="aside-container">
        <Sidebar />
      </el-aside>
      <el-container>
        <el-header class="header-container">
          <div class="header-left">
            <span>植物养护管理系统</span>
          </div>
          <div class="header-right">
            <span class="username">{{ userStore.username }}</span>
            <el-button type="info" link @click="handleLogout">退出登录</el-button>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import Sidebar from './components/Sidebar.vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.common-layout {
  height: 100vh;
  display: flex;
}

.el-container {
  height: 100%;
}

.aside-container {
  background-color: #304156;
  color: #fff;
  height: 100%;
  overflow-y: auto;
}

.header-container {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
  
  .username {
    font-size: 14px;
    color: #606266;
  }
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
