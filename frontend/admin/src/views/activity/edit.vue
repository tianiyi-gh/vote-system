<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>编辑活动</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" v-loading="loading">
        <el-form-item label="活动名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动名称" />
        </el-form-item>

        <el-form-item label="活动描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入活动描述" />
        </el-form-item>

        <el-form-item label="封面图">
          <div class="image-upload-section">
            <input
              type="file"
              ref="fileInputRef"
              @change="handleImageUpload"
              accept="image/*"
              style="display: none;"
            />
            <div class="image-preview" @click="triggerFileInput">
              <el-image
                v-if="form.coverImage"
                :src="getCoverImageUrl(form.coverImage)"
                style="width: 300px; height: 200px; border-radius: 8px;"
                :preview-src-list="[getCoverImageUrl(form.coverImage)]"
                fit="cover"
              />
              <div v-else class="upload-placeholder">
                <el-icon :size="40"><upload-filled /></el-icon>
                <span>点击上传封面图</span>
              </div>
            </div>
            <div class="image-actions">
              <el-button v-if="form.coverImage" type="danger" size="small" @click.stop="removeImage">删除图片</el-button>
            </div>
          </div>
          <div class="el-upload__tip">
            支持 jpg/png/gif/webp 文件，推荐尺寸 800x400，文件大小不超过 5MB
          </div>
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="每人投票限制" prop="voteLimit">
          <el-input-number v-model="form.voteLimit" :min="0" :max="100" />
          <span style="margin-left: 10px; color: #909399">每人最多可投的票数（0表示不限制）</span>
        </el-form-item>

        <el-form-item label="每天最多候选人" prop="dailyCandidateLimit">
          <el-input-number v-model="form.dailyCandidateLimit" :min="0" :max="50" />
          <span style="margin-left: 10px; color: #909399">每天最多可对多少名候选人投票（0表示不限制）</span>
        </el-form-item>

        <el-form-item label="候选人每日限制" prop="candidateDailyLimit">
          <el-input-number v-model="form.candidateDailyLimit" :min="0" :max="20" />
          <span style="margin-left: 10px; color: #909399">每个候选人每天限投多少次（0表示不限制）</span>
        </el-form-item>

        <el-form-item label="活动状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">进行中</el-radio>
            <el-radio :value="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const fileInputRef = ref<HTMLInputElement>()

const form = reactive({
  id: null as number | null,
  title: '',
  description: '',
  coverImage: '',
  startTime: '',
  endTime: '',
  voteLimit: 20,
  dailyCandidateLimit: 10,
  candidateDailyLimit: 5,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  voteLimit: [{ required: true, message: '请设置投票限制', trigger: 'blur' }]
}

const loadActivity = async () => {
  loading.value = true
  try {
    const activityId = Number(route.params.id)
    const response = await api.activities.get(activityId)
    const activity = response.data

    form.id = activity.id
    form.title = activity.title
    form.description = activity.description || ''
    form.coverImage = activity.coverImage || ''
    // 处理时间格式
    form.startTime = activity.startTime ? formatDateTime(activity.startTime) : ''
    form.endTime = activity.endTime ? formatDateTime(activity.endTime) : ''
    form.voteLimit = activity.voteLimit || 20
    form.dailyCandidateLimit = activity.dailyCandidateLimit || 10
    form.candidateDailyLimit = activity.candidateDailyLimit || 5
    form.status = activity.status

    console.log('加载的活动数据:', activity)
    console.log('格式化后的开始时间:', form.startTime)
    console.log('格式化后的结束时间:', form.endTime)
  } catch (error) {
    console.error('加载活动详情失败:', error)
    ElMessage.error('加载活动详情失败')
    router.push('/activity/list')
  } finally {
    loading.value = false
  }
}

// 格式化日期时间为 YYYY-MM-DD HH:mm:ss
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const submitForm = async () => {
  if (!formRef.value || !form.id) return

  const valid = await formRef.value.validate()
  if (!valid) return

  submitting.value = true
  try {
    const updateData = {
      title: form.title,
      description: form.description || '',
      coverImage: form.coverImage || '',
      startTime: form.startTime,
      endTime: form.endTime,
      voteLimit: form.voteLimit,
      dailyCandidateLimit: form.dailyCandidateLimit,
      candidateDailyLimit: form.candidateDailyLimit,
      status: form.status
    }

    console.log('提交的数据:', updateData)
    console.log('活动ID:', form.id)

    await api.activities.update(form.id, updateData)
    ElMessage.success('保存成功')
    router.push('/activity/list')
  } catch (error: any) {
    console.error('保存失败:', error)
    console.error('错误详情:', error.message, error.stack)
    // 显示更详细的错误信息
    const errorMsg = error.message || '保存失败，请检查输入数据'
    ElMessage.error(errorMsg)
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/activity/list')
}

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleImageUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  uploading.value = true

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await api.upload.uploadAvatar(formData)

    if (response.data && response.data.url) {
      form.coverImage = response.data.url
      ElMessage.success('封面图上传成功')
    } else {
      ElMessage.error('图片上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
    if (target) target.value = ''
  }
}

const removeImage = () => {
  form.coverImage = ''
  ElMessage.info('封面图已删除，可以重新上传')
}

const getCoverImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 开发环境使用环境变量，生产环境使用相对路径
  const uploadUrl = import.meta.env.VITE_API_UPLOAD_URL
  return uploadUrl ? `${uploadUrl}${url}` : url
}

onMounted(() => {
  loadActivity()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.box-card {
  max-width: 800px;
  margin: 0 auto;
}

.image-upload-section {
  .image-preview {
    width: 300px;
    min-height: 200px;
    border: 2px dashed #d9d9d9;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s;
    overflow: hidden;

    &:hover {
      border-color: #667eea;
      background-color: #f8f9fa;
    }
  }

  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    min-height: 200px;
    color: #909399;
  }

  .image-actions {
    margin-top: 10px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>