<template>
  <div class="home-page">
    <!-- 导航栏 -->
    <van-nav-bar title="投票活动" fixed />
    
    <!-- 轮播图 -->
    <van-notice-bar 
      v-if="notices.length > 0"
      left-icon="volume-o"
      :text="currentNotice"
    />

    <!-- 搜索栏 -->
    <div class="search-section">
      <van-search 
        v-model="searchKeyword" 
        placeholder="搜索活动..."
        @search="handleSearch"
        background="#f7f8fa"
      />
    </div>

    <!-- 活动列表 -->
    <div class="container">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div 
            v-for="activity in activities" 
            :key="activity.id"
            class="activity-card"
            @click="goToVote(activity)"
          >
            <van-card>
              <template #thumb>
                <van-image 
                  :src="activity.coverImage || '/default-activity.png'"
                  width="100%"
                  height="100%"
                  fit="cover"
                />
              </template>
              <template #title>
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-subtitle">{{ activity.subtitle }}</div>
              </template>
              <template #desc>
                <div class="activity-info">
                  <van-tag :type="getStatusType(activity.status)">
                    {{ getStatusText(activity.status) }}
                  </van-tag>
                  <span class="region-text">{{ activity.region }}</span>
                  <span class="time-text">{{ formatTime(activity.endTime) }}结束</span>
                </div>
              </template>
              <template #footer>
                <div class="activity-stats">
                  <span>{{ activity.totalVotes || 0 }}人参与</span>
                  <span>{{ activity.candidateCount || 0 }}个候选人</span>
                </div>
              </template>
            </van-card>
          </div>
        </van-list>
      </van-pull-refresh>

      <!-- 空状态 -->
      <EmptyState
        v-if="!loading && activities.length === 0"
        text="暂无活动"
        description="当前没有进行中的活动，请稍后再来"
      >
        <template #icon>
          <van-icon name="todo-list-o" size="60" color="#dcdee0" />
        </template>
      </EmptyState>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import dayjs from 'dayjs'
import api from '@/api'
import type { Activity } from '@/types'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()

// 响应式数据
const activities = ref<Activity[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const searchKeyword = ref('')
const notices = ref([
  '欢迎参与投票活动，请选择您支持的候选人',
  '投票时间：每日00:00-23:59',
  '每人每天限投1票，严禁刷票行为'
])
const currentNotice = ref('')

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 方法
const onLoad = async () => {
  try {
    // 调用API获取活动列表
    const response = await fetchActivities()

    if (response.length === 0) {
      finished.value = true
    } else {
      // 直接赋值，不要push
      activities.value = response
      finished.value = true
    }
  } catch (error) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

const onRefresh = async () => {
  activities.value = []
  pagination.current = 1
  finished.value = false
  await onLoad()
  refreshing.value = false
}

const handleSearch = () => {
  // 搜索功能
  router.push(`/search?q=${encodeURIComponent(searchKeyword.value)}`)
}

const goToVote = (activity: Activity) => {
  if (activity.status === 2) {
    showToast('活动已结束')
    return
  }

  // 优先使用serviceId，如果没有则使用id
  const activityId = activity.serviceId || String(activity.id)
  router.push(`/vote/${activityId}`)
}

const fetchActivities = async () => {
  const response = await api.get('/api/activities')
  return response.data.data || []
}

const rotateNotices = () => {
  let index = 0
  setInterval(() => {
    currentNotice.value = notices.value[index]
    index = (index + 1) % notices.value.length
  }, 3000)
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
  rotateNotices()
  // 初始加载数据
  if (activities.value.length === 0) {
    loading.value = true
    onLoad()
  }
})

// 页面激活时重新加载（从其他页面返回时）
onActivated(() => {
  console.log('Home page activated, refreshing data...')
  loading.value = true
  activities.value = []
  onLoad()
})
</script>

<style scoped lang="scss">
.home-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  
  .search-section {
    background: white;
    padding: 8px 0;
    margin-bottom: 8px;
  }

  .activity-card {
    margin-bottom: 12px;
    
    .activity-title {
      font-size: 16px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 4px;
    }
    
    .activity-subtitle {
      font-size: 14px;
      color: #969799;
      margin-bottom: 8px;
    }
    
    .activity-info {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .region-text, .time-text {
        font-size: 12px;
        color: #969799;
      }
    }
    
    .activity-stats {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #969799;
    }
  }
}
</style>