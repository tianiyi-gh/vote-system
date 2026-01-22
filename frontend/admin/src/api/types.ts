/**
 * API 类型定义
 */

// 活动类型
export interface Activity {
  id: number
  title: string
  description?: string
  startTime: string
  endTime: string
  status: number // 1-进行中，2-已结束，3-已取消
  voteLimit: number
  candidateCount: number
  totalVotes: number
  createdBy?: string
  createdAt: string
  updatedAt: string
}

// 候选人类型
export interface Candidate {
  id: number
  activityId: number
  name: string
  description?: string
  avatar?: string
  votes: number
  orderNum: number
  status: number // 1-正常，2-禁用
  createdAt: string
  updatedAt: string
}

// 投票记录类型
export interface VoteRecord {
  id: number
  activityId: number
  candidateId: number
  voterName?: string
  voterPhone?: string
  voterIp?: string
  channel: string // SMS/IVR/WEB/APP/WECHAT/PAY
  voteTime: string
  valid: number // 1-有效，0-无效
  deviceInfo?: string
  location?: string
}

// 用户类型
export interface User {
  id: number
  username: string
  realName?: string
  phone?: string
  email?: string
  role: string // ADMIN/USER
  status: number // 1-正常，0-禁用
  lastLoginTime?: string
  createdAt: string
  updatedAt: string
}

// 统计数据类型
export interface Statistics {
  totalVotes: number
  validVotes: number
  invalidVotes: number
  uniqueVoters: number
  peakHour?: number
  peakVotes?: number
  dailyStats?: Array<{
    date: string
    votes: number
  }>
  candidateStats?: Array<{
    candidateName: string
    votes: number
    percentage: number
  }>
}

// API 响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: string
}

// 分页类型
export interface PageParams {
  page: number
  size: number
  total?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 登录类型
export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: User
}