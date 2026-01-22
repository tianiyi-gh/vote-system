<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="icon" color="#409eff" :size="40"><TrendCharts /></el-icon>
            <div class="content">
              <div class="value">{{ stats.totalActivities }}</div>
              <div class="label">总活动数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="icon" color="#67c23a" :size="40"><User /></el-icon>
            <div class="content">
              <div class="value">{{ stats.totalCandidates }}</div>
              <div class="label">总候选人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="icon" color="#e6a23c" :size="40"><DataLine /></el-icon>
            <div class="content">
              <div class="value">{{ stats.totalVotes.toLocaleString() }}</div>
              <div class="label">总投票数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="icon" color="#f56c6c" :size="40"><Operation /></el-icon>
            <div class="content">
              <div class="value">{{ stats.activeCount }}</div>
              <div class="label">进行中活动</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近活动</span>
          </template>
          <el-table :data="recentActivities" style="width: 100%" v-loading="activitiesLoading">
            <el-table-column prop="title" label="活动名称" min-width="150" />
            <el-table-column prop="region" label="区域" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '进行中' : '已结束' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>投票趋势</span>
          </template>
          <div ref="trendChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import api from '@/api'
import { ElMessage } from 'element-plus'
import { TrendCharts, User, DataLine, Operation } from '@element-plus/icons-vue'

const stats = reactive({
  totalActivities: 0,
  totalCandidates: 0,
  totalVotes: 0,
  activeCount: 0
})

const recentActivities = ref([])
const activitiesLoading = ref(false)
const trendChartRef = ref()
let trendChartInstance: any = null

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await fetch('/api/admin/overview').then(res => res.json())
    if (response.code === 200) {
      stats.totalActivities = response.data.totalActivities || 0
      stats.totalCandidates = response.data.totalCandidates || 0
      stats.totalVotes = response.data.totalVotes || 0
      stats.activeCount = response.data.activeActivities || 0
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

// 加载最近活动
const loadRecentActivities = async () => {
  activitiesLoading.value = true
  try {
    const response = await api.activities.list()
    if (response.code === 200) {
      const activities = response.data || []
      // 取最近5个活动
      recentActivities.value = activities.slice(0, 5).map((a: any) => ({
        title: a.title,
        region: a.region || '-',
        status: a.status || 0
      }))
    }
  } catch (error) {
    console.error('加载活动列表失败:', error)
    ElMessage.error('加载活动列表失败')
  } finally {
    activitiesLoading.value = false
  }
}

// 绘制投票趋势图
const drawTrendChart = () => {
  if (!trendChartRef.value || !trendChartInstance) {
    trendChartInstance = echarts.init(trendChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '投票数',
        type: 'line',
        data: [120, 132, 101, 134, 90, 230, 210],
        smooth: true,
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      }
    ]
  }

  trendChartInstance.setOption(option)
}

onMounted(async () => {
  await Promise.all([
    loadStats(),
    loadRecentActivities()
  ])

  await nextTick()
  drawTrendChart()

  // 窗口大小变化时重绘图表
  window.addEventListener('resize', () => {
    trendChartInstance?.resize()
  })
})
</script>

<style scoped lang="scss">
.dashboard {
  .stat-card {
    display: flex;
    align-items: center;
    gap: 20px;

    .icon {
      flex-shrink: 0;
    }

    .content {
      flex: 1;

      .value {
        font-size: 28px;
        font-weight: bold;
        margin-bottom: 8px;
      }

      .label {
        font-size: 14px;
        color: #999;
      }
    }
  }
}
</style>
