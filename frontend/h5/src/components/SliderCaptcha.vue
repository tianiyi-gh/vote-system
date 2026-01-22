<template>
  <div class="slider-captcha">
    <div class="captcha-container">
      <!-- 背景图（带缺口） -->
      <canvas ref="bgCanvas" :width="width" :height="height"></canvas>

      <!-- 拼图块（移动的滑块） -->
      <canvas
        ref="puzzleCanvas"
        :width="puzzleWidth"
        :height="puzzleHeight"
        class="puzzle-canvas"
        :style="{ left: puzzleX + 'px', top: puzzleY + 'px' }"
      ></canvas>

      <div class="slider-box">
        <div class="slider-track">
          <div
            class="slider-button"
            :style="{ left: sliderX + 'px' }"
            @touchstart="onTouchStart"
            @touchmove="onTouchMove"
            @touchend="onTouchEnd"
            @mousedown="onMouseDown"
            @mousemove="onMouseMove"
            @mouseup="onMouseUp"
          >
            <span class="arrow-icon">→</span>
          </div>
        </div>
        <div class="slider-text">{{ statusText }}</div>
      </div>
    </div>
    <div class="refresh-btn" @click="refresh">
      <span class="refresh-icon">↻</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'

const emit = defineEmits<{
  success: []
  fail: []
}>()

const bgCanvas = ref<HTMLCanvasElement>()
const puzzleCanvas = ref<HTMLCanvasElement>()

const width = 320
const height = 180
const puzzleWidth = 50
const puzzleHeight = 50

const sliderX = ref(0)
const puzzleX = ref(0)
const puzzleY = ref(0)
const initialPuzzleX = ref(0)
const isDragging = ref(false)
const startX = ref(0)
const statusText = ref('向右滑动完成验证')

// 生成随机数
const random = (min: number, max: number) => Math.floor(Math.random() * (max - min) + min)

// 生成随机颜色
const randomColor = (min = 150, max = 255) => {
  const r = random(min, max)
  const g = random(min, max)
  const b = random(min, max)
  return `rgb(${r},${g},${b})`
}

// 生成拼图验证码
const generateCaptcha = () => {
  if (!bgCanvas.value || !puzzleCanvas.value) return

  const bgCtx = bgCanvas.value.getContext('2d', { willReadFrequently: true })
  const puzzleCtx = puzzleCanvas.value.getContext('2d', { willReadFrequently: true })

  if (!bgCtx || !puzzleCtx) return

  // 清空画布
  bgCtx.clearRect(0, 0, width, height)
  puzzleCtx.clearRect(0, 0, puzzleWidth, puzzleHeight)

  // 生成随机渐变背景
  const gradient = bgCtx.createLinearGradient(0, 0, width, height)
  gradient.addColorStop(0, randomColor(200, 240))
  gradient.addColorStop(0.5, randomColor(180, 220))
  gradient.addColorStop(1, randomColor(200, 240))
  bgCtx.fillStyle = gradient
  bgCtx.fillRect(0, 0, width, height)

  // 绘制随机形状作为图案
  const shapes = ['circle', 'rect', 'triangle']
  for (let i = 0; i < 30; i++) {
    bgCtx.fillStyle = randomColor(150, 200)
    bgCtx.globalAlpha = 0.3
    const shape = shapes[random(0, shapes.length)]
    const x = random(0, width)
    const y = random(0, height)
    const size = random(20, 60)

    bgCtx.beginPath()
    if (shape === 'circle') {
      bgCtx.arc(x, y, size / 2, 0, Math.PI * 2)
    } else if (shape === 'rect') {
      bgCtx.fillRect(x - size / 2, y - size / 2, size, size)
    } else {
      bgCtx.moveTo(x, y - size / 2)
      bgCtx.lineTo(x + size / 2, y + size / 2)
      bgCtx.lineTo(x - size / 2, y + size / 2)
      bgCtx.closePath()
    }
    bgCtx.fill()
  }
  bgCtx.globalAlpha = 1

  // 生成拼图块位置
  initialPuzzleX.value = random(puzzleWidth + 50, width - puzzleWidth - 50)
  puzzleX.value = 0 // 初始位置在左边
  puzzleY.value = random(puzzleHeight, height - puzzleHeight - 30)

  // 获取拼图块区域的图像数据
  const imageData = bgCtx.getImageData(initialPuzzleX.value, puzzleY.value, puzzleWidth, puzzleHeight)

  // 在背景上绘制缺口（灰色矩形）
  bgCtx.fillStyle = 'rgba(200, 200, 200, 0.8)'
  bgCtx.fillRect(initialPuzzleX.value, puzzleY.value, puzzleWidth, puzzleHeight)

  // 绘制缺口边框
  bgCtx.strokeStyle = '#999'
  bgCtx.lineWidth = 2
  bgCtx.strokeRect(initialPuzzleX.value, puzzleY.value, puzzleWidth, puzzleHeight)

  // 将图像数据绘制到拼图块画布
  puzzleCtx.putImageData(imageData, 0, 0)

  // 在拼图块上绘制边框
  puzzleCtx.strokeStyle = '#fff'
  puzzleCtx.lineWidth = 3
  puzzleCtx.strokeRect(0, 0, puzzleWidth, puzzleHeight)

  // 存储目标位置
  localStorage.setItem('slider_captcha_target_x', initialPuzzleX.value.toString())

  console.log('拼图验证码生成:', '目标X:', initialPuzzleX.value, '目标Y:', puzzleY.value)
}

const onTouchStart = (e: TouchEvent) => {
  isDragging.value = true
  startX.value = e.touches[0].clientX - sliderX.value
}

const onTouchMove = (e: TouchEvent) => {
  if (!isDragging.value) return
  e.preventDefault()
  const clientX = e.touches[0].clientX
  moveSlider(clientX)
}

const onTouchEnd = () => {
  if (isDragging.value) {
    isDragging.value = false
    verifyCaptcha()
  }
}

const onMouseDown = (e: MouseEvent) => {
  isDragging.value = true
  startX.value = e.clientX - sliderX.value
}

const onMouseMove = (e: MouseEvent) => {
  if (!isDragging.value) return
  e.preventDefault()
  moveSlider(e.clientX)
}

const onMouseUp = () => {
  if (isDragging.value) {
    isDragging.value = false
    verifyCaptcha()
  }
}

const moveSlider = (clientX: number) => {
  const newSliderX = clientX - startX.value

  // 限制滑动范围（减去滑块宽度）
  const maxSliderX = width - puzzleWidth
  sliderX.value = Math.max(0, Math.min(newSliderX, maxSliderX))

  // 同步更新拼图块位置
  puzzleX.value = sliderX.value
}

const verifyCaptcha = () => {
  const tolerance = 5 // 允许的误差范围（像素）
  const storedTargetX = parseFloat(localStorage.getItem('slider_captcha_target_x') || '0')

  // 计算当前位置
  const currentPosition = sliderX.value
  const distance = Math.abs(currentPosition - storedTargetX)

  console.log('验证位置:', currentPosition, '目标位置:', storedTargetX, '差距:', distance)

  if (distance <= tolerance) {
    statusText.value = '验证成功 ✓'
    emit('success')
    showToast('验证成功')
  } else {
    statusText.value = '验证失败，请重试'
    emit('fail')
    showToast('验证失败，请重试')
    setTimeout(() => {
      refresh()
    }, 1000)
  }
}

const refresh = () => {
  sliderX.value = 0
  puzzleX.value = 0
  statusText.value = '向右滑动完成验证'
  generateCaptcha()
}

onMounted(() => {
  generateCaptcha()
})

defineExpose({
  refresh
})
</script>

<style scoped lang="scss">
.slider-captcha {
  position: relative;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.captcha-container {
  position: relative;
  user-select: none;
}

canvas {
  display: block;
  border-radius: 8px;
  border: 1px solid #ebedf0;
}

.puzzle-canvas {
  position: absolute;
  border-radius: 8px;
  border: 3px solid #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  z-index: 10;
  cursor: move;
}

.slider-box {
  margin-top: 16px;
}

.slider-track {
  position: relative;
  height: 44px;
  background: #f2f3f5;
  border-radius: 22px;
  overflow: hidden;
}

.slider-button {
  position: absolute;
  top: 0;
  width: 54px;
  height: 44px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.4);
  transition: box-shadow 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(79, 172, 254, 0.6);
  }

  &:active {
    cursor: grabbing;
    transform: scale(0.98);
  }

  .arrow-icon {
    color: #fff;
    font-size: 24px;
    font-weight: bold;
    user-select: none;
  }
}

.slider-text {
  text-align: center;
  font-size: 13px;
  color: #969799;
  margin-top: 12px;
  font-weight: 500;
}

.refresh-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px solid #ebedf0;
  transition: all 0.2s;

  &:hover {
    background: #fff;
    transform: rotate(180deg);
  }

  &:active {
    transform: rotate(180deg) scale(0.95);
  }

  .refresh-icon {
    font-size: 20px;
    color: #4facfe;
    font-weight: bold;
  }
}
</style>
