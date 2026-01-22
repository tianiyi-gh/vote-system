<template>
  <div class="vote-page">
    <!-- 导航栏 -->
    <van-nav-bar 
      :title="activity?.title || '投票'" 
      left-arrow 
      @click-left="$router.back()"
      fixed
    />

    <div v-if="loading" class="loading-wrapper">
      <van-skeleton title :row="3" />
      <van-skeleton title :row="3" />
    </div>

    <template v-else-if="activity">
      <!-- 活动信息 -->
      <div class="activity-header">
        <van-image 
          :src="activity.coverImage || '/default-activity.png'"
          width="100%"
          height="200"
          fit="cover"
        />
        <div class="activity-info">
          <h2>{{ activity.title }}</h2>
          <p>{{ activity.subtitle }}</p>
          <div class="activity-meta">
            <van-tag :type="getStatusType(activity.status)">
              {{ getStatusText(activity.status) }}
            </van-tag>
            <span class="time-info">
              {{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}
            </span>
          </div>
        </div>
      </div>

      <!-- 搜索和功能按钮 -->
      <div class="search-section">
        <van-search
          v-model="searchKeyword"
          placeholder="搜索候选人..."
          @search="searchCandidates"
        />
        <div class="action-buttons">
          <div class="ranking-btn" @click="goToRanking">
            <van-icon name="medal" color="#FFD700" size="20" />
            <span>排行榜</span>
          </div>
          <div class="stats-btn" @click="goToStatistics">
            <van-icon name="chart-trending-o" color="#07c160" size="20" />
            <span>数据统计</span>
          </div>
        </div>
      </div>

      <!-- 候选人列表 -->
      <div class="container">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            :finished="finished"
            finished-text="没有更多了"
            :loading="loading"
            @load="onLoad"
          >
            <div
              v-for="(candidate, index) in filteredCandidates"
              :key="candidate.id"
              class="candidate-card list-item-animate"
              :style="{ animationDelay: `${index * 0.05}s` }"
              @click="selectCandidate(candidate)"
            >
              <van-card>
                <template #thumb>
                  <van-image
                    :src="candidate.avatar || '/default-avatar.png'"
                    width="80"
                    height="80"
                    fit="cover"
                    round
                  />
                </template>
                <template #title>
                  <div class="candidate-name">
                    {{ candidate.name }}
                    <van-tag v-if="candidate.ranking && candidate.ranking <= 3" :type="getRankingType(candidate.ranking)" size="small">
                      TOP{{ candidate.ranking }}
                    </van-tag>
                  </div>
                  <div class="candidate-no" v-if="candidate.candidateNo">编号: {{ candidate.candidateNo }}</div>
                </template>
                <template #desc>
                  <div class="candidate-desc">{{ candidate.description || '暂无简介' }}</div>
                  <div class="candidate-stats">
                    <div class="vote-count">{{ (candidate.totalVotes || 0).toLocaleString() }} 票</div>
                    <van-progress
                      :percentage="getVotePercentage(candidate.totalVotes || 0)"
                      :color="getProgressColor(candidate.ranking)"
                      stroke-width="4"
                    />
                  </div>
                </template>
              </van-card>
            </div>
          </van-list>
        </van-pull-refresh>

        <!-- 空状态 -->
        <EmptyState
          v-if="!loading && filteredCandidates.length === 0"
          :text="searchKeyword ? '未找到相关候选人' : '暂无候选人'"
          :description="searchKeyword ? '尝试使用其他关键词搜索' : '当前活动暂无候选人数据'"
        >
          <template #icon>
            <van-icon :name="searchKeyword ? 'search' : 'friends-o'" size="60" color="#dcdee0" />
          </template>
        </EmptyState>
      </div>
    </template>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <van-image 
        src="/error-image.png" 
        class="error-image"
      />
      <div class="error-text">活动不存在或已结束</div>
      <van-button type="primary" @click="$router.push('/')">返回首页</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
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
const refreshing = ref(false)
const finished = ref(false)
const searchKeyword = ref('')

const activity = ref<Activity>()
const candidates = ref<Candidate[]>()

// 计算属性
const filteredCandidates = computed(() => {
  if (!searchKeyword.value) return candidates.value
  return candidates.value.filter(candidate =>
    (candidate.name && candidate.name.includes(searchKeyword.value)) ||
    (candidate.candidateNo && candidate.candidateNo.includes(searchKeyword.value))
  )
})

// 方法
const loadData = async () => {
  console.log('loadData called, loading:', loading.value)
  try {
    const serviceId = route.params.serviceId as string
    console.log('Loading data for serviceId:', serviceId)

    if (!serviceId) {
      console.error('No serviceId in route params')
      showToast('活动ID不存在')
      return
    }

    // 获取活动信息
    const activityData = await fetchActivity(serviceId)
    console.log('Activity loaded:', activityData)

    activity.value = activityData

    if (activity.value && activity.value.status !== 2) {
      // 获取候选人列表
      const candidatesData = await fetchCandidates(serviceId)
      console.log('Candidates loaded (count):', candidatesData.length)
      candidates.value = candidatesData
    } else {
      // 活动已结束或不存在，清空候选人列表
      candidates.value = []
    }
  } catch (error: any) {
    console.error('Load data error:', error)
    showToast('加载失败: ' + (error.message || '未知错误'))
    activity.value = undefined
    candidates.value = []
  } finally {
    console.log('loadData completed, setting loading to false')
    loading.value = false
  }
}

const onLoad = async () => {
  // 数据由loadData在组件挂载时加载，这里标记完成
  finished.value = true
}

const onRefresh = async () => {
  await loadData()
  refreshing.value = false
}

const searchCandidates = () => {
  // 搜索功能由计算属性处理
}

const goToRanking = () => {
  router.push(`/ranking/${route.params.serviceId}`)
}

const goToStatistics = () => {
  router.push(`/statistics/${route.params.serviceId}`)
}

const selectCandidate = (candidate: Candidate) => {
  // 跳转到详情页
  router.push(`/candidate/${activity.value?.serviceId}/${candidate.id}`)
}

const fetchActivity = async (serviceId: string): Promise<Activity> => {
  const response = await api.get(`/api/activities/service/${serviceId}`)
  console.log('Fetch activity response:', response.data)
  return response.data.data
}

const fetchCandidates = async (serviceId: string): Promise<Candidate[]> => {
  const response = await api.get(`/api/activities/service/${serviceId}/candidates`)
  console.log('Fetch candidates response:', response.data)
  return response.data.data
}

const getVotePercentage = (votes: number) => {
  if (!candidates.value || !candidates.value.length) return 0
  const maxVotes = Math.max(...candidates.value.map(c => c.totalVotes || 0))
  if (maxVotes === 0) return 0
  return Math.round((votes / maxVotes) * 100)
}

const getProgressColor = (ranking: number) => {
  const colors = {
    1: '#FFD700',
    2: '#C0C0C0', 
    3: '#CD7F32'
  }
  return colors[ranking] || '#409EFF'
}

const getRankingType = (ranking: number) => {
  const types = { 1: 'warning', 2: 'success', 3: 'info' }
  return types[ranking] || ''
}

const formatTime = (time: string) => {
  return dayjs(time).format('MM-DD HH:mm')
}

const getStatusType = (status: number) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'default'
}

const getStatusText = (status: number) => {
  const texts = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return texts[status] || '未知'
}

// 生命周期
onMounted(() => {
  console.log('Vote component mounted')
  loadData()
})

// 监听路由变化，当从其他页面返回时重新加载数据
watch(() => route.params.serviceId, (newServiceId, oldServiceId) => {
  console.log('Route params changed - newServiceId:', newServiceId, 'oldServiceId:', oldServiceId)
  if (newServiceId) {
    loading.value = true
    loadData()
  }
}, { immediate: true })
</script>

<style scoped lang="scss">
.vote-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  padding-bottom: 80px;

  .loading-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200px;
  }

  .activity-header {
    background: white;
    
    .activity-info {
      padding: 16px;
      
      h2 {
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 8px;
      }
      
      p {
        font-size: 14px;
        color: #969799;
        margin-bottom: 12px;
      }
      
      .activity-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .time-info {
          font-size: 12px;
          color: #969799;
        }
      }
    }
  }

  .search-section {
    background: white;
    padding: 8px 16px;
    margin: 8px 0;
    position: relative;

    .action-buttons {
      position: absolute;
      right: 16px;
      top: 50%;
      transform: translateY(-50%);
      display: flex;
      gap: 8px;

      .ranking-btn, .stats-btn {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 6px 12px;
        border-radius: 16px;
        font-size: 13px;
        font-weight: bold;
        color: white;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
        cursor: pointer;
        transition: all 0.3s;

        &:active {
          transform: scale(0.95);
        }
      }

      .ranking-btn {
        background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
      }

      .stats-btn {
        background: linear-gradient(135deg, #07c160 0%, #06ae56 100%);
      }
    }
  }

  .candidate-card {
    margin-bottom: 12px;
    border-radius: 8px;
    overflow: hidden;
    transition: all 0.3s ease;
    cursor: pointer;

    &:active {
      transform: scale(0.98);
    }

    &.candidate-card-hover:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
    }
    
    .candidate-name {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 4px;
    }
    
    .candidate-no {
      font-size: 12px;
      color: #969799;
      margin-bottom: 8px;
    }
    
    .candidate-desc {
      font-size: 14px;
      color: #646566;
      margin-bottom: 12px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
    
    .candidate-stats {
      .vote-count {
        font-size: 16px;
        font-weight: bold;
        color: #ee0a24;
        margin-bottom: 8px;
      }
    }
  }

  .error-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 400px;

    .error-image {
      width: 120px;
      height: 120px;
      margin-bottom: 16px;
    }

    .error-text {
      color: #969799;
      margin-bottom: 16px;
    }
  }
}
</style>