<template>
  <div class="search-page">
    <!-- 导航栏 -->
    <van-nav-bar 
      title="搜索" 
      left-arrow 
      @click-left="$router.back()"
      fixed
    />

    <!-- 搜索框 -->
    <div class="search-container">
      <van-search
        v-model="searchKeyword"
        placeholder="搜索活动或候选人..."
        show-action
        @search="handleSearch"
        @cancel="$router.back()"
      />
    </div>

    <!-- 搜索历史 -->
    <div v-if="!hasSearched && searchHistory.length > 0" class="history-section">
      <div class="section-header">
        <span>搜索历史</span>
        <van-icon name="delete-o" @click="clearHistory" />
      </div>
      <div class="history-tags">
        <van-tag
          v-for="(item, index) in searchHistory"
          :key="index"
          plain
          type="default"
          @click="searchWithHistory(item)"
        >
          {{ item }}
        </van-tag>
      </div>
    </div>

    <!-- 热门搜索 -->
    <div v-if="!hasSearched" class="hot-section">
      <div class="section-header">热门搜索</div>
      <div class="hot-tags">
        <van-tag
          v-for="(item, index) in hotSearches"
          :key="index"
          :type="index < 3 ? 'danger' : 'default'"
          plain
          @click="searchWithHistory(item)"
        >
          {{ item }}
        </van-tag>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="hasSearched" class="results-container">
      <!-- 活动结果 -->
      <div v-if="activityResults.length > 0" class="result-section">
        <div class="section-header">相关活动</div>
        <div
          v-for="activity in activityResults"
          :key="activity.id"
          class="activity-result"
          @click="goToVote(activity)"
        >
          <van-image
            :src="activity.coverImage || '/default-activity.png'"
            width="60"
            height="60"
            fit="cover"
          />
          <div class="result-info">
            <div class="result-title">{{ activity.title }}</div>
            <div class="result-desc">{{ activity.subtitle || activity.description }}</div>
            <van-tag :type="getStatusType(activity.status)" size="small">
              {{ getStatusText(activity.status) }}
            </van-tag>
          </div>
        </div>
      </div>

      <!-- 候选人结果 -->
      <div v-if="candidateResults.length > 0" class="result-section">
        <div class="section-header">相关候选人</div>
        <div
          v-for="candidate in candidateResults"
          :key="candidate.id"
          class="candidate-result"
          @click="goToCandidate(candidate)"
        >
          <van-image
            :src="candidate.avatar || '/default-avatar.png'"
            width="50"
            height="50"
            fit="cover"
            round
          />
          <div class="result-info">
            <div class="result-title">{{ candidate.name }}</div>
            <div class="result-desc">{{ candidate.description }}</div>
            <div class="result-meta">
              <span>{{ candidate.totalVotes || 0 }} 票</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 无结果 -->
      <EmptyState
        v-if="activityResults.length === 0 && candidateResults.length === 0"
        text="未找到相关内容"
        description="尝试使用其他关键词搜索"
      >
        <template #icon>
          <van-icon name="search" size="60" color="#dcdee0" />
        </template>
        <template #action>
          <van-button type="primary" size="small" @click="$router.back()">
            返回
          </van-button>
        </template>
      </EmptyState>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import api from '@/api'
import type { Activity, Candidate } from '@/types'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const searchKeyword = ref('')
const hasSearched = ref(false)
const searchHistory = ref<string[]>([])
const hotSearches = ref([
  '年度评选',
  '企业家',
  '领军人物',
  '十大',
  '创新',
  '科技',
  '创业'
])
const activityResults = ref<Activity[]>([])
const candidateResults = ref<Candidate[]>([])
const loading = ref(false)

// 方法
const loadSearchHistory = () => {
  const history = localStorage.getItem('search_history')
  if (history) {
    searchHistory.value = JSON.parse(history)
  }
}

const saveSearchHistory = (keyword: string) => {
  const history = searchHistory.value.filter(item => item !== keyword)
  history.unshift(keyword)
  if (history.length > 10) {
    history.pop()
  }
  searchHistory.value = history
  localStorage.setItem('search_history', JSON.stringify(history))
}

const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('search_history')
  showToast('历史记录已清除')
}

const searchWithHistory = (keyword: string) => {
  searchKeyword.value = keyword
  handleSearch()
}

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    showToast('请输入搜索关键词')
    return
  }

  loading.value = true
  hasSearched.value = true
  activityResults.value = []
  candidateResults.value = []

  try {
    // 搜索活动
    const activitiesResponse = await api.get('/api/activities')
    const allActivities = activitiesResponse.data.data || []

    activityResults.value = allActivities.filter((activity: Activity) => {
      const title = (activity.title || '').toLowerCase()
      const subtitle = (activity.subtitle || '').toLowerCase()
      const desc = (activity.description || '').toLowerCase()
      const kw = keyword.toLowerCase()
      return title.includes(kw) || subtitle.includes(kw) || desc.includes(kw)
    })

    // 搜索候选人（从第一个活动获取）
    if (allActivities.length > 0) {
      const firstActivity = allActivities[0]
      try {
        const candidatesResponse = await api.get(`/api/activities/service/${firstActivity.serviceId}/candidates`)
        const allCandidates = candidatesResponse.data.data || []

        candidateResults.value = allCandidates.filter((candidate: Candidate) => {
          const name = (candidate.name || '').toLowerCase()
          const desc = (candidate.description || '').toLowerCase()
          const kw = keyword.toLowerCase()
          return name.includes(kw) || desc.includes(kw)
        })
      } catch (e) {
        // 忽略候选人搜索错误
      }
    }

    // 保存搜索历史
    saveSearchHistory(keyword)

  } catch (error) {
    showToast('搜索失败')
  } finally {
    loading.value = false
  }
}

const goToVote = (activity: Activity) => {
  if (activity.status === 2) {
    showToast('活动已结束')
    return
  }
  const activityId = activity.serviceId || String(activity.id)
  router.push(`/vote/${activityId}`)
}

const goToCandidate = (candidate: Candidate) => {
  router.push(`/candidate/${candidate.serviceId}/${candidate.id}`)
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
  // 检查URL参数
  const q = route.query.q as string
  if (q) {
    searchKeyword.value = decodeURIComponent(q)
    handleSearch()
  } else {
    loadSearchHistory()
  }
})
</script>

<style scoped lang="scss">
.search-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  padding-top: 46px;

  .search-container {
    background: white;
    padding: 8px 16px;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    font-size: 14px;
    font-weight: bold;
    color: #323233;
  }

  .history-section, .hot-section {
    background: white;
    margin: 8px 0;

    .history-tags, .hot-tags {
      padding: 0 16px 16px;
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .van-tag {
        cursor: pointer;
      }
    }
  }

  .results-container {
    padding: 8px;
  }

  .result-section {
    background: white;
    margin-bottom: 8px;
    padding: 16px;
    border-radius: 8px;

    .activity-result, .candidate-result {
      display: flex;
      gap: 12px;
      padding: 12px;
      border-bottom: 1px solid #ebedf0;
      cursor: pointer;

      &:last-child {
        border-bottom: none;
      }

      &:active {
        background: #f7f8fa;
      }

      .result-info {
        flex: 1;
        min-width: 0;

        .result-title {
          font-size: 15px;
          font-weight: bold;
          color: #323233;
          margin-bottom: 4px;
        }

        .result-desc {
          font-size: 13px;
          color: #969799;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .result-meta {
          font-size: 12px;
          color: #323233;
        }
      }
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 16px;
    text-align: center;

    .empty-text {
      color: #969799;
      margin: 16px 0 24px;
    }
  }
}
</style>
