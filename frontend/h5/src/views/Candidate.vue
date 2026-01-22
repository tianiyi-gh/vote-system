<template>
  <div class="candidate-page">
    <!-- 导航栏 -->
    <van-nav-bar 
      title="候选人详情" 
      left-arrow 
      @click-left="$router.back()"
      fixed
    />

    <div v-if="loading" class="loading-wrapper">
      <van-loading size="24px" vertical>加载中...</van-loading>
    </div>

    <template v-else-if="candidate">
      <!-- 候选人基本信息 -->
      <div class="candidate-header">
        <van-image
          :src="candidate.avatar || '/default-avatar.png'"
          width="100%"
          height="250"
          fit="cover"
        />
          <div class="candidate-info">
            <h1>{{ candidate.name }}</h1>
            <div class="candidate-meta">
              <van-tag :type="getRankingType(candidate.ranking)" size="large">
                第{{ candidate.ranking }}名
              </van-tag>
              <span v-if="candidate.candidateNo" class="candidate-no">编号: {{ candidate.candidateNo }}</span>
            </div>
          
          <!-- 投票数据 -->
          <div class="vote-stats">
            <div class="stats-item">
              <div class="stats-value">{{ (candidate.totalVotes || 0).toLocaleString() }}</div>
              <div class="stats-label">总票数</div>
            </div>
            <div class="stats-divider"></div>
            <div class="stats-item">
              <div class="stats-value">{{ getVotePercentage() }}%</div>
              <div class="stats-label">得票率</div>
            </div>
            <div class="stats-divider"></div>
            <div class="stats-item">
              <div class="stats-value">{{ getCandidateGap() }}</div>
              <div class="stats-label">与前一名差距</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 候选人简介 -->
      <div class="section-card">
        <h3>个人简介</h3>
        <div class="candidate-desc">
          {{ candidate.description || '暂无简介信息' }}
        </div>
      </div>

      <!-- 投票进度 -->
      <div class="section-card">
        <h3>投票进度</h3>
        <van-progress
          :percentage="parseFloat(getVotePercentage())"
          :color="getProgressColor(candidate.ranking)"
          stroke-width="12"
          :pivot-text="getVotePercentage() + '%'"
          pivot-color="#323233"
          text-color="#323233"
        />
        <div class="progress-text">
          当前得票率: {{ getVotePercentage() }}% / 总票数: {{ getTotalVotes().toLocaleString() }}票
        </div>
      </div>

      <!-- 实时排名 -->
      <div class="section-card" v-if="topCandidates.length > 0">
        <h3>实时排名 TOP10</h3>
        <div class="ranking-list">
          <div 
            v-for="(item, index) in topCandidates" 
            :key="item.id"
            class="ranking-item"
            :class="{ 
              'current-candidate': item.id === candidate.id,
              'top-three': index < 3
            }"
          >
            <div class="ranking-number">
              <van-tag v-if="index < 3" :type="getRankingType(index + 1)" size="small">
                {{ index + 1 }}
              </van-tag>
              <span v-else>{{ index + 1 }}</span>
            </div>
            <van-image
              :src="item.avatar || '/default-avatar.png'"
              width="40"
              height="40"
              fit="cover"
              round
            />
            <div class="candidate-name">
              {{ item.name }}
              <van-tag 
                v-if="item.id === candidate.id" 
                type="danger" 
                size="small"
              >
                我
              </van-tag>
            </div>
            <div class="candidate-votes">{{ item.totalVotes.toLocaleString() }}票</div>
          </div>
        </div>
      </div>
    </template>

    <!-- 错误状态 -->
    <div v-else class="error-state">
      <van-image 
        src="/error-image.png" 
        class="error-image"
      />
      <div class="error-text">候选人不存在</div>
      <van-button type="primary" @click="$router.back()">返回</van-button>
    </div>

    <!-- 投票和分享按钮 -->
    <div v-if="candidate" class="action-buttons-container">
      <button class="vote-btn-large" @click="showVoteDialog">
        <span class="vote-icon">❤</span>
        <span class="vote-text">为 {{ candidate.name }} 投票</span>
        <span class="vote-arrow">→</span>
      </button>
      <button class="share-btn-large" @click="showShareSheet">
        <span class="share-icon">📤</span>
        <span class="share-text">分享</span>
      </button>
    </div>

    <!-- 投票对话框 -->
    <van-dialog
      v-model:show="showDialog"
      title="确认投票"
      show-cancel-button
      @confirm="handleVote"
    >
      <div class="vote-dialog-content">
        <div class="selected-candidate">
          <van-image
            :src="candidate?.avatar || '/default-avatar.png'"
            width="60"
            height="60"
            fit="cover"
            round
          />
          <div>
            <div class="candidate-name">{{ candidate?.name }}</div>
            <div v-if="candidate?.candidateNo" class="candidate-no">编号: {{ candidate.candidateNo }}</div>
          </div>
        </div>

        <!-- 滑块验证码 -->
        <SliderCaptcha
          ref="sliderCaptchaRef"
          @success="onCaptchaSuccess"
          @fail="onCaptchaFail"
        />
      </div>
    </van-dialog>

    <!-- 分享弹窗 -->
    <ShareSheet
      v-model:show="showShareSheetDialog"
      v-if="candidate"
      :candidate="candidate"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import api from '@/api'
import type { Candidate, Activity } from '@/types'
import SliderCaptcha from '@/components/SliderCaptcha.vue'
import ShareSheet from '@/components/ShareSheet.vue'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const voting = ref(false)
const captchaValid = ref(false)
const showDialog = ref(false)
const showShareSheetDialog = ref(false)
const candidate = ref<Candidate>()
const activity = ref<Activity>()
const topCandidates = ref<Candidate[]>([])
const sliderCaptchaRef = ref()

// 计算属性
const canVote = computed(() => {
  return activity.value && activity.value.status === 1
})

// 方法
const loadData = async () => {
  try {
    const { serviceId, candidateId } = route.params
    console.log('Candidate loadData called with serviceId:', serviceId, 'candidateId:', candidateId)

    // 顺序获取数据，避免并行请求导致后端错误
    const candidateData = await fetchCandidate(serviceId as string, parseInt(candidateId as string))
    console.log('Candidate data loaded:', candidateData)

    const activityData = await fetchActivity(serviceId as string)
    console.log('Activity data loaded:', activityData)

    // 获取TOP候选人列表
    const topCandidatesData = await fetchTopCandidates(serviceId as string, 100)
    console.log('Top candidates loaded (count):', topCandidatesData.length)
    console.log('First 3 candidates:', topCandidatesData.slice(0, 3))

    candidate.value = candidateData
    activity.value = activityData
    topCandidates.value = topCandidatesData

    console.log('Final values set - candidate:', candidate.value, 'activity:', activity.value, 'topCandidates count:', topCandidates.value.length)
  } catch (error) {
    console.error('Load data error:', error)
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

const showVoteDialog = () => {
  // 检查活动状态
  if (!activity.value) {
    showToast('活动不存在')
    return
  }

  if (activity.value.status !== 1) {
    const statusText = activity.value.status === 0 ? '活动尚未开始' : '活动已结束'
    showToast(statusText)
    return
  }

  // 检查活动时间
  const now = new Date()
  const startTime = new Date(activity.value.startTime)
  const endTime = new Date(activity.value.endTime)

  if (now < startTime) {
    showToast('活动尚未开始')
    return
  }

  if (now > endTime) {
    showToast('活动已结束')
    return
  }

  // 重置验证码状态
  captchaValid.value = false

  // 刷新滑块验证码
  if (sliderCaptchaRef.value) {
    sliderCaptchaRef.value.refresh()
  }

  showDialog.value = true
}

const showShareSheet = () => {
  if (!candidate.value) {
    showToast('候选人信息未加载')
    return
  }
  showShareSheetDialog.value = true
}

const onCaptchaSuccess = () => {
  captchaValid.value = true
}

const onCaptchaFail = () => {
  captchaValid.value = false
}

const handleVote = async () => {
  if (!candidate.value || !activity.value) return

  // 检查滑块验证码是否通过
  if (!captchaValid.value) {
    showToast('请先完成滑块验证')
    return
  }

  voting.value = true

  try {
    // 生成用户唯一标识（基于浏览器指纹 + 时间戳）
    const userFingerprint = localStorage.getItem('vote_user_id') || 'user_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    localStorage.setItem('vote_user_id', userFingerprint)

    const voteRequest = {
      activityId: activity.value.id,
      candidateId: candidate.value.id,
      channel: 'WEB',
      captcha: 'SLIDER_PASSED',
      captchaKey: 'slider_' + Date.now(),
      voterId: userFingerprint,
      voterPhone: userFingerprint
    }

    const response = await api.post('/api/votes', voteRequest)

    if (response.data.code === 200) {
      showToast('投票成功！')

      // 更新候选人票数
      candidate.value.totalVotes += 1

      // 关闭对话框
      showDialog.value = false
      captchaValid.value = false

      // 刷新候选人列表以更新排名
      await loadData()
    } else {
      const errorMsg = response.data.message || '投票失败'
      showToast(errorMsg)
      if (sliderCaptchaRef.value) {
        sliderCaptchaRef.value.refresh()
      }
      captchaValid.value = false
    }
  } catch (error: any) {
    const errorMsg = error.response?.data?.message || error.message || '投票失败'
    showToast(errorMsg)
    if (sliderCaptchaRef.value) {
      sliderCaptchaRef.value.refresh()
    }
    captchaValid.value = false
  } finally {
    voting.value = false
  }
}

const goToVote = () => {
  router.push(`/vote/${route.params.serviceId}`)
}

const fetchCandidate = async (serviceId: string, candidateId: number): Promise<Candidate> => {
  const response = await api.get(`/api/activities/service/${serviceId}/candidates/${candidateId}`)
  return response.data.data
}

const fetchActivity = async (serviceId: string): Promise<Activity> => {
  const response = await api.get(`/api/activities/service/${serviceId}`)
  return response.data.data
}

const fetchTopCandidates = async (serviceId: string, limit: number): Promise<Candidate[]> => {
  try {
    console.log('Fetching top candidates for service:', serviceId, 'limit:', limit)

    // 不带排序参数获取所有候选人
    const response = await api.get(`/api/activities/service/${serviceId}/candidates`)
    const candidates = response.data.data || []
    console.log('Top candidates raw response:', candidates)

    if (!candidates || candidates.length === 0) {
      console.log('No candidates found')
      return []
    }

    // 按票数降序排序
    const sortedCandidates = [...candidates].sort((a, b) => {
      const votesA = a.totalVotes || 0
      const votesB = b.totalVotes || 0
      return votesB - votesA
    })

    // 限制返回数量
    const limitedCandidates = sortedCandidates.slice(0, limit)

    // 添加ranking字段
    const result = limitedCandidates.map((c: Candidate, index: number) => ({
      ...c,
      ranking: c.ranking || index + 1
    }))

    console.log('Top candidates processed:', result)
    return result
  } catch (error) {
    console.error('Fetch top candidates error:', error)
    return []
  }
}

const getVotePercentage = () => {
  try {
    console.log('getVotePercentage called')
    console.log('candidate.value:', candidate.value)
    console.log('topCandidates.value:', topCandidates.value)

    if (!candidate.value) {
      console.log('No candidate data')
      return '0.00'
    }

    const currentVotes = candidate.value.totalVotes || 0
    console.log('currentVotes:', currentVotes)

    if (currentVotes === 0) {
      console.log('currentVotes is 0')
      return '0.00'
    }

    // 计算所有候选人的总票数
    if (!topCandidates.value || topCandidates.value.length === 0) {
      console.log('No top candidates data')
      return '0.00'
    }

    const totalVotes = topCandidates.value.reduce((sum, c) => sum + (c.totalVotes || 0), 0)
    console.log('totalVotes calculated:', totalVotes)

    if (totalVotes === 0) {
      console.log('totalVotes is 0')
      return '0.00'
    }

    // 计算得票率（百分比）
    const percentage = (currentVotes / totalVotes) * 100
    console.log('percentage calculated:', percentage)

    return percentage.toFixed(2)
  } catch (error) {
    console.error('Calculate vote percentage error:', error)
    return '0.00'
  }
}

const getCandidateGap = () => {
  try {
    console.log('getCandidateGap called')
    console.log('candidate.value:', candidate.value)
    console.log('topCandidates.value:', topCandidates.value)

    if (!candidate.value || !topCandidates.value || topCandidates.value.length === 0) {
      console.log('Missing data, returning -')
      return '-'
    }

    // 在topCandidates中找到当前候选人的索引
    const currentIndex = topCandidates.value.findIndex(c => c.id === candidate.value.id)
    console.log('currentIndex:', currentIndex)

    if (currentIndex === -1 || currentIndex === 0) {
      console.log('Candidate not found or is first, returning -')
      return '-'
    }

    // 获取前一名的候选人
    const previousCandidate = topCandidates.value[currentIndex - 1]
    console.log('previousCandidate:', previousCandidate)

    if (!previousCandidate) {
      console.log('No previous candidate found')
      return '-'
    }

    const currentVotes = candidate.value.totalVotes || 0
    const previousVotes = previousCandidate.totalVotes || 0
    const gap = previousVotes - currentVotes

    console.log('currentVotes:', currentVotes, 'previousVotes:', previousVotes, 'gap:', gap)

    if (gap <= 0) return '0票'
    return gap.toLocaleString() + '票'
  } catch (error) {
    console.error('Calculate candidate gap error:', error)
    return '-'
  }
}

const getProgressColor = (ranking: number) => {
  const colors = {
    1: '#FFD700',
    2: '#C0C0C0',
    3: '#CD7F32'
  }
  return colors[ranking] || '#409EFF'
}

const getTotalVotes = () => {
  if (!topCandidates.value || !topCandidates.value.length) return 0
  return topCandidates.value.reduce((sum, c) => sum + (c.totalVotes || 0), 0)
}

const getRankingType = (ranking: number) => {
  const types = { 1: 'warning', 2: 'success', 3: 'info' }
  return types[ranking] || ''
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.candidate-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  padding-bottom: 80px;

  .loading-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200px;
  }

  .candidate-header {
    background: white;
    
    .candidate-info {
      padding: 16px;
      
      h1 {
        font-size: 24px;
        font-weight: bold;
        margin-bottom: 12px;
        text-align: center;
      }
      
      .candidate-meta {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 12px;
        margin-bottom: 20px;
        
        .candidate-no, .group-name {
          font-size: 14px;
          color: #969799;
        }
      }
      
      .vote-stats {
        display: flex;
        justify-content: space-around;
        align-items: center;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 12px;
        padding: 20px;
        color: white;
        
        .stats-item {
          text-align: center;
          
          .stats-value {
            font-size: 20px;
            font-weight: bold;
            margin-bottom: 4px;
          }
          
          .stats-label {
            font-size: 12px;
            opacity: 0.9;
          }
        }
        
        .stats-divider {
          width: 1px;
          height: 40px;
          background: rgba(255, 255, 255, 0.3);
        }
      }
    }
  }

  .section-card {
    background: white;
    margin: 8px 16px;
    padding: 16px;
    border-radius: 8px;
    
    h3 {
      font-size: 16px;
      font-weight: bold;
      margin-bottom: 12px;
      color: #323233;
    }
    
    .candidate-desc {
      font-size: 14px;
      line-height: 1.6;
      color: #646566;
    }
    
    .progress-text {
      text-align: center;
      font-size: 12px;
      color: #969799;
      margin-top: 8px;
    }
    
    .ranking-list {
      .ranking-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 0;
        border-bottom: 1px solid #ebedf0;
        
        &:last-child {
          border-bottom: none;
        }
        
        &.current-candidate {
          background: #fef2f2;
          margin: 0 -16px;
          padding-left: 16px;
          padding-right: 16px;
          border-radius: 4px;
        }
        
        &.top-three {
          font-weight: bold;
        }
        
        .ranking-number {
          width: 30px;
          text-align: center;
          font-weight: bold;
        }
        
        .candidate-name {
          flex: 1;
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
        }
        
        .candidate-votes {
          font-size: 14px;
          color: #323233;
          font-weight: bold;
        }
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

  .action-buttons-container {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: white;
    box-shadow: 0 -2px 16px rgba(0, 0, 0, 0.1);
    padding: 12px 16px;
    z-index: 999;
    display: flex;
    gap: 12px;

    .vote-btn-large, .share-btn-large {
      flex: 1;
      max-width: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      padding: 16px 32px;
      border: none;
      border-radius: 28px;
      font-size: 18px;
      font-weight: bold;
      cursor: pointer;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:active {
        transform: scale(0.98);
      }

      &:hover {
        transform: translateY(-2px);
      }
    }

    .vote-btn-large {
      background: linear-gradient(135deg, #ff6b6b 0%, #ee0a24 100%);
      color: white;
      box-shadow: 0 4px 12px rgba(238, 10, 36, 0.3);

      &:hover {
        background: linear-gradient(135deg, #ff5252 0%, #d32f2f 100%);
        box-shadow: 0 6px 16px rgba(238, 10, 36, 0.4);
      }

      .vote-icon {
        font-size: 24px;
        animation: pulse 2s ease-in-out infinite;
      }

      .vote-text {
        text-align: center;
      }

      .vote-arrow {
        font-size: 20px;
      }
    }

    .share-btn-large {
      background: linear-gradient(135deg, #07c160 0%, #06ae56 100%);
      color: white;

      &:hover {
        background: linear-gradient(135deg, #06ae56 0%, #059d4b 100%);
      }

      .share-icon {
        font-size: 24px;
      }

      .share-text {
        text-align: center;
      }
    }
  }

  .vote-dialog-content {
    padding: 16px;

    .selected-candidate {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #ebedf0;

      .candidate-name {
        font-size: 16px;
        font-weight: bold;
        color: #323233;
      }

      .candidate-no {
        font-size: 14px;
        color: #969799;
        margin-top: 4px;
      }
    }
  }

  @keyframes pulse {
    0%, 100% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.1);
    }
  }
}
</style>