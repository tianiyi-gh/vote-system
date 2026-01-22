<template>
  <van-action-sheet
    v-model:show="show"
    :actions="actions"
    cancel-text="取消"
    close-on-click-action
    @select="handleSelect"
  >
    <template #description>
      <div class="share-description">
        <div class="candidate-info">
          <van-image
            :src="candidate.avatar || '/default-avatar.png'"
            width="60"
            height="60"
            fit="cover"
            round
          />
          <div class="info-text">
            <div class="name">{{ candidate.name }}</div>
            <div class="votes">{{ (candidate.totalVotes || 0).toLocaleString() }} 票</div>
          </div>
        </div>
        <div class="share-tip">为 {{ candidate.name }} 拉票吧！</div>
      </div>
    </template>
  </van-action-sheet>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { showToast, showDialog } from 'vant'
import type { Candidate } from '@/types'
import { setShareData, configWeChatSDK, isWeChat } from '@/utils/wechat'

interface Props {
  show: boolean
  candidate: Candidate
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
}>()

const show = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value)
})

// 检查是否已配置微信 JSSDK
const isWeChatConfigured = ref(false)

// 组件加载时尝试配置微信 JSSDK
onMounted(async () => {
  if (isWeChat()) {
    try {
      const success = await configWeChatSDK()
      if (success) {
        isWeChatConfigured.value = true
        console.log('微信 JSSDK 配置成功')
      }
    } catch (error) {
      console.warn('微信 JSSDK 配置失败，将使用基础分享功能:', error)
    }
  }
})

const actions = [
  { name: '分享到微信', icon: 'wechat' },
  { name: '分享到朋友圈', icon: 'wechat-moments' },
  { name: '复制链接', icon: 'link' },
  { name: '生成海报', icon: 'photo' }
]

const handleSelect = (action: any) => {
  switch (action.name) {
    case '复制链接':
      handleCopyLink()
      break
    case '生成海报':
      handleGeneratePoster()
      break
    case '分享到微信':
      handleWeChatShare()
      break
    case '分享到朋友圈':
      handleMomentsShare()
      break
  }
}

const handleCopyLink = () => {
  const link = `${window.location.origin}/candidate/${props.candidate.activityId}/${props.candidate.id}`
  const shareText = `为 ${props.candidate.name} 投票！TA目前有 ${props.candidate.totalVotes || 0} 票，快去支持一下吧！${link}`

  // 优先使用现代的 Clipboard API
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(shareText).then(() => {
      showToast('链接已复制到剪贴板')
    }).catch(() => {
      // 降级方案：使用传统方法
      fallbackCopyText(shareText)
    })
  } else {
    // 降级方案：使用传统方法
    fallbackCopyText(shareText)
  }
}

const fallbackCopyText = (text: string) => {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()

  try {
    const successful = document.execCommand('copy')
    if (successful) {
      showToast('链接已复制到剪贴板')
    } else {
      showToast('复制失败，请手动复制')
    }
  } catch (err) {
    showToast('复制失败，请手动复制')
  }

  document.body.removeChild(textarea)
}

const handleWeChatShare = async () => {
  // 检测是否在微信环境中
  const inWeChat = isWeChat()

  if (inWeChat) {
    // 如果已配置 JSSDK，设置分享内容
    if (isWeChatConfigured.value) {
      setShareData({
        title: `为 ${props.candidate.name} 投票！`,
        desc: `TA目前有 ${props.candidate.totalVotes || 0} 票，快去支持一下吧！`,
        link: `${window.location.origin}/candidate/${props.candidate.activityId}/${props.candidate.id}`,
        imgUrl: props.candidate.avatar || '/default-avatar.png'
      })
      showToast('请点击右上角"..."菜单分享')
    } else {
      // 未配置 JSSDK，提示用户
      showDialog({
        title: '分享到微信',
        message: '请点击右上角 "..." 菜单，选择"发送给朋友"进行分享',
        showCancelButton: false,
        confirmButtonText: '我知道了'
      })
    }
  } else {
    // 非微信环境，提示复制链接
    showDialog({
      title: '分享到微信',
      message: '请先复制链接，然后在微信中粘贴分享',
      showCancelButton: true,
      confirmButtonText: '复制链接',
      cancelButtonText: '取消'
    }).then((action: any) => {
      if (action === 'confirm') {
        handleCopyLink()
      }
    })
  }
}

const handleMomentsShare = async () => {
  // 检测是否在微信环境中
  const inWeChat = isWeChat()

  if (inWeChat) {
    // 如果已配置 JSSDK，设置分享内容
    if (isWeChatConfigured.value) {
      setShareData({
        title: `为 ${props.candidate.name} 投票！TA目前有 ${props.candidate.totalVotes || 0} 票`,
        desc: '', // 朋友圈不显示描述
        link: `${window.location.origin}/candidate/${props.candidate.activityId}/${props.candidate.id}`,
        imgUrl: props.candidate.avatar || '/default-avatar.png'
      })
      showToast('请点击右上角"..."菜单分享到朋友圈')
    } else {
      // 未配置 JSSDK，提示用户
      showDialog({
        title: '分享到朋友圈',
        message: '请点击右上角 "..." 菜单，选择"分享到朋友圈"进行分享',
        showCancelButton: false,
        confirmButtonText: '我知道了'
      })
    }
  } else {
    // 非微信环境，提示使用海报
    showDialog({
      title: '分享到朋友圈',
      message: '朋友圈只能分享图片，是否生成海报？',
      showCancelButton: true,
      confirmButtonText: '生成海报',
      cancelButtonText: '取消'
    }).then((action: any) => {
      if (action === 'confirm') {
        handleGeneratePoster()
      }
    })
  }
}

const handleGeneratePoster = async () => {
  try {
    // 生成海报逻辑
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    if (!ctx) return

    const width = 750
    const height = 1200
    canvas.width = width
    canvas.height = height

    // 绘制渐变背景
    const gradient = ctx.createLinearGradient(0, 0, width, height)
    gradient.addColorStop(0, '#667eea')
    gradient.addColorStop(1, '#764ba2')
    ctx.fillStyle = gradient
    ctx.fillRect(0, 0, width, height)

    // 绘制白色卡片背景
    ctx.fillStyle = '#ffffff'
    ctx.roundRect(40, 60, width - 80, 1000, 20)
    ctx.fill()

    // 绘制标题
    ctx.fillStyle = '#333333'
    ctx.font = 'bold 48px sans-serif'
    ctx.textAlign = 'center'
    ctx.fillText('为 TA 投票', width / 2, 140)

    // 绘制头像
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.src = props.candidate.avatar || '/default-avatar.png'

    await new Promise<void>((resolve) => {
      img.onload = () => {
        ctx.save()
        ctx.beginPath()
        ctx.arc(width / 2, 340, 150, 0, Math.PI * 2)
        ctx.clip()
        ctx.drawImage(img, width / 2 - 150, 190, 300, 300)
        ctx.restore()
        resolve()
      }
      img.onerror = () => resolve()
    })

    // 绘制姓名
    ctx.fillStyle = '#333333'
    ctx.font = 'bold 40px sans-serif'
    ctx.textAlign = 'center'
    ctx.fillText(props.candidate.name, width / 2, 580)

    // 绘制票数
    ctx.fillStyle = '#ff6b6b'
    ctx.font = 'bold 48px sans-serif'
    ctx.fillText(`${(props.candidate.totalVotes || 0).toLocaleString()} 票`, width / 2, 660)

    // 绘制分割线
    ctx.strokeStyle = '#e0e0e0'
    ctx.lineWidth = 2
    ctx.beginPath()
    ctx.moveTo(80, 720)
    ctx.lineTo(width - 80, 720)
    ctx.stroke()

    // 绘制提示文字
    ctx.fillStyle = '#999999'
    ctx.font = '28px sans-serif'
    ctx.fillText('长按识别二维码参与投票', width / 2, 800)

    // 绘制活动二维码占位符
    ctx.fillStyle = '#f5f5f5'
    ctx.fillRect(200, 840, 350, 350)

    ctx.fillStyle = '#333333'
    ctx.font = '32px sans-serif'
    ctx.fillText('二维码区域', width / 2, 1020)

    // 转换为图片
    canvas.toBlob((blob) => {
      if (blob) {
        const url = URL.createObjectURL(blob)
        // 打开图片
        const a = document.createElement('a')
        a.href = url
        a.download = `${props.candidate.name}_海报.png`
        a.click()
        URL.revokeObjectURL(url)
        showToast('海报已下载')
      }
    })
  } catch (error) {
    showToast('海报生成失败')
    console.error('生成海报错误:', error)
  }
}
</script>

<style scoped lang="scss">
.share-description {
  padding: 20px 0;

  .candidate-info {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    .info-text {
      flex: 1;

      .name {
        font-size: 16px;
        font-weight: bold;
        color: #323233;
        margin-bottom: 4px;
      }

      .votes {
        font-size: 14px;
        color: #ff6b6b;
        font-weight: 500;
      }
    }
  }

  .share-tip {
    text-align: center;
    font-size: 14px;
    color: #969799;
    padding: 12px;
    background: #f7f8fa;
    border-radius: 8px;
  }
}

:deep(.van-action-sheet__item) {
  font-size: 16px;
}

:deep(.van-action-sheet__icon) {
  font-size: 20px;
  margin-right: 12px;
}
</style>
