<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>创建活动</span>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="150px">
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
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="每人投票限制">
          <el-input-number v-model="form.voteLimit" :min="0" :max="100" style="width: 200px" />
          <span style="margin-left: 10px; color: #909399">每人最多可投的票数（0表示不限制）</span>
        </el-form-item>

        <el-form-item label="每天最多候选人">
          <el-input-number v-model="form.dailyCandidateLimit" :min="0" :max="50" style="width: 200px" />
          <span style="margin-left: 10px; color: #909399">每天最多可对多少名候选人投票（0表示不限制）</span>
        </el-form-item>

        <el-form-item label="候选人每日限制">
          <el-input-number v-model="form.candidateDailyLimit" :min="0" :max="20" style="width: 200px" />
          <span style="margin-left: 10px; color: #909399">每个候选人每天限投多少次（0表示不限制）</span>
        </el-form-item>

        <el-form-item label="IP投票限制">
          <el-input-number v-model="form.ipLimit" :min="0" :max="100" style="width: 200px" />
          <span style="margin-left: 10px; color: #909399">每个IP最多可投的票数</span>
        </el-form-item>

        <el-form-item label="活动状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">进行中</el-radio>
            <el-radio :value="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider content-position="left">渠道权重设置</el-divider>

        <el-form-item label="短信投票权重">
          <el-input-number v-model="form.smsWeight" :min="0" style="width: 200px" />
        </el-form-item>

        <el-form-item label="网络投票权重">
          <el-input-number v-model="form.webWeight" :min="0" style="width: 200px" />
        </el-form-item>

        <el-form-item label="微信投票权重">
          <el-input-number v-model="form.wechatWeight" :min="0" style="width: 200px" />
        </el-form-item>

        <el-divider content-position="left">其他设置</el-divider>

        <el-form-item label="启用验证码">
          <el-switch v-model="form.enableCaptcha" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-form-item label="显示票数">
          <el-switch v-model="form.showVotes" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">创建</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const uploading = ref(false)
const fileInputRef = ref<HTMLInputElement>()

const form = reactive({
  title: '',
  subtitle: '',
  region: '',
  description: '',
  coverImage: '',
  startTime: '',
  endTime: '',
  voteLimit: 20,
  dailyCandidateLimit: 10,
  candidateDailyLimit: 5,
  ipLimit: 0,
  groupCount: 1,
  smsWeight: 100,
  ivrWeight: 100,
  webWeight: 100,
  appWeight: 100,
  wechatWeight: 100,
  payWeight: 100,
  enableCaptcha: 1,
  showVotes: 1,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const submitForm = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate()
  if (!valid) return

  submitting.value = true
  try {
    await api.activities.create(form)
    ElMessage.success('创建成功')
    router.push('/activity/list')
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  form.coverImage = ''
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
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.box-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
</style>