<template>
  <div class="statistics-page">
    <!-- 导航栏 -->
    <van-nav-bar
      title="数据统计"
      left-arrow
      @click-left="$router.back()"
      fixed
    />

    <div v-if="loading" class="loading-wrapper">
      <van-skeleton title :row="3" />
      <van-skeleton title :row="3" />
      <van-skeleton title :row="3" />
    </div>

    <template v-else-if="activity">
      <!-- 活动信息 -->
      <div class="activity-header">
        <h2>{{ activity.title }}</h2>
        <p>活动时间: {{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}</p>
      </div>

      <!-- 总体统计 -->
      <div class="stats-section">
        <h3>总体数据</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ (totalVotes || 0).toLocaleString() }}</div>
            <div class="stat-label">总票数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ candidates.length }}</div>
            <div class="stat-label">候选人数量</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ calculateAverageVotes().toLocaleString() }}</div>
            <div class="stat-label">平均票数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ (candidates[0]?.totalVotes || 0).toLocaleString() }}</div>
            <div class="stat-label">最高票数</div>
          </div>
        </div>
      </div>

      <!-- TOP 10 柱状图 -->
      <div class="chart-section">
        <h3>票数排名 TOP 10</h3>
        <div class="bar-chart">
          <div
            v-for="(item, index) in topCandidates"
            :key="item.id"
            class="bar-item"
          >
            <div class="bar-wrapper">
              <div
                class="bar"
                :style="{
                  width: getBarWidth(item.totalVotes) + '%',
                  background: getBarColor(index)
                }"
              >
                <span class="bar-value">{{ (item.totalVotes || 0).toLocaleString() }}</span>
              </div>
            </div>
            <div class="bar-label">
              <div class="rank">{{ index + 1 }}</div>
              <div class="name">{{ item.name }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 得票率分布 -->
      <div class="chart-section">
        <h3>得票率分布</h3>
        <div class="pie-chart">
          <div class="pie-chart-container">
            <svg viewBox="0 0 100 100" class="pie-svg">
              <circle
                v-for="(segment, index) in pieData"
                :key="index"
                cx="50"
                cy="50"
                r="40"
                fill="transparent"
                :stroke="segment.color"
                :stroke-width="20"
                :stroke-dasharray="`${segment.value} ${100 - segment.value}`"
                :stroke-dashoffset="getPieOffset(index)"
              />
            </svg>
          </div>
          <div class="pie-legend">
            <div
              v-for="(segment, index) in pieData"
              :key="index"
              class="legend-item"
            >
              <div class="legend-color" :style="{ background: segment.color }"></div>
              <div class="legend-text">
                <div class="legend-label">{{ segment.label }}</div>
                <div class="legend-value">{{ segment.count }}人 ({{ segment.percentage }}%)</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 前三名对比 -->
      <div class="chart-section">
        <h3>TOP 3 对比</h3>
        <div class="comparison-chart">
          <div
            v-for="(item, index) in topThree"
            :key="item.id"
            class="comparison-item"
            :class="`rank-${index + 1}`"
          >
            <div class="crown-icon">{{ index === 0 ? '🥇' : index === 1 ? '🥈' : '🥉' }}</div>
            <div class="comparison-avatar">
              <van-image
                :src="item.avatar || '/default-avatar.png'"
                width="60"
                height="60"
                fit="cover"
                round
              />
            </div>
            <div class="comparison-info">
              <div class="comparison-name">{{ item.name }}</div>
              <div class="comparison-votes">{{ (item.totalVotes || 0).toLocaleString() }} 票</div>
              <div class="comparison-percentage">{{ getVotePercentage(item.totalVotes) }}%</div>
            </div>
            <div class="comparison-bar">
              <div
                class="comparison-bar-fill"
                :style="{
                  width: getVotePercentage(item.totalVotes) + '%',
                  background: getBarColor(index)
                }"
              />
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <EmptyState
        text="活动不存在"
        description="请检查活动链接是否正确"
      >
        <template #icon>
          <van-icon name="warning-o" size="60" color="#dcdee0" />
        </template>
      </EmptyState>
      <van-button type="primary" @click="$router.push('/')">返回首页</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import dayjs from 'dayjs'
import api from '@/api'
import type { Activity, Candidate } from '@/types'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const activity = ref<Activity>()
const candidates = ref<Candidate[]>([])

// 计算属性
const topCandidates = computed(() => {
  return [...candidates.value].sort((a, b) => (b.totalVotes || 0) - (a.totalVotes || 0)).slice(0, 10)
})

const topThree = computed(() => topCandidates.value.slice(0, 3))

const totalVotes = computed(() => {
  return candidates.value.reduce((sum, c) => sum + (c.totalVotes || 0), 0)
})

const pieData = computed(() => {
  const total = totalVotes.value
  if (total === 0) return []

  const ranges = [
    { min: 0, max: 100, label: '0-100票', color: '#95de64' },
    { min: 101, max: 500, label: '101-500票', color: '#3ba272' },
    { min: 501, max: 1000, label: '501-1000票', color: '#36a3eb' },
    { min: 1001, max: Infinity, label: '1000+票', color: '#f37b1d' }
  ]

  return ranges.map(range => {
    const count = candidates.value.filter(c => {
      const votes = c.totalVotes || 0
      return votes >= range.min && votes <= range.max
    }).length

    const percentage = ((count / candidates.value.length) * 100).toFixed(1)

    return {
      ...range,
      count,
      value: (count / candidates.value.length) * 100,
      percentage
    }
  }).filter(d => d.count > 0)
})

