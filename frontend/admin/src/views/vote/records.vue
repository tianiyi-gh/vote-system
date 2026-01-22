<template>
  <div class="vote-records">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>投票记录</span>
          <el-button type="primary" @click="exportRecords">导出记录</el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="选择活动">
            <el-select
              v-model="searchForm.activityId"
              placeholder="请选择活动"
              filterable
              clearable
              @change="handleActivityChange"
              style="width: 200px"
            >
              <el-option
                v-for="activity in activityList"
                :key="activity.id"
                :label="activity.title"
                :value="activity.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="候选人">
            <el-select
              v-model="searchForm.candidateId"
              placeholder="请选择候选人"
              clearable
              style="width: 150px"
              :disabled="!searchForm.activityId"
            >
              <el-option
                v-for="candidate in candidateList"
                :key="candidate.id"
                :label="candidate.name"
                :value="candidate.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="投票人手机">
            <el-input v-model="searchForm.voterPhone" placeholder="请输入手机号" clearable />
          </el-form-item>
          <el-form-item label="投票渠道">
            <el-select v-model="searchForm.channel" placeholder="请选择渠道" clearable style="width: 120px">
              <el-option label="网络投票" value="WEB" />
              <el-option label="短信投票" value="SMS" />
              <el-option label="微信投票" value="WECHAT" />
              <el-option label="APP投票" value="APP" />
              <el-option label="IVR投票" value="IVR" />
            </el-select>
          </el-form-item>
          <el-form-item label="有效性">
            <el-select v-model="searchForm.valid" placeholder="请选择" clearable style="width: 100px">
              <el-option label="有效" :value="1" />
              <el-option label="无效" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 360px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" :index="(index) => (pagination.current - 1) * pagination.size + index + 1" />
        <el-table-column label="候选人" min-width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-avatar :size="40" :src="getCandidateAvatar(row)" />
              <div>
                <div style="font-weight: 500;">{{ row.candidateName }}</div>
                <div style="font-size: 12px; color: #909399;">编号: {{ row.candidateNo }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="voterPhone" label="投票人手机" width="130" />
        <el-table-column prop="voterIp" label="投票IP" width="140" />
        <el-table-column prop="channel" label="投票渠道" width="100">
          <template #default="{ row }">
            <el-tag :type="getChannelType(row.channel)">
              {{ getChannelText(row.channel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="voteTime" label="投票时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.voteTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="valid" label="有效性" width="80">
          <template #default="{ row }">
            <el-tag :type="row.valid === 1 ? 'success' : 'danger'">
              {{ row.valid === 1 ? '有效' : '无效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceInfo" label="设备信息" width="150" show-overflow-tooltip />
        <el-table-column prop="location" label="位置信息" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="投票记录详情" width="600px">
      <el-descriptions :column="2" border v-if="currentRecord">
        <el-descriptions-item label="投票ID">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="活动ID">{{ currentRecord.activityId }}</el-descriptions-item>
        <el-descriptions-item label="候选人姓名">{{ currentRecord.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="候选人ID">{{ currentRecord.candidateId }}</el-descriptions-item>
        <el-descriptions-item label="投票人手机">{{ currentRecord.voterPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="投票IP">{{ currentRecord.voterIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="投票渠道">
          <el-tag :type="getChannelType(currentRecord.channel)">
            {{ getChannelText(currentRecord.channel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="有效性">
          <el-tag :type="currentRecord.valid === 1 ? 'success' : 'danger'">
            {{ currentRecord.valid === 1 ? '有效' : '无效' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="投票时间" :span="2">{{ formatTime(currentRecord.voteTime) }}</el-descriptions-item>
        <el-descriptions-item label="设备信息" :span="2">{{ currentRecord.deviceInfo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="位置信息" :span="2">{{ currentRecord.location || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import api from '@/api'
import type { Activity, Candidate } from '@/api/types'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const activityList = ref<Activity[]>([])
const candidateList = ref<Candidate[]>([])
const detailVisible = ref(false)
const currentRecord = ref<any>(null)

// 搜索表单
const searchForm = reactive({
  activityId: null as number | null,
  candidateId: null as number | null,
  voterPhone: '',
  channel: '',
  valid: null as number | null,
  dateRange: [] as string[]
})

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 加载活动列表
const loadActivities = async () => {
  try {
    const response = await api.activities.list()
    activityList.value = response.data || []
  } catch (error) {
    console.error('加载活动列表失败:', error)
  }
}

// 加载候选人列表
const loadCandidates = async (activityId: number) => {
  try {
    const response = await api.candidates.list(activityId)
    candidateList.value = response.data || []
  } catch (error) {
    console.error('加载候选人列表失败:', error)
  }
}

// 活动变更时加载候选人
const handleActivityChange = (activityId: number) => {
  searchForm.candidateId = null
  if (activityId) {
    loadCandidates(activityId)
  } else {
    candidateList.value = []
  }
  pagination.current = 1
  loadData()
}

// 加载投票记录
const loadData = async () => {
  if (!searchForm.activityId) {
    ElMessage.warning('请先选择活动')
    return
  }

  loading.value = true
  try {
    const offset = (pagination.current - 1) * pagination.size
    const response = await api.votes.records({
      activityId: searchForm.activityId,
      page: pagination.current - 1,
      size: pagination.size
    })

    console.log('投票记录响应:', response)

    if (response.data) {
      // 映射数据，确保字段名正确
      tableData.value = response.data.map((item: any) => ({
        ...item,
        candidateName: item.candidateName || item.candidate_name || '',
        candidateAvatar: item.candidateAvatar || item.candidate_avatar || '',
        candidateNo: item.candidateNo || item.candidate_no || 0
      }))
      pagination.total = response.data.length
    } else {
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载投票记录失败:', error)
    ElMessage.error('加载投票记录失败')
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.activityId = null
  searchForm.candidateId = null
  searchForm.voterPhone = ''
  searchForm.channel = ''
  searchForm.valid = null
  searchForm.dateRange = []
  candidateList.value = []
  pagination.current = 1
  tableData.value = []
  pagination.total = 0
}

const getChannelType = (channel: string) => {
  const types: Record<string, string> = {
    WEB: 'primary',
    SMS: 'success',
    WECHAT: 'warning',
    APP: 'info',
    IVR: ''
  }
  return types[channel] || ''
}

const getChannelText = (channel: string) => {
  const texts: Record<string, string> = {
    WEB: '网络',
    SMS: '短信',
    WECHAT: '微信',
    APP: 'APP',
    IVR: 'IVR'
  }
  return texts[channel] || channel
}

const formatTime = (time: any) => {
  if (!time) return '-'
  try {
    return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
  } catch {
    return '-'
  }
}

const getCandidateAvatar = (row: any) => {
  if (row.candidateAvatar) {
    // 如果已经是完整URL，直接返回
    if (row.candidateAvatar.startsWith('http://') || row.candidateAvatar.startsWith('https://')) {
      return row.candidateAvatar
    }
    // 否则通过代理访问
    return row.candidateAvatar
  }
  return '/default-avatar.png'
}

const viewDetail = (row: any) => {
  currentRecord.value = row
  detailVisible.value = true
}

const exportRecords = async () => {
  if (!searchForm.activityId) {
    ElMessage.warning('请先选择活动')
    return
  }

  try {
    // 导出CSV
    let csv = '\uFEFF'
    csv += '序号,候选人姓名,投票人手机,投票IP,投票渠道,投票时间,有效性,设备信息,位置信息\n'

    tableData.value.forEach((row: any, index: number) => {
      csv += [
        index + 1,
        row.candidateName,
        row.voterPhone || '',
        row.voterIp || '',
        getChannelText(row.channel),
        formatTime(row.voteTime),
        row.valid === 1 ? '有效' : '无效',
        row.deviceInfo || '',
        row.location || ''
      ].map(escapeCsv).join(',') + '\n'
    })

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `vote_records_${searchForm.activityId}_${dayjs().format('YYYYMMDD_HHmmss')}.csv`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const escapeCsv = (value: any) => {
  if (value === null || value === undefined) {
    return ''
  }
  const str = String(value)
  if (str.includes(',') || str.includes('"') || str.includes('\n')) {
    return `"${str.replace(/"/g, '""')}"`
  }
  return str
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped lang="scss">
.vote-records {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-area {
    margin-bottom: 20px;

    :deep(.el-form-item) {
      margin-bottom: 12px;
    }
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
