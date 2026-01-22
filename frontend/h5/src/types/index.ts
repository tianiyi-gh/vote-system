// 类型定义
export interface Activity {
  id: number
  serviceId: string
  title: string
  subtitle?: string
  coverImage?: string
  region?: string
  status: number
  startTime: string
  endTime: string
  totalVotes?: number
  candidateCount?: number
}

export interface Candidate {
  id: number
  activityId: number
  serviceId: string
  candidateNo?: string
  name: string
  groupName?: string
  description?: string
  avatar?: string
  votes?: number
  totalVotes: number
  ranking: number
  status: number
  createTime: string
}

export interface VoteRequest {
  activityId: number
  candidateId: number
  channel: string
  voterPhone?: string
  voterIp: string
  voterId?: string
  captcha?: string
  captchaKey?: string
}