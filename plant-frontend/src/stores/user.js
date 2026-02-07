import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userId = ref(localStorage.getItem('userId') || '')
    const username = ref(localStorage.getItem('username') || '')
    const role = ref(localStorage.getItem('role') || '')

    function setLoginInfo(info) {
        token.value = info.token
        userId.value = info.userId
        username.value = info.username
        role.value = info.role

        localStorage.setItem('token', info.token)
        localStorage.setItem('userId', info.userId)
        localStorage.setItem('username', info.username)
        localStorage.setItem('role', info.role)
    }

    function logout() {
        token.value = ''
        userId.value = ''
        username.value = ''
        role.value = ''

        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
    }

    return { token, userId, username, role, setLoginInfo, logout }
})
