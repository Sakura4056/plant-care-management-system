import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Layout from '@/layout/index.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/pages/login/index.vue'),
        meta: { hidden: true }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/pages/register/index.vue'),
        meta: { hidden: true }
    },
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/pages/dashboard/index.vue'),
                meta: { title: '首页', icon: 'House' }
            }
        ]
    },
    {
        path: '/plant',
        component: Layout,
        meta: { title: '植物管理', icon: 'Plant' },
        children: [
            {
                path: 'official',
                name: 'OfficialPlant',
                component: () => import('@/pages/plant/official.vue'),
                meta: { title: '官方植物库' }
            },
            {
                path: 'local-add',
                name: 'LocalPlantAdd',
                component: () => import('@/pages/plant/local-add.vue'),
                meta: { title: '添加自定义植物' }
            },
            {
                path: 'local-list',
                name: 'LocalPlantList',
                component: () => import('@/pages/plant/local-list.vue'),
                meta: { title: '我的植物' }
            }
        ]
    },
    {
        path: '/care',
        component: Layout,
        meta: { title: '养护管理', icon: 'Calendar' },
        children: [
            {
                path: 'schedule-list',
                name: 'CareSchedule',
                component: () => import('@/pages/care/schedule-list.vue'),
                meta: { title: '养护计划' }
            },
            {
                path: 'schedule-add',
                name: 'CareScheduleAdd',
                component: () => import('@/pages/care/schedule-add.vue'),
                meta: { title: '新建计划', hidden: true }
            },
            {
                path: 'record-add',
                name: 'CareRecordAdd',
                component: () => import('@/pages/care/record-add.vue'),
                meta: { title: '添加记录' }
            },
            {
                path: 'record-statistic',
                name: 'CareStatistic',
                component: () => import('@/pages/care/record-statistic.vue'),
                meta: { title: '养护统计' }
            }
        ]
    },
    {
        path: '/photo',
        component: Layout,
        meta: { title: '成长相册', icon: 'Camera' },
        children: [
            {
                path: 'list',
                name: 'PhotoList',
                component: () => import('@/pages/photo/list.vue'),
                meta: { title: '相册列表' }
            },
            {
                path: 'upload',
                name: 'PhotoUpload',
                component: () => import('@/pages/photo/upload.vue'),
                meta: { title: '上传照片' }
            }
        ]
    },
    {
        path: '/reminder',
        component: Layout,
        meta: { title: '提醒通知', icon: 'Bell' },
        children: [
            {
                path: 'unread',
                name: 'UnreadReminder',
                component: () => import('@/pages/reminder/unread.vue'),
                meta: { title: '未读消息' }
            },
            {
                path: 'config',
                name: 'ReminderConfig',
                component: () => import('@/pages/reminder/config.vue'),
                meta: { title: '提醒配置' }
            }
        ]
    },
    {
        path: '/user',
        component: Layout,
        meta: { hidden: true },
        children: [
            {
                path: 'update',
                name: 'UserUpdate',
                component: () => import('@/pages/user/update.vue'),
                meta: { title: '个人信息' }
            }
        ]
    },
    {
        path: '/admin',
        component: Layout,
        meta: { title: '系统管理', icon: 'Setting', roles: ['ADMIN'] },
        children: [
            {
                path: 'user-list',
                name: 'UserList',
                component: () => import('@/pages/admin/user-list.vue'),
                meta: { title: '用户管理' }
            },
            {
                path: 'plant-audit',
                name: 'PlantAudit',
                component: () => import('@/pages/admin/plant-audit.vue'),
                meta: { title: '植物审核' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    const token = userStore.token
    const role = userStore.role

    if (to.path === '/login' || to.path === '/register') {
        next()
    } else {
        if (!token) {
            next(`/login?redirect=${to.fullPath}`)
        } else {
            // Role Check
            if (to.meta.roles && !to.meta.roles.includes(role)) {
                // 403
                next('/403') // Ideally create a 403 page
            } else {
                next()
            }
        }
    }
})

export default router
