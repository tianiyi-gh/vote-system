<template>
  <div class="ranking-page">
    <!-- 导航栏 -->
    <van-nav-bar
      title="排行榜"
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
        <p>{{ activity.subtitle }}</p>
      </div>

      <!-- 搜索栏 -->
      <div class="search-section">
        <van-search
          v-model="searchKeyword"
          placeholder="搜索候选人..."
          @search="handleSearch"
          background="white"
        />
      </div>

      <!-- 排行榜 -->
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <div class="ranking-content">
          <!-- 前三名展示 -->
          <div class="top-three" v-if="displayedCandidates.length > 0">
            <div
              v-for="item in topThree"
              :key="item.id"
              class="top-item"
              :class="`rank-${item.ranking}`"
            >
              <div class="crown" v-if="item.ranking === 1">🥇</div>
              <div class="crown" v-else-if="item.ranking === 2">🥈</div>
              <div class="crown" v-else-if="item.ranking === 3">🥉</div>

              <div class="avatar-wrapper">
                <van-image
                  :src="item.avatar || '/default-avatar.png'"
                  width="70"
                  height="70"
                  fit="cover"
                  round
                />
                <div class="rank-badge">{{ item.ranking }}</div>
              </div>

              <div class="candidate-name">{{ item.name }}</div>
              <div class="vote-count">{{ (item.totalVotes || 0).toLocaleString() }} 票</div>
            </div>
          </div>

          <!-- 完整排名列表 -->
          <div class="ranking-list">
            <div
              v-for="(item, index) in displayedOtherCandidates"
              :key="item.id"
              class="ranking-item list-item-animate"
              :style="{ animationDelay: `${(index + 3) * 0.05}s` }"
              @click="goToDetail(item)"
            >
              <div class="rank-number" :class="{ 'in-top-three': item.ranking <= 3 }">
                {{ item.ranking }}
              </div>
              <van-image
                :src="item.avatar || '/default-avatar.png'"
                width="50"
                height="50"
                fit="cover"
                round
              />
              <div class="candidate-info">
                <div class="name">{{ item.name }}</div>
                <div class="no" v-if="item.candidateNo">编号: {{ item.candidateNo }}</div>
              </div>
              <div class="vote-info">
                <div class="count">{{ (item.totalVotes || 0).toLocaleString() }}</div>
                <div class="unit">票</div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <EmptyState
            v-if="!loading && displayedCandidates.length === 0"
            :text="searchKeyword ? '未找到相关候选人' : '暂无排名数据'"
            :description="searchKeyword ? '尝试使用其他关键词搜索' : '当前活动暂无排名数据'"
          >
            <template #icon>
              <van-icon :name="searchKeyword ? 'search' : 'trend-o'" size="60" color="#dcdee0" />
            </template>
          </EmptyState>
        </div>
      </van-pull-refresh>
    </template>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <van-empty description="活动不存在" />
      <van-button type="primary" @click="$router.push('/')">返回首页</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import api from '@/api'
import type { Activity, Candidate } from '@/types'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const refreshing = ref(false)
const activity = ref<Activity>()
const candidates = ref<Candidate[]>([])
const searchKeyword = ref('')

// 计算属性
const topThree = computed(() => {
  return displayedCandidates.value.slice(0, 3)
})

const displayedOtherCandidates = computed(() => {
  return displayedCandidates.value.slice(3)
})

const displayedCandidates = computed(() => {
  if (!searchKeyword.value) return candidates.value

  return candidates.value.filter(candidate =>
    (candidate.name && candidate.name.includes(searchKeyword.value)) ||
    (candidate.candidateNo && candidate.candidateNo.includes(searchKeyword.value))
  )
})

// 方法
const loadData = async () => {
  try {
    const serviceId = route.params.serviceId as string
    console.log('Ranking loadData for serviceId:', serviceId)

    // 获取活动信息
    activity.value = await fetchActivity(serviceId)

    // 获取候选人排名
    candidates.value = await fetchRanking(serviceId)
  } catch (error: any) {
    console.error('Load data error:', error)
    showToast('加载失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const onRefresh = async () => {
  searchKeyword.value = ''
  await loadData()
  refreshing.value = false
}

const handleSearch = () => {
  // 搜索功能由计算属性处理
  console.log('Search for:', searchKeyword.value)
}

const goToDetail = (candidate: Candidate) => {
  router.push(`/candidate/${activity.value?.serviceId}/${candidate.id}`)
}

const fetchActivity = async (serviceId: string): Promise<Activity> => {
  const response = await api.get(`/api/activities/service/${serviceId}`)
  return response.data.data
}

const fetchRanking = async (serviceId: string): Promise<Candidate[]> => {
  const response = await api.get(`/api/activities/service/${serviceId}/ranking`)
  return response.data.data
}

// 生命周期
onMounted(() => {
  loadData()
})

// 监听路由变化
watch(() => route.params.serviceId, (newServiceId) => {
  if (newServiceId) {
    searchKeyword.value = ''
    loading.value = true
    loadData()
  }
})
</script>

<style scoped lang="scss">
.ranking-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  padding-top: 46px;

  .loading-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200px;
  }

  .activity-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 20px 16px;
    text-align: center;
    color: white;

    h2 {
      font-size: 20px;
      font-weight: bold;
      margin-bottom: 8px;
    }

    p {
      font-size: 14px;
      opacity: 0.9;
    }
  }

  .search-section {
    background: white;
    padding: 12px 16px;
    margin-bottom: 8px;
  }

  .ranking-content {
    margin-top: -16px;
    border-radius: 16px 16px 0 0;
    background: white;
    padding-top: 20px;
  }

  .top-three {
    display: flex;
    justify-content: center;
    align-items: flex-end;
    padding: 60px 16px 40px;
    gap: 16px;
    background: linear-gradient(180deg, rgba(102, 126, 234, 0.1) 0%, rgba(255, 255, 255, 0) 100%);
    border-radius: 16px;

    .top-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      position: relative;
      min-width: 100px;

      &.rank-1 {
        transform: scale(1.15);
        z-index: 3;
        order: 2;
      }

      &.rank-2 {
        transform: scale(1.05);
        z-index: 2;
        order: 1;
      }

      &.rank-3 {
        transform: scale(1.05);
        z-index: 1;
        order: 3;
      }

      .crown {
        font-size: 32px;
        margin-bottom: 8px;
        animation: bounce 2s ease-in-out infinite;
        filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
      }

      .avatar-wrapper {
        position: relative;
        display: flex;
        justify-content: center;
      }

      .rank-badge {
        position: absolute;
        bottom: -6px;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: bold;
        color: white;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
        border: 2px solid white;
      }

      &.rank-1 .rank-badge {
        background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
        width: 28px;
        height: 28px;
        font-size: 14px;
      }

      &.rank-2 .rank-badge {
        background: linear-gradient(135deg, #C0C0C0 0%, #A8A8A8 100%);
      }

      &.rank-3 .rank-badge {
        background: linear-gradient(135deg, #CD7F32 0%, #B87333 100%);
      }

      .candidate-name {
        margin-top: 16px;
        font-size: 14px;
        font-weight: bold;
        color: #323233;
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        text-align: center;
      }

      .vote-count {
        margin-top: 8px;
        font-size: 16px;
        font-weight: bold;
        color: #ee0a24;
      }
    }
  }

  .ranking-list {
    padding: 0 16px 20px;

    .ranking-item {
      display: flex;
      align-items: center;
      padding: 12px;
      background: white;
      border-radius: 12px;
      margin-bottom: 12px;
      transition: all 0.3s;
      cursor: pointer;

      &:active {
        transform: scale(0.98);
      }

      .rank-number {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        font-weight: bold;
        background: #f0f0f0;
        color: #999;
        margin-right: 12px;

        &.in-top-three {
          color: white;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
      }

      .candidate-info {
        flex: 1;
        margin-left: 12px;
        overflow: hidden;

        .name {
          font-size: 15px;
          font-weight: 500;
          color: #323233;
          margin-bottom: 4px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .no {
          font-size: 12px;
          color: #969799;
        }
      }

      .vote-info {
        text-align: right;

        .count {
          font-size: 18px;
          font-weight: bold;
          color: #ee0a24;
        }

        .unit {
          font-size: 12px;
          color: #969799;
        }
      }
    }
  }

  .empty-state,
  .error-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 16px;
  }

  @keyframes bounce {
    0%, 100% {
      transform: translateY(0) scale(1);
    }
    50% {
      transform: translateY(-6px) scale(1.05);
    }
  }
}

</style>
