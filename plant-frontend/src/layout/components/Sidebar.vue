<template>
  <el-menu
    active-text-color="#409EFF"
    background-color="#304156"
    class="el-menu-vertical-demo"
    text-color="#bfcbd9"
    router
    :default-active="activeMenu"
  >
    <div class="logo-container">P.M.S</div>
    
    <template v-for="route in menuRoutes" :key="route.path">
      <el-sub-menu v-if="route.children && route.children.length > 1" :index="route.path">
        <template #title>
          <el-icon v-if="route.meta && route.meta.icon">
            <component :is="route.meta.icon" />
          </el-icon>
          <span>{{ route.meta.title }}</span>
        </template>
        
        <el-menu-item 
          v-for="child in route.children" 
          :key="child.path" 
          :index="resolvePath(route.path, child.path)"
        >
          {{ child.meta.title }}
        </el-menu-item>
      </el-sub-menu>

      <el-menu-item 
        v-else-if="route.children && route.children.length === 1" 
        :index="resolvePath(route.path, route.children[0].path)"
      >
        <el-icon v-if="route.children[0].meta && route.children[0].meta.icon">
          <component :is="route.children[0].meta.icon" />
        </el-icon>
        <template #title>{{ route.children[0].meta.title }}</template>
      </el-menu-item>
      
       <el-menu-item 
        v-else-if="!route.children"
        :index="route.path"
      >
         <el-icon v-if="route.meta && route.meta.icon">
          <component :is="route.meta.icon" />
        </el-icon>
        <template #title>{{ route.meta.title }}</template>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const menuRoutes = computed(() => {
  return router.options.routes.filter(r => {
    if (r.meta && r.meta.hidden) return false
    if (r.meta && r.meta.roles && !r.meta.roles.includes(userStore.role)) return false
    return true
  })
})

const resolvePath = (basePath, routePath) => {
  if (basePath === '/') return '/' + routePath
  return `${basePath}/${routePath}`.replace(/\/+/g, '/')
}
</script>

<style scoped lang="scss">
.el-menu-vertical-demo {
  border-right: none;
}
.logo-container {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  background-color: #2b3649;
}
</style>
