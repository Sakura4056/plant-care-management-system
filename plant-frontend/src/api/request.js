import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// Request Interceptor
service.interceptors.request.use(
    config => {
        const userStore = useUserStore()
        const token = userStore.token
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// Response Interceptor
service.interceptors.response.use(
    response => {
        const res = response.data
        // Assuming backend returns { code: 200, data: ..., message: ... }
        // If standard Result structure:
        if (res.code !== 200) {
            ElMessage({
                message: res.message || 'Error',
                type: 'error',
                duration: 5 * 1000
            })
            return Promise.reject(new Error(res.message || 'Error'))
        } else {
            return res.data
        }
    },
    error => {
        console.error('err' + error)
        let message = error.message
        if (error.response) {
            const status = error.response.status
            switch (status) {
                case 401:
                    message = '未登录或Token过期，请重新登录'
                    const userStore = useUserStore()
                    userStore.logout()
                    router.push(`/login?redirect=${router.currentRoute.value.fullPath}`)
                    break
                case 403:
                    message = '拒绝访问：您没有权限执行此操作'
                    break
                case 404:
                    message = '请求的资源不存在'
                    break
                case 500:
                    message = '服务器内部错误'
                    break
                default:
                    message = `连接错误 ${status}`
            }
        }
        ElMessage({
            message: message,
            type: 'error',
            duration: 5 * 1000
        })
        return Promise.reject(error)
    }
)

export default service
