import axios from 'axios'

// API基础URL配置
const API_BASE_URL = import.meta.env.DEV
  ? '' // 开发环境使用Vite代理
  : 'http://10.19.95.128:8082' // 生产环境直接访问

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Cache-Control': 'no-cache',
    'Pragma': 'no-cache'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    console.log('API Request:', config.method?.toUpperCase(), config.url)
    return config
  },
  (error) => {
    console.error('API Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    console.log('API Response:', response.data)
    return response
  },
  (error) => {
    console.error('API Response Error:', error)
    return Promise.reject(error)
  }
)

export default api
