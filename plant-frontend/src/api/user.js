import request from './request'

// Register
export function register(data) {
    return request({
        url: '/user/register',
        method: 'post',
        data
    })
}

// Login
export function login(data) {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}

// Update user info
export function updateUserInfo(data) {
    return request({
        url: '/user/update',
        method: 'put',
        data
    })
}

// Delete user (Admin)
export function deleteUser(id) {
    return request({
        url: `/user/delete/${id}`,
        method: 'delete'
    })
}

// List users (Admin)
export function getUserList(params) {
    return request({
        url: '/user/list',
        method: 'get',
        params
    })
}
