import request from './request'

// Update reminder config
export function updateConfig(data) {
    return request({
        url: '/reminder/config/update',
        method: 'put',
        data
    })
}

// Get unread reminders
export function getUnread(userId) {
    return request({
        url: `/reminder/unread/${userId}`,
        method: 'get'
    })
}

// Mark reminder as read
export function markRead(id) {
    return request({
        url: `/reminder/read/${id}`,
        method: 'put'
    })
}