// 方法
const loadData = async () => {
  loading.value = true
  try {
    const { serviceId } = route.params

    // 获取活动数据
    const activityData = await api.get(`/api/activities/service/${serviceId}`)
    activity.value = activityData.data.data

    // 获取候选人数据
    const candidatesData = await api.get(`/api/activities/service/${serviceId}/candidates`)
    const allCandidates = candidatesData.data.data || []

    // 按票数排序
    candidates.value = allCandidates.sort((a, b) => (b.totalVotes || 0) - (a.totalVotes || 0))
  } catch (error) {
    console.error('Load statistics error:', error)
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (time: string) => {
  return dayjs(time).format('MM-DD HH:mm')
}

const calculateAverageVotes = () => {
  if (candidates.value.length === 0) return 0
  const total = totalVotes.value
  return Math.round(total / candidates.value.length)
}

const getBarWidth = (votes: number) => {
  const maxVotes = topCandidates.value[0]?.totalVotes || 0
  if (maxVotes === 0) return 0
  return ((votes || 0) / maxVotes) * 100
}

const getBarColor = (index: number) => {
  const colors = ['#FF6B6B', '#FF9F43', '#FFD700', '#3BA272', '#36A3EB']
  return colors[index % colors.length]
}

const getVotePercentage = (votes: number) => {
  const total = totalVotes.value
  if (total === 0) return '0.00'
  return ((votes || 0) / total * 100).toFixed(2)
}

const getPieOffset = (index: number) => {
  let offset = 0
  for (let i = 0; i < index; i++) {
    offset += pieData.value[i]?.value || 0
  }
  return -offset
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.statistics-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  padding-bottom: 20px;

  .loading-wrapper {
    padding-top: 100px;
  }

  .activity-header {
    background: white;
    padding: 16px;
    margin-bottom: 12px;

    h2 {
      font-size: 18px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 8px;
    }

    p {
      font-size: 14px;
      color: #969799;
    }
  }

  .stats-section, .chart-section {
    background: white;
    margin-bottom: 12px;
    padding: 16px;

    h3 {
      font-size: 16px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 16px;
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;

    .stat-card {
      padding: 16px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      text-align: center;

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: white;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.8);
      }
    }
  }

  .bar-chart {
    .bar-item {
      display: flex;
      align-items: center;
      margin-bottom: 12px;

      .bar-wrapper {
        flex: 1;
        padding-right: 12px;

        .bar {
          height: 24px;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: flex-end;
          padding-right: 8px;
          transition: width 0.5s ease;

          .bar-value {
            color: white;
            font-size: 12px;
            font-weight: bold;
          }
        }
      }

      .bar-label {
        min-width: 60px;

        .rank {
          font-size: 12px;
          color: #969799;
          margin-bottom: 2px;
        }

        .name {
          font-size: 14px;
          color: #323233;
          font-weight: 500;
        }
      }
    }
  }

  .pie-chart {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;

    .pie-chart-container {
      width: 200px;
      height: 200px;
    }

    .pie-svg {
      width: 100%;
      height: 100%;
      transform: rotate(-90deg);
    }

    .pie-legend {
      display: grid;
      gap: 12px;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;

        .legend-color {
          width: 16px;
          height: 16px;
          border-radius: 4px;
        }

        .legend-text {
          .legend-label {
            font-size: 14px;
            color: #323233;
            margin-bottom: 2px;
          }

          .legend-value {
            font-size: 12px;
            color: #969799;
          }
        }
      }
    }
  }

  .comparison-chart {
    .comparison-item {
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
      background: #f7f8fa;
      border-radius: 12px;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .crown-icon {
        font-size: 48px;
      }

      .comparison-avatar {
        flex-shrink: 0;
      }

      .comparison-info {
        flex: 1;

        .comparison-name {
          font-size: 16px;
          font-weight: bold;
          color: #323233;
          margin-bottom: 4px;
        }

        .comparison-votes {
          font-size: 20px;
          color: #ff6b6b;
          font-weight: bold;
          margin-bottom: 4px;
        }

        .comparison-percentage {
          font-size: 14px;
          color: #969799;
        }
      }

      .comparison-bar {
        width: 120px;
        height: 8px;
        background: #ebedf0;
        border-radius: 4px;
        overflow: hidden;

        .comparison-bar-fill {
          height: 100%;
          border-radius: 4px;
          transition: width 0.5s ease;
        }
      }
    }

    &.rank-1 {
      background: linear-gradient(135deg, #fff4d 0%, #ffd700 20%);
    }

    &.rank-2 {
      background: linear-gradient(135deg, #f5f5f5 0%, #c0c0c0 20%);
    }

    &.rank-3 {
      background: linear-gradient(135deg, #f5f5f5 0%, #cd7f32 20%);
    }
  }

  .error-state {
    padding: 60px 20px;
    text-align: center;
  }
}
</style>
