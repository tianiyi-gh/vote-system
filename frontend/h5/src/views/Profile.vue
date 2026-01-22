<template>
  <div class="profile-page">
    <!-- 导航栏 -->
    <van-nav-bar title="个人中心" fixed />

    <div class="container">
      <!-- 用户信息卡片 -->
      <div class="user-card">
        <div class="user-avatar">
          <van-icon name="user-o" size="40" color="#fff" />
        </div>
        <div class="user-info">
          <div class="user-name">投票用户</div>
          <div class="user-id">ID: {{ userId }}</div>
        </div>
      </div>

      <!-- 投票统计 -->
      <div class="stats-section">
        <div class="stat-item">
          <div class="stat-value">{{ todayVotes }}</div>
          <div class="stat-label">今日投票</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-value">{{ totalVotes }}</div>
          <div class="stat-label">累计投票</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-value">{{ voteRecords.length }}</div>
          <div class="stat-label">投票记录</div>
        </div>
      </div>

      <!-- 功能列表 -->
      <div class="menu-section">
        <van-cell-group>
          <van-cell
            title="我的投票记录"
            is-link
            @click="showVoteRecords"
          >
            <template #icon>
              <van-icon name="bill-o" color="#1989fa" />
            </template>
          </van-cell>
          <van-cell
            title="投票规则"
            is-link
            @click="showRules"
          >
            <template #icon>
              <van-icon name="description" color="#ff976a" />
            </template>
          </van-cell>
          <van-cell
            title="清除缓存"
            @click="clearCache"
          >
            <template #icon>
              <van-icon name="delete" color="#ee0a24" />
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <!-- 设置 -->
      <div class="menu-section">
        <van-cell-group>
          <van-cell
            title="关于我们"
            is-link
            @click="showAbout"
          >
            <template #icon>
              <van-icon name="info-o" color="#07c160" />
            </template>
          </van-cell>
          <van-cell
            title="版本信息"
            :value="version"
          >
            <template #icon>
              <van-icon name="apps-o" color="#969799" />
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <!-- 退出登录 -->
      <div class="logout-section">
        <van-button
          type="danger"
          block
          @click="handleLogout"
        >
          清除数据并退出
        </van-button>
      </div>
    </div>

    <!-- 投票记录弹窗 -->
    <van-popup v-model:show="showRecordsPopup" position="bottom" round style="height: 70%">
      <div class="popup-container">
        <div class="popup-header">
          <div class="popup-title">我的投票记录</div>
          <van-icon name="cross" @click="showRecordsPopup = false" />
        </div>
        <div class="records-list">
          <div
            v-for="(record, index) in voteRecords"
            :key="index"
            class="record-item"
          >
            <div class="record-time">{{ formatTime(record.time) }}</div>
            <div class="record-info">
              <div class="record-candidate">{{ record.candidateName }}</div>
              <van-tag type="success" size="small">投票成功</van-tag>
            </div>
          </div>
          <EmptyState
            v-if="voteRecords.length === 0"
            text="暂无投票记录"
            description="去投票页为候选人投票吧"
          >
            <template #icon>
              <van-icon name="bill-o" size="60" color="#dcdee0" />
            </template>
          </EmptyState>
        </div>
      </div>
    </van-popup>

    <!-- 规则弹窗 -->
    <van-popup v-model:show="showRulesPopup" position="bottom" round style="height: 80%">
      <div class="popup-container">
        <div class="popup-header">
          <div class="popup-title">投票规则</div>
          <van-icon name="cross" @click="showRulesPopup = false" />
        </div>
        <div class="rules-content">
          <div class="rule-item">
            <h4>📅 投票时间</h4>
            <p>活动开始至结束期间，每日 00:00-23:59 可进行投票</p>
          </div>
          <div class="rule-item">
            <h4>👤 投票限制</h4>
            <p>• 每个用户每天限投 1 票</p>
            <p>• 每位候选人每天最多接收 1 票</p>
            <p>• 同一 IP 地址每小时限投 1 票</p>
          </div>
          <div class="rule-item">
            <h4>🚫 禁止行为</h4>
            <p>• 严禁使用任何刷票工具或程序</p>
            <p>• 严禁虚假注册、恶意刷票</p>
            <p>• 违规用户将被取消投票资格</p>
          </div>
          <div class="rule-item">
            <h4>✅ 投票方式</h4>
            <p>• 点击候选人进入详情页</p>
            <p>• 完成滑块验证码验证</p>
            <p>• 点击投票按钮完成投票</p>
          </div>
          <div class="rule-item">
            <h4>📊 结果公示</h4>
            <p>投票结果将在活动结束后公示</p>
            <p>排名将根据总票数从高到低排列</p>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 关于弹窗 -->
    <van-dialog v-model:show="showAboutDialog" title="关于我们" show-cancel-button>
      <div class="about-content">
        <p>投票系统 v1.0.0</p>
        <p>一个简单易用的在线投票平台</p>
        <p>支持多候选人、多轮次投票</p>
        <p class="contact-info">如有问题请联系客服</p>
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import dayjs from 'dayjs'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()

// 响应式数据
const userId = ref('')
const version = ref('v1.0.0')
const todayVotes = ref(0)
const totalVotes = ref(0)
const voteRecords = ref<Array<{
  time: string
  candidateName: string
  candidateId: number
  activityTitle: string
}>>([])
const showRecordsPopup = ref(false)
const showRulesPopup = ref(false)
const showAboutDialog = ref(false)

// 计算属性
const voteRecordsFiltered = computed(() => {
  // 只显示最近30天的记录
  const thirtyDaysAgo = dayjs().subtract(30, 'day')
  return voteRecords.value.filter(record =>
    dayjs(record.time).isAfter(thirtyDaysAgo)
  )
})

// 方法
const loadUserData = () => {
  // 加载用户ID
  const storedId = localStorage.getItem('vote_user_id')
  userId.value = storedId || '未生成'

  // 加载投票统计
  const storedTodayVotes = localStorage.getItem('vote_today_count')
  const storedTotalVotes = localStorage.getItem('vote_total_count')
  const storedRecords = localStorage.getItem('vote_records')

  if (storedTodayVotes) {
    todayVotes.value = parseInt(storedTodayVotes)
  }
  if (storedTotalVotes) {
    totalVotes.value = parseInt(storedTotalVotes)
  }
  if (storedRecords) {
    try {
      voteRecords.value = JSON.parse(storedRecords)
    } catch (e) {
      voteRecords.value = []
    }
  }

  // 检查今日是否需要重置
  const lastVoteDate = localStorage.getItem('vote_last_date')
  const today = dayjs().format('YYYY-MM-DD')
  if (lastVoteDate !== today) {
    todayVotes.value = 0
    localStorage.setItem('vote_today_count', '0')
  }
}

const showVoteRecords = () => {
  showRecordsPopup.value = true
}

const showRules = () => {
  showRulesPopup.value = true
}

const showAbout = () => {
  showAboutDialog.value = true
}

const clearCache = () => {
  showConfirmDialog({
    title: '确认清除',
    message: '是否清除所有缓存数据？',
  })
    .then(() => {
      // 清除缓存（保留用户ID和投票记录）
      localStorage.removeItem('vote_today_count')
      localStorage.removeItem('vote_total_count')
      showToast('缓存已清除')
      loadUserData()
    })
    .catch(() => {})
}

const handleLogout = () => {
  showConfirmDialog({
    title: '确认退出',
    message: '退出后将清除所有数据，是否继续？',
  })
    .then(() => {
      localStorage.clear()
      showToast('已退出')
      router.push('/')
    })
    .catch(() => {})
}

const formatTime = (time: string) => {
  return dayjs(time).format('MM-DD HH:mm:ss')
}

// 生命周期
onMounted(() => {
  loadUserData()
})
</script>

<style scoped lang="scss">
.profile-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  padding-top: 46px;

  .container {
    padding: 16px;
  }

  .user-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    color: white;
    margin-bottom: 16px;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

    .user-avatar {
      width: 64px;
      height: 64px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .user-info {
      flex: 1;

      .user-name {
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 4px;
      }

      .user-id {
        font-size: 13px;
        opacity: 0.9;
      }
    }
  }

  .stats-section {
    background: white;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    justify-content: space-around;
    align-items: center;
    margin-bottom: 16px;

    .stat-item {
      text-align: center;

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #323233;
        margin-bottom: 4px;
      }

      .stat-label {
        font-size: 12px;
        color: #969799;
      }
    }

    .stat-divider {
      width: 1px;
      height: 40px;
      background: #ebedf0;
    }
  }

  .menu-section {
    background: white;
    border-radius: 12px;
    margin-bottom: 16px;
    overflow: hidden;
  }

  .logout-section {
    margin-top: 24px;
  }

  .popup-container {
    height: 100%;
    display: flex;
    flex-direction: column;

    .popup-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid #ebedf0;

      .popup-title {
        font-size: 16px;
        font-weight: bold;
        color: #323233;
      }
    }

    .records-list {
      flex: 1;
      overflow-y: auto;
      padding: 16px;

      .record-item {
        padding: 12px;
        border-bottom: 1px solid #ebedf0;

        &:last-child {
          border-bottom: none;
        }

        .record-time {
          font-size: 12px;
          color: #969799;
          margin-bottom: 4px;
        }

        .record-info {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .record-candidate {
            font-size: 14px;
            color: #323233;
          }
        }
      }

      .empty-records {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 60px 16px;
        color: #969799;
      }
    }

    .rules-content {
      flex: 1;
      overflow-y: auto;
      padding: 16px;

      .rule-item {
        margin-bottom: 20px;

        h4 {
          font-size: 15px;
          color: #323233;
          margin-bottom: 8px;
        }

        p {
          font-size: 13px;
          color: #646566;
          line-height: 1.6;
          margin: 4px 0;
        }
      }
    }
  }

  .about-content {
    padding: 16px 0;
    text-align: center;

    p {
      font-size: 14px;
      color: #646566;
      margin: 8px 0;
    }

    .contact-info {
      color: #1989fa;
      font-weight: bold;
    }
  }
}
</style>
