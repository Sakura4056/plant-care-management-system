import request from './request'

// Upload photo
export function uploadPhoto(data) {
    return request({
        url: '/photo/upload',
        method: 'post',
        data,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// Query photos
export function queryPhotos(params) {
    return request({
        url: '/photo/query',
        method: 'get',
        params
    })
}

// Delete photo
export function deletePhoto(id) {
    return request({
        url: `/photo/delete/${id}`,
        method: 'delete'
    })
}

// Get photo view URL (helper, not async request)
export function getPhotoViewUrl(filename) {
    if (!filename) return ''
    // Ensure filename is clean
    const name = filename.replace(/\\/g, '/').split('/').pop()
    return `${import.meta.env.VITE_API_BASE_URL}/photo/view/${name}`
}
