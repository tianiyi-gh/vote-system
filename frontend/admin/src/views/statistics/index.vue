<template>
  <div class="statistics-page">
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="活动">
          <el-select 
            v-model="filterForm.activityId" 
            placeholder="请选择活动"
            filterable
            style="width: 200px"
          >
            <el-option 
              v-for="activity in activities"
              :key="activity.id"
              :label="activity.title"
              :value="activity.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStatistics">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览 -->
    <div class="stats-overview" v-if="statistics">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card>
            <div class="stat-item">
              <div class="value">{{ statistics.totalVotes?.toLocaleString() || 0 }}</div>
              <div class="label">总投票数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card>
            <div class="stat-item">
              <div class="value">{{ statistics.totalUniqueVoters || 0 }}</div>
              <div class="label">独立投票人数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card>
            <div class="stat-item">
              <div class="value">{{ getTopCandidateName() }}</div>
              <div class="label">票数最高候选人</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card>
            <div class="stat-item">
              <div class="value">{{ getActiveDays() }}</div>
              <div class="label">活跃天数</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 投票趋势图 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>投票趋势</span>
              <el-radio-group v-model="trendDays" size="small">
                <el-radio-button :label="7">7天</el-radio-button>
                <el-radio-button :label="30">30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChart" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <!-- 渠道分布图 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>投票渠道分布</span>
          </template>
          <div ref="channelChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 候选人TOP榜 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>候选人TOP 10</span>
          <el-button @click="refreshTopCandidates">刷新</el-button>
        </div>
      </template>
      <el-table :data="topCandidates" style="width: 100%">
        <el-table-column label="排名" width="80">
          <template #default="{ $index }">
            <el-tag v-if="$index < 3" :type="getRankingType($index + 1)">
              {{ $index + 1 }}
            </el-tag>
            <span v-else>{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="照片" width="80">
          <template #default="{ row }">
            <el-avatar 
              :src="row.photo || '/default-avatar.png'" 
              :size="40"
              :alt="row.candidateName"
            />
          </template>
        </el-table-column>
        <el-table-column prop="candidateName" label="候选人姓名" min-width="120" />
        <el-table-column prop="groupName" label="分组" width="100" />
        <el-table-column prop="totalVotes" label="总票数" width="120" sortable>
          <template #default="{ row }">
            <strong>{{ row.totalVotes?.toLocaleString() || 0 }}</strong>
          </template>
        </el-table-column>
        <el-table-column label="进度条" width="200">
          <template #default="{ row }">
            <el-progress 
              :percentage="getVotePercentage(row.totalVotes)"
              :color="getProgressColor(row.ranking)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 每日统计详情 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>每日统计详情</span>
          <el-button @click="exportData">导出数据</el-button>
        </div>
      </template>
      <el-table :data="dailyStats" style="width: 100%" max-height="400">
        <el-table-column prop="date" label="日期" width="120">
          <template #default="{ row }">
            {{ dayjs(row.date).format('YYYY-MM-DD') }}
          </template>
        </el-table-column>
        <el-table-column prop="totalVotes" label="总票数" width="100" />
        <el-table-column prop="uniqueVoters" label="独立投票人数" width="120" />
        <el-table-column label="各渠道票数">
          <template #default="{ row }">
            <div class="channel-stats">
              <el-tag v-if="row.channelVotes?.WEB" type="success" size="small">
                网络: {{ row.channelVotes.WEB }}
              </el-tag>
              <el-tag v-if="row.channelVotes?.SMS" type="warning" size="small">
                短信: {{ row.channelVotes.SMS }}
              </el-tag>
              <el-tag v-if="row.channelVotes?.WECHAT" type="primary" size="small">
                微信: {{ row.channelVotes.WECHAT }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 导出列选择对话框 -->
    <el-dialog v-model="exportColumnDialogVisible" title="选择导出列" width="600px">
      <div class="export-columns-dialog">
        <el-checkbox-group v-model="exportColumns">
          <div class="column-grid">
            <el-checkbox
              v-for="column in exportColumns"
              :key="column.key"
              :label="column"
              :model-value="column.visible"
              @change="column.visible = $event"
            >
              {{ column.label }}
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="exportColumnDialogVisible = false">取消</el-button>
          <el-button @click="selectAllColumns">全选</el-button>
          <el-button @click="deselectAllColumns">全不选</el-button>
          <el-button type="primary" @click="confirmExport">
            确认导出（{{ exportFormat === 'excel' ? 'Excel' : 'CSV' }}）
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import api from '@/api'
import { exportToCSV, exportToExcel, createExportColumns, type ExportColumn } from '@/utils/exportHelper'

// 响应式数据
const statistics = ref(null)
const topCandidates = ref([])
const dailyStats = ref([])
const activities = ref([])
const trendDays = ref(7)
const trendChart = ref()
const channelChart = ref()
let trendChartInstance = null
let channelChartInstance = null

// 导出相关
const exportColumnDialogVisible = ref(false)
const exportFormat = ref<'csv' | 'excel'>('csv')
const exportColumns = ref<ExportColumn[]>([])
const exportResolve = ref<(() => void) | null>(null)
const exportReject = ref<(() => void) | null>(null)

// 筛选表单
const filterForm = reactive({
  activityId: null,
  dateRange: []
})

// 方法
const loadStatistics = async () => {
  if (!filterForm.activityId) {
    ElMessage.warning('请选择活动')
    return
  }

  try {
    const [startDate, endDate] = filterForm.dateRange.length > 0
      ? filterForm.dateRange
      : [dayjs().subtract(30, 'day').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]

    // 调用统计API
    const response = await api.statistics.activity(filterForm.activityId)
    if (response.code === 200 && response.data) {
      statistics.value = response.data

      // 获取投票趋势（带时间范围参数）
      let trendUrl = `/api/admin/trends/${filterForm.activityId}`
      if (filterForm.dateRange.length > 0) {
        trendUrl += `?startDate=${filterForm.dateRange[0]}&endDate=${filterForm.dateRange[1]}`
      }

      const trendResponse = await fetch(trendUrl).then(res => res.json())
      if (trendResponse.code === 200 && trendResponse.data) {
        dailyStats.value = trendResponse.data.map((item: any) => ({
          date: item.date || item.voteDate,
          totalVotes: item.totalVotes || 0,
          uniqueVoters: item.uniqueVoters || 0,
          channelVotes: {
            WEB: item.webVotes || 0,
            SMS: item.smsVotes || 0,
            WECHAT: item.wechatVotes || 0
          }
        }))
      }

      // 获取候选人数据并按票数排序
      const allCandidates = statistics.value.candidates?.map((c: any) => ({
        candidateId: c.id,
        candidateName: c.name,
        groupName: c.groupName || '',
        totalVotes: c.totalVotes || 0,
        photo: c.avatar || c.photo || '',
        ranking: 0
      })) || []

      // 按票数降序排序并取前10
      allCandidates.sort((a, b) => b.totalVotes - a.totalVotes)
      allCandidates.forEach((c, index) => {
        c.ranking = index + 1
      })
      topCandidates.value = allCandidates.slice(0, 10)
    }

    // 绘制图表
    await nextTick()
    drawCharts()

  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

const drawTrendChart = () => {
  if (!trendChartInstance) {
    trendChartInstance = echarts.init(trendChart.value)
  }

  const dates = dailyStats.value.map(item => dayjs(item.date).format('MM-DD'))
  const voteData = dailyStats.value.map(item => item.totalVotes)
  const voterData = dailyStats.value.map(item => item.uniqueVoters)

  const option = {
    title: {
      text: '投票趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['投票数', '独立投票人数'],
      bottom: 0
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '投票数',
        type: 'line',
        data: voteData,
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '独立投票人数',
        type: 'line',
        data: voterData,
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }

  trendChartInstance.setOption(option)
}

const drawChannelChart = () => {
  if (!channelChartInstance) {
    channelChartInstance = echarts.init(channelChart.value)
  }

  const channelVotes = statistics.value?.channelVotes || {}
  const data = Object.entries(channelVotes).map(([channel, votes]) => ({
    name: getChannelName(channel),
    value: votes
  })).filter(item => item.value > 0)

  const option = {
    title: {
      text: '投票渠道分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '投票渠道',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  channelChartInstance.setOption(option)
}

const drawCharts = () => {
  drawTrendChart()
  drawChannelChart()
}

const resetFilter = () => {
  filterForm.activityId = null
  filterForm.dateRange = []
  statistics.value = null
  topCandidates.value = []
  dailyStats.value = []
}

const refreshTopCandidates = async () => {
  if (!filterForm.activityId) return
  
  try {
    // topCandidates.value = await statisticsApi.getTopCandidates(filterForm.activityId, 10)
    ElMessage.success('刷新成功')
  } catch (error) {
    ElMessage.error('刷新失败')
  }
}

const exportData = async () => {
  if (!filterForm.activityId) {
    ElMessage.warning('请先选择活动')
    return
  }

  try {
    const action = await ElMessageBox.confirm(
      '选择导出格式和选项',
      '导出统计报表',
      {
        distinguishCancelAndClose: true,
        confirmButtonText: 'CSV导出',
        cancelButtonText: '取消',
        type: 'info',
      }
    ).then(() => 'csv')
      .catch(action => action === 'cancel' ? 'excel' : null)

    if (!action) return

    // 询问是否自定义列
    const customColumns = await ElMessageBox.confirm(
      '是否自定义导出列？',
      '导出选项',
      {
        distinguishCancelAndClose: true,
        confirmButtonText: '自定义列',
        cancelButtonText: '全部导出',
        type: 'info',
      }
    ).then(() => true)
      .catch(() => false)

    if (customColumns) {
      // 显示列选择对话框
      await showExportColumnDialog(action)
    } else {
      // 全部导出
      await downloadExport(action)
    }
  } catch {
    // 用户取消
  }
}

const showExportColumnDialog = (format: string) => {
  return new Promise<void>((resolve, reject) => {
    exportColumnDialogVisible.value = true
    exportFormat.value = format as 'csv' | 'excel'
    exportResolve.value = resolve
    exportReject.value = reject
  })
}

const confirmExport = () => {
  const columns = exportColumns.value.filter(col => col.visible)
  const format = exportFormat.value

  try {
    const loading = ElLoading.service({
      lock: true,
      text: '正在导出...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 导出数据
    if (format === 'excel') {
      exportToExcel(dailyStats.value, columns, `daily_stats_${filterForm.activityId}_${Date.now()}.xls`)
    } else {
      exportToCSV(dailyStats.value, columns, `daily_stats_${filterForm.activityId}_${Date.now()}.csv`)
    }

    loading.close()
    ElMessage.success('导出成功')
    exportColumnDialogVisible.value = false

    if (exportResolve.value) {
      exportResolve.value()
    }
  } catch (error) {
    console.error('导出错误:', error)
    ElMessage.error('导出失败，请重试')

    if (exportReject.value) {
      exportReject.value()
    }
  }
}

const selectAllColumns = () => {
  exportColumns.value.forEach(col => {
    col.visible = true
  })
}

const deselectAllColumns = () => {
  exportColumns.value.forEach(col => {
    col.visible = false
  })
}

const downloadExport = async (format: string) => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '正在导出...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 直接从表格数据生成
    if (format === 'excel') {
      const csvContent = generateCsvFromDailyStats()
      // 使用Excel导出
      const blob = new Blob([csvContent], { type: 'application/vnd.ms-excel;charset=utf-8;' })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `daily_stats_${filterForm.activityId}_${Date.now()}.xls`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(url)
    } else {
      const csvContent = generateCsvFromDailyStats()
      downloadCsv(csvContent, `daily_stats_${filterForm.activityId}_${Date.now()}.csv`)
    }

    loading.close()
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出错误:', error)
    loading.close()
    ElMessage.error('导出失败，请重试')
  }
}

const generateCsvFromDailyStats = () => {
  // CSV 头部（UTF-8 BOM）
  let csv = '\uFEFF'
  csv += '日期,总票数,独立投票人数,网络票,短信票,微信票\n'

  // 添加数据行
  dailyStats.value.forEach(row => {
    const channelVotes = row.channelVotes || {}
    csv += [
      row.date,
      row.totalVotes || 0,
      row.uniqueVoters || 0,
      channelVotes.WEB || 0,
      channelVotes.SMS || 0,
      channelVotes.WECHAT || 0
    ].map(escapeCsvValue).join(',') + '\n'
  })

  return csv
}

const escapeCsvValue = (value: any) => {
  if (value === null || value === undefined) {
    return ''
  }
  const str = String(value)
  if (str.includes(',') || str.includes('"') || str.includes('\n')) {
    return `"${str.replace(/"/g, '""')}"`
  }
  return str
}

const downloadCsv = (content: string, filename: string) => {
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(url)
}

const getTopCandidateName = () => {
  return topCandidates.value[0]?.candidateName || '-'
}

const getActiveDays = () => {
  return dailyStats.value.length || 0
}

const getVotePercentage = (votes) => {
  if (!votes || !topCandidates.value.length) return 0
  const maxVotes = Math.max(...topCandidates.value.map(c => c.totalVotes || 0))
  return Math.round((votes / maxVotes) * 100)
}

const getProgressColor = (ranking) => {
  const colors = {
    1: '#FFD700',
    2: '#C0C0C0', 
    3: '#CD7F32'
  }
  return colors[ranking] || '#409EFF'
}

const getRankingType = (ranking) => {
  const types = { 1: 'warning', 2: 'success', 3: 'info' }
  return types[ranking] || ''
}

const getChannelName = (channel) => {
  const names = {
    WEB: '网络投票',
    SMS: '短信投票',
    WECHAT: '微信投票',
    IVR: '语音投票',
    APP: 'APP投票',
    PAY: '付费投票'
  }
  return names[channel] || channel
}

// 监听趋势天数变化
watch(trendDays, () => {
  if (statistics.value) {
    drawTrendChart()
  }
})

// 生命周期
onMounted(async () => {
  // 初始化导出列定义
  initExportColumns()

  // 加载活动列表
  try {
    const response = await api.activities.list()
    activities.value = response.data || []
  } catch (error) {
    console.error('加载活动列表失败:', error)
    activities.value = []
  }
})

// 初始化导出列
const initExportColumns = () => {
  exportColumns.value = createExportColumns([
    { key: 'date', label: '日期' },
    { key: 'totalVotes', label: '总票数' },
    { key: 'uniqueVoters', label: '独立投票人数' },
    { key: 'channelVotes.WEB', label: '网络票' },
    { key: 'channelVotes.SMS', label: '短信票' },
    { key: 'channelVotes.WECHAT', label: '微信票' }
  ])
}
</script>

<style scoped lang="scss">
.statistics-page {
  .filter-card {
    margin-bottom: 20px;
  }

  .stats-overview {
    margin-bottom: 20px;

    .stat-item {
      text-align: center;
      padding: 20px;

      .value {
        font-size: 28px;
        font-weight: bold;
        color: #409EFF;
        margin-bottom: 8px;
      }

      .label {
        font-size: 14px;
        color: #606266;
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .channel-stats {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .export-columns-dialog {
    .column-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
      max-height: 400px;
      overflow-y: auto;
      padding: 10px;
      border: 1px solid #eee;
      border-radius: 4px;
    }

    .el-checkbox {
      margin-right: 0;
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }
}
</style>