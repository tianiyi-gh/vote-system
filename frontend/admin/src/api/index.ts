/**
 * API 配置文件
 */

// API 基础配置
const API_BASE_URL = ''

// 通用请求配置
const request = (url: string, options: any = {}) => {
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    },
    ...options
  }

  // 使用相对路径，由 Vite 代理转发
  return fetch(url, config)
    .then(async response => {
      if (!response.ok) {
        const errorText = await response.text()
        console.error(`HTTP ${response.status} - ${url}`, errorText)
        throw new Error(`HTTP error! status: ${response.status}${errorText ? `, details: ${errorText}` : ''}`)
      }
      return response.json()
    })
    .catch(error => {
      console.error('API请求错误:', error)
      throw error
    })
}

// 专门处理文件下载的请求
const downloadRequest = (url: string) => {
  return fetch(url, {
    headers: {
      'Accept': 'application/octet-stream'
    }
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      return response.blob()
    })
    .catch(error => {
      console.error('文件下载错误:', error)
      throw error
    })
}

// 专门处理文件上传的请求
const uploadRequest = (url: string, formData: FormData) => {
  return fetch(url, {
    method: 'POST',
    body: formData
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      return response.json()
    })
    .catch(error => {
      console.error('文件上传错误:', error)
      throw error
    })
}

// GET 请求
export const get = (url: string, params?: any) => {
  const queryString = params ? `?${new URLSearchParams(params)}` : ''
  return request(`${url}${queryString}`)
}

// POST 请求
export const post = (url: string, data?: any) => {
  return request(url, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

// PUT 请求
export const put = (url: string, data?: any) => {
  return request(url, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

// PUT 请求带查询参数
export const putWithParams = (url: string, params?: any) => {
  const queryString = params ? `?${new URLSearchParams(params)}` : ''
  return request(`${url}${queryString}`, {
    method: 'PUT'
  })
}

// DELETE 请求
export const del = (url: string) => {
  return request(url, {
    method: 'DELETE'
  })
}

// API 接口
export const api = {
  // 健康检查
  health: () => get('/health'),
  
  // 活动管理
  activities: {
    list: (params?: any) => get('/api/activities', params),
    create: (data: any) => post('/api/activities', data),
    update: (id: number, data: any) => put(`/api/activities/${id}`, data),
    delete: (id: number) => del(`/api/activities/${id}`),
    get: (id: number) => get(`/api/activities/${id}`),
    copy: (id: number) => post(`/api/activities/${id}/copy`)
  },
  
  // 候选人管理
  candidates: {
    list: (activityId: number) => get(`/api/admin/candidates?activityId=${activityId}`),
    create: (data: any) => post('/api/admin/candidates', data),
    update: (id: number, data: any) => put(`/api/admin/candidates/${id}`, data),
    delete: (id: number) => del(`/api/admin/candidates/${id}`),
    get: (id: number) => get(`/api/admin/candidates/${id}`),
    batchImport: (data: any) => post('/api/admin/candidates/batch-import', data),
    downloadTemplate: () => downloadRequest('/api/admin/candidates/template'),
    updateOrder: (id: number, orderNum: number) => putWithParams(`/api/admin/candidates/${id}/order`, { orderNum })
  },
  
  // 投票管理
  votes: {
    records: (params?: any) => get('/api/votes/records', params),
    statistics: (activityId?: number) => get(`/api/votes/statistics${activityId ? `?activityId=${activityId}` : ''}`),
    vote: (data: any) => post('/api/votes', data)
  },

  // 统计分析
  statistics: {
    activity: (activityId: number) => get(`/api/admin/statistics/${activityId}`),
    trends: (activityId: number) => get(`/api/admin/trends/${activityId}`),
    voteRates: (activityId: number) => get(`/api/admin/candidates/vote-rate/${activityId}`),
    participation: (activityId: number, date?: string) => {
      const params = date ? `?date=${date}` : ''
      return get(`/api/admin/participation/${activityId}${params}`)
    }
  },
  
  // 用户管理
  users: {
    list: (params?: any) => get('/api/users', params),
    create: (data: any) => post('/api/users', data),
    update: (id: number, data: any) => put(`/api/users/${id}`, data),
    delete: (id: number) => del(`/api/users/${id}`),
    login: (data: any) => post('/api/auth/login', data),
    logout: () => post('/api/auth/logout')
  },

  // 报表导出
  admin: {
    export: (data: any) => post('/api/admin/export', data)
  },

  // 快速导出
  export: {
    candidates: (activityId: number) => get(`/api/export/simple/candidates/${activityId}`),
    activity: (activityId: number) => get(`/api/export/simple/activity/${activityId}`)
  },

  // 文件上传
  upload: {
    uploadAvatar: (formData: FormData) => uploadRequest('/api/upload/avatar', formData)
  }
}

export default api