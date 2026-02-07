import request from './request'

// Get local plant list
export function getLocalPlantList(params) {
    return request({
        url: '/plant/local/list',
        method: 'get',
        params
    })
}

// Add local plant
export function addLocalPlant(data) {
    return request({
        url: '/plant/local/add',
        method: 'post',
        data
    })
}

// Get official plant list
export function getOfficialPlantList(params) {
    return request({
        url: '/plant/official/query',
        method: 'get',
        params
    })
}

// Submit for audit
export function submitAudit(localPlantId) {
    return request({
        url: `/plant/local/submit-audit/${localPlantId}`,
        method: 'put'
    })
}

// Audit Order (Approve/Reject)
export function auditOrder(id, data) {
    return request({
        url: `/plant/audit/${id}`,
        method: 'put',
        data
    })
}

// Get Audit List (Admin)
export function getAuditList(params) {
    return request({
        url: '/plant/audit/list',
        method: 'get',
        params
    })
}
