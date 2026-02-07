import request from './request'

// Get schedule list
export function getScheduleList(params) {
    return request({
        url: '/care/schedule/query',
        method: 'get',
        params
    })
}

// Create schedule
export function createSchedule(data) {
    return request({
        url: '/care/schedule/add',
        method: 'post',
        data
    })
}

// Update schedule
export function updateSchedule(id, data) {
    return request({
        url: `/care/schedule/update/${id}`,
        method: 'put',
        data
    })
}

// Add care record
export function addCareRecord(data) {
    return request({
        url: '/care/record/add',
        method: 'post',
        data
    })
}

// Get statistics
export function getCareStatistics(params) {
    return request({
        url: '/care/record/stats', // Assuming this endpoint exists based on page name, need to verify
        method: 'get',
        params
    })
}
