<template>
  <div class="candidate-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>候选人管理</span>
          <div class="header-actions">
            <el-button v-if="!sortMode" type="warning" @click="enableSortMode">排序模式</el-button>
            <el-button @click="exportCandidateStats">导出统计</el-button>
            <el-button @click="handleBatchImport">批量导入</el-button>
            <el-button type="primary" @click="handleCreate">添加候选人</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="searchForm" inline>
          <el-form-item label="选择活动">
            <el-select
              v-model="currentActivityId"
              placeholder="请选择活动"
              filterable
              @change="loadData"
              style="width: 200px"
            >
              <el-option
                v-for="activity in activityList"
                :key="activity.id"
                :label="activity.title"
                :value="activity.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="候选人姓名">
            <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
          </el-form-item>
          <el-form-item label="所属分组">
            <el-select v-model="searchForm.groupName" placeholder="请选择分组" clearable>
              <el-option label="第一组" value="1" />
              <el-option label="第二组" value="2" />
              <el-option label="第三组" value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="未开始" :value="0" />
              <el-option label="正常" :value="1" />
              <el-option label="禁用" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格操作栏 -->
      <div class="table-toolbar" v-if="sortMode">
        <el-alert type="info" :closable="false">
          <template #title>
            点击上移/下移按钮调整候选人排序，完成后点击"保存排序"
          </template>
        </el-alert>
        <el-button type="primary" @click="saveOrder" :loading="savingOrder">
          保存排序
        </el-button>
        <el-button @click="cancelSort">取消</el-button>
      </div>

      <!-- 表格 -->
      <el-table
        :data="tableData"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
        v-if="!sortMode"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="排名" width="80">
          <template #default="{ $index }">
            <el-tag v-if="$index < 3" :type="getRankingType($index + 1)">
              {{ $index + 1 }}
            </el-tag>
            <span v-else>{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="照片" width="80">
          <template #default="{ row }">
            <el-avatar
              :src="getAvatarUrl(row.avatar)"
              :size="50"
              :alt="row.name"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="候选人姓名" min-width="120" />
        <el-table-column prop="candidateNo" label="候选人编号" width="120" />
        <el-table-column prop="groupName" label="所属分组" width="100">
          <template #default="{ row }">
            <el-tag>{{ getGroupName(row.groupName) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalVotes" label="总票数" width="100" sortable>
          <template #default="{ row }">
            <strong>{{ row.totalVotes?.toLocaleString() || 0 }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="100" sortable>
          <template #default="{ row }">
            {{ row.score?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row, $index }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handleViewVotes(row)">投票记录</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 排序模式下的表格 -->
      <el-table
        :data="tableData"
        style="width: 100%"
        v-loading="loading"
        row-key="id"
        v-else
      >
        <el-table-column label="排序" width="80">
          <template #default="{ $index }">
            <el-button-group>
              <el-button size="small" :disabled="$index === 0" @click="moveUp($index)">
                <el-icon><arrow-up /></el-icon>
              </el-button>
              <el-button size="small" :disabled="$index === tableData.length - 1" @click="moveDown($index)">
                <el-icon><arrow-down /></el-icon>
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
        <el-table-column label="排名" width="80">
          <template #default="{ $index }">
            <el-tag v-if="$index < 3" :type="getRankingType($index + 1)">
              {{ $index + 1 }}
            </el-tag>
            <span v-else>{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="照片" width="80">
          <template #default="{ row }">
            <el-avatar
              :src="getAvatarUrl(row.avatar)"
              :size="50"
              :alt="row.name"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="候选人姓名" min-width="120" />
        <el-table-column prop="candidateNo" label="候选人编号" width="120" />
        <el-table-column prop="groupName" label="所属分组" width="100">
          <template #default="{ row }">
            <el-tag>{{ getGroupName(row.groupName) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalVotes" label="总票数" width="100">
          <template #default="{ row }">
            <strong>{{ row.totalVotes?.toLocaleString() || 0 }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="100">
          <template #default="{ row }">
            {{ row.score?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedCandidates.length > 0">
        <span>已选择 {{ selectedCandidates.length }} 项</span>
        <el-button size="small" @click="handleBatchDelete">批量删除</el-button>
        <el-button size="small" @click="handleBatchStatus(1)">批量设为正常</el-button>
        <el-button size="small" @click="handleBatchStatus(2)">批量禁用</el-button>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑候选人' : '添加候选人'" 
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="候选人姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入候选人姓名" />
        </el-form-item>
        <el-form-item label="候选人编号">
          <el-input v-model="form.candidateNo" placeholder="候选人编号，如 C001" />
        </el-form-item>
        <el-form-item label="所属分组">
          <el-select v-model="form.groupName" placeholder="请选择分组">
            <el-option label="第一组" value="1" />
            <el-option label="第二组" value="2" />
            <el-option label="第三组" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="照片">
          <el-upload
            class="avatar-uploader"
            :action="'/api/upload/avatar'"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <el-image
              v-if="form.avatar"
              :src="getAvatarUrl(form.avatar)"
              style="width: 100px; height: 100px; border-radius: 10px;"
              fit="cover"
            />
            <el-icon v-else class="avatar-uploader-icon"><upload-filled /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="简介">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入候选人简介" 
          />
        </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">正常</el-radio>
              <el-radio :label="0">未开始</el-radio>
              <el-radio :label="2">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="批量导入候选人" width="700px">
      <el-steps :active="importStep" finish-status="success" align-center>
        <el-step title="选择文件" />
        <el-step title="确认导入" />
        <el-step title="完成" />
      </el-steps>

      <!-- 步骤1：选择文件 -->
      <div v-if="importStep === 0" class="import-step">
        <el-upload
          ref="uploadRef"
          class="upload-excel"
          drag
          :auto-upload="false"
          :on-change="handleFileSelect"
          :before-upload="beforeImportUpload"
          accept=".xlsx,.xls"
          :limit="1"
          :on-exceed="handleExceed"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传 xlsx/xls 文件，且不超过 10MB
            </div>
          </template>
        </el-upload>

        <div class="template-download">
          <el-button type="primary" link @click="downloadTemplate">
            <el-icon style="margin-right: 5px"><download /></el-icon>
            下载导入模板
          </el-button>
        </div>
      </div>

      <!-- 步骤2：确认导入 -->
      <div v-if="importStep === 1" class="import-step">
        <el-alert
          title="导入预览"
          type="info"
          :closable="false"
          style="margin-bottom: 15px;"
        >
          已读取 {{ importPreview.length }} 条候选人数据，请确认后导入
        </el-alert>

        <el-table
          :data="importPreview"
          style="width: 100%"
          max-height="300"
          border
        >
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="name" label="候选人姓名" width="120" />
          <el-table-column prop="orderNum" label="编号" width="80" />
          <el-table-column prop="groupName" label="所属分组" width="100" />
          <el-table-column prop="description" label="简介" show-overflow-tooltip />
          <el-table-column prop="avatar" label="头像URL" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 步骤3：完成 -->
      <div v-if="importStep === 2" class="import-step">
        <el-result
          icon="success"
          title="导入成功"
          :sub-title="`成功导入 ${importSuccessCount} 条候选人数据`"
        />
      </div>

      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button v-if="importStep === 1" @click="importStep = 0">上一步</el-button>
        <el-button
          v-if="importStep === 0"
          type="primary"
          @click="nextImportStep"
          :disabled="!selectedFile"
        >
          下一步
        </el-button>
        <el-button
          v-if="importStep === 1"
          type="primary"
          @click="confirmImport"
          :loading="importing"
        >
          确认导入
        </el-button>
        <el-button v-if="importStep === 2" type="primary" @click="finishImport">
          完成
        </el-button>
      </template>
    </el-dialog>

    <!-- 导出列选择对话框 -->
    <el-dialog v-model="exportColumnDialogVisible" title="选择导出列" width="600px">
      <div class="export-columns-dialog">
        <el-checkbox-group v-model="exportColumns">
          <div class="column-grid">
            <el-checkbox
              v-for="column in exportColumns"
              :key="column.key"
              :label="column"
              :model-value="column.visible"
              @change="column.visible = $event"
            >
              {{ column.label }}
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="exportColumnDialogVisible = false">取消</el-button>
          <el-button @click="selectAllColumns">全选</el-button>
          <el-button @click="deselectAllColumns">全不选</el-button>
          <el-button type="primary" @click="confirmExport">
            确认导出（{{ exportFormat === 'excel' ? 'Excel' : 'CSV' }}）
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { UploadFilled, Download, Rank, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import dayjs from 'dayjs'
import api from '@/api'
import type { Activity, Candidate } from '@/api/types'
import { exportToCSV, exportToExcel, createExportColumns, type ExportColumn } from '@/utils/exportHelper'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const ing = ref(false)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref<Candidate[]>([])
const activityList = ref<Activity[]>([])
const selectedCandidates = ref<Candidate[]>([])
const formRef = ref()

// 排序相关
const sortMode = ref(false)
const savingOrder = ref(false)

// 批量导入相关
const importStep = ref(0)
const selectedFile = ref<File | null>(null)
const importPreview = ref<any[]>([])
const importSuccessCount = ref(0)

// 导出相关
const exportColumnDialogVisible = ref(false)
const exportFormat = ref<'csv' | 'excel'>('csv')
const exportColumns = ref<ExportColumn[]>([])
const exportResolve = ref<(() => void) | null>(null)
const exportReject = ref<(() => void) | null>(null)

// 当前活动ID
const currentActivityId = ref<number | null>(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  groupName: '',
  status: ''
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表单
const form = reactive({
  id: null,
  name: '',
  candidateNo: '',
  orderNum: 0,
  groupName: '1',
  avatar: '',
  description: '',
  status: 1
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入候选人姓名', trigger: 'blur' }
  ]
}

// 加载活动列表
const loadActivities = async () => {
  try {
    const response = await api.activities.list()
    activityList.value = response.data || []
    if (activityList.value.length > 0 && !currentActivityId.value) {
      currentActivityId.value = activityList.value[0].id
      loadData()
    }
  } catch (error) {
    console.error('加载活动列表失败:', error)
    ElMessage.error('加载活动列表失败')
  }
}

// 方法
const loadData = async () => {
  if (!currentActivityId.value) {
    ElMessage.warning('请先选择活动')
    return
  }

  loading.value = true
  try {
    const response = await api.candidates.list(currentActivityId.value)
    const data = response.data || []

    // 映射后端数据到前端
    tableData.value = data.map((item: any) => ({
      id: item.id,
      name: item.name,
      candidateNo: item.candidateNo || '',
      groupName: item.groupName || '',
      avatar: item.avatar || '',
      description: item.description || '',
      orderNum: item.orderNum || 0,
      totalVotes: item.votes || item.totalVotes || 0,
      webVotes: item.webVotes || 0,
      smsVotes: item.smsVotes || 0,
      wechatVotes: item.wechatVotes || 0,
      score: item.score || 0,
      ranking: 0,
      status: item.status ?? 1,
      createTime: item.createTime || item.createdAt || null,
      updatedAt: item.updateTime || item.updatedAt || null
    }))

    // 根据查询条件过滤
    if (searchForm.name) {
      tableData.value = tableData.value.filter(item =>
        item.name.includes(searchForm.name)
      )
    }
    if (searchForm.groupName) {
      tableData.value = tableData.value.filter(item =>
        item.groupName === searchForm.groupName
      )
    }
    if (searchForm.status !== '') {
      tableData.value = tableData.value.filter(item =>
        item.status === Number(searchForm.status)
      )
    }

    pagination.total = tableData.value.length
  } catch (error) {
    console.error('加载候选人列表失败:', error)
    ElMessage.error('加载候选人列表失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.groupName = ''
  searchForm.status = ''
  pagination.current = 1
  loadData()
}

const handleCreate = () => {
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.candidateNo = row.candidateNo || ''
  form.orderNum = row.orderNum || 0
  form.groupName = row.groupName || '1'
  form.avatar = row.avatar || ''
  form.description = row.description || ''
  form.status = row.status ?? 0
  dialogVisible.value = true
}

const handleDelete = async (row: Candidate) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除候选人"${row.name}"吗？删除后不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await api.candidates.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleViewVotes = (row) => {
  // 跳转到投票记录页面
  console.log('查看投票记录:', row)
}

const handleSelectionChange = (selection) => {
  selectedCandidates.value = selection
}

const handleBatchDelete = async () => {
  if (selectedCandidates.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedCandidates.value.length} 个候选人吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用批量删除API
    ElMessage.success('批量删除成功')
    loadData()
  } catch {
    // 用户取消
  }
}

const handleBatchStatus = async (status) => {
  if (selectedCandidates.value.length === 0) return

  let statusText = ''
  if (status === 0) statusText = '设为未开始'
  else if (status === 1) statusText = '设为正常'
  else if (status === 2) statusText = '禁用'

  try {
    await ElMessageBox.confirm(
      `确定要${statusText}选中的 ${selectedCandidates.value.length} 个候选人吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // 调用批量更新状态API
    ElMessage.success(`批量${statusText}成功`)
    loadData()
  } catch {
    // 用户取消
  }
}

const handleBatchImport = () => {
  importStep.value = 0
  selectedFile.value = null
  importPreview.value = []
  importDialogVisible.value = true
}

const handleFileSelect = async (file: any) => {
  selectedFile.value = file.raw
  try {
    const data = await parseExcel(file.raw)
    importPreview.value = data
    ElMessage.success(`成功读取 ${data.length} 条数据`)
  } catch (error: any) {
    console.error('解析Excel失败:', error)
    ElMessage.error(error.message || '解析Excel文件失败')
  }
}

const parseExcel = (file: File): Promise<any[]> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const data = e.target?.result
        const workbook = XLSX.read(data, { type: 'binary' })
        const firstSheetName = workbook.SheetNames[0]
        const worksheet = workbook.Sheets[firstSheetName]
        const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })

        // 跳过表头，从第2行开始（索引1）
        if (jsonData.length > 1) {
          const rows = jsonData.slice(1).map((row: any) => ({
            name: row[0] || '',  // 候选人姓名
            orderNum: parseInt(row[1]) || 0,  // 编号
            groupName: mapGroupName(row[2]) || '1',  // 所属分组
            description: row[3] || '',  // 简介
            avatar: row[4] || '',  // 头像URL
            status: parseInt(row[5]) || 1  // 状态
          }))
          resolve(rows)
        } else {
          resolve([])
        }
      } catch (error) {
        reject(error)
      }
    }
    reader.onerror = reject
    reader.readAsBinaryString(file)
  })
}

const mapGroupName = (value: string): string => {
  if (!value) return '1'
  const name = value.trim()
  if (['第一组', '第二组', '第三组'].includes(name)) {
    return name === '第一组' ? '1' : name === '第二组' ? '2' : '3'
  }
  return name
}

const beforeImportUpload = (file: File) => {
  const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
                  file.type === 'application/vnd.ms-excel'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isExcel) {
    ElMessage.error('上传文件只能是 Excel 格式!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件，请先删除已选择的文件')
}

const nextImportStep = () => {
  if (importPreview.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }
  importStep.value = 1
}

const confirmImport = async () => {
  if (!currentActivityId.value) {
    ElMessage.warning('请先选择活动')
    return
  }

  ing.value = true
  try {
    await api.candidates.batchImport({
      activityId: currentActivityId.value,
      candidates: importPreview.value
    })
    importSuccessCount.value = importPreview.value.length
    importStep.value = 2
    ElMessage.success('导入成功')
    loadData()
  } catch (error) {
    console.error('导入失败:', error)
    ElMessage.error('导入失败，请重试')
  } finally {
    ing.value = false
  }
}

const finishImport = () => {
  importDialogVisible.value = false
  importStep.value = 0
  importPreview.value = []
  selectedFile.value = null
}

const downloadTemplate = async () => {
  try {
    const response = await api.candidates.downloadTemplate()
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '候选人导入模板.xlsx'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    console.error('下载模板失败:', error)
    ElMessage.error('下载模板失败')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate()
  if (!valid) return

  if (!currentActivityId.value) {
    ElMessage.warning('请先选择活动')
    return
  }

  submitting.value = true
  try {
    const currentActivity = activityList.value.find(a => a.id === currentActivityId.value)
    const data = {
      activityId: currentActivityId.value,
      serviceId: currentActivity?.serviceId || '',
      name: form.name,
      candidateNo: form.candidateNo || '',
      orderNum: form.orderNum || 0,
      groupName: form.groupName,
      avatar: form.avatar,
      description: form.description,
      status: form.status
    }

    if (isEdit.value) {
      await api.candidates.update(form.id, data)
    } else {
      await api.candidates.create(data)
    }

    ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.candidateNo = ''
  form.orderNum = 0
  form.groupName = '1'
  form.avatar = ''
  form.description = ''
  form.status = 1  // 默认正常
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200 && response.data && response.data.url) {
    form.avatar = response.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const removeImage = () => {
  form.avatar = ''
  ElMessage.info('图片已删除，可以重新上传')
}



const getRankingType = (ranking) => {
  if (ranking === 1) return 'danger'
  if (ranking === 2) return 'warning'
  if (ranking === 3) return 'info'
  return 'info'
}

const getStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  if (status === 0) return 'warning'
  return 'info'
}

const getGroupName = (groupName) => {
  const map: Record<string, string> = {
    '1': '第一组',
    '2': '第二组',
    '3': '第三组'
  }
  return map[groupName] || groupName || '-'
}

const getStatusText = (status) => {
  if (status === 0) return '未开始'
  if (status === 1) return '正常'
  if (status === 2) return '禁用'
  return '未知'
}

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar
  }
  // 否则拼接完整URL（通过代理访问）
  return avatar
}

const formatTime = (time) => {
  if (!time) return '-'
  try {
    return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
  } catch {
    return '-'
  }
}

const exportCandidateStats = async () => {
  try {
    const action = await ElMessageBox.confirm(
      '选择导出格式和选项',
      '导出候选人统计',
      {
        distinguishCancelAndClose: true,
        confirmButtonText: 'CSV导出',
        cancelButtonText: '取消',
        type: 'info',
      }
    ).then(() => 'csv')
      .catch(action => action === 'cancel' ? 'excel' : null)

    if (!action) return

    // 询问是否自定义列
    const customColumns = await ElMessageBox.confirm(
      '是否自定义导出列？',
      '导出选项',
      {
        distinguishCancelAndClose: true,
        confirmButtonText: '自定义列',
        cancelButtonText: '全部导出',
        type: 'info',
      }
    ).then(() => true)
      .catch(() => false)

    if (customColumns) {
      // 显示列选择对话框
      await showExportColumnDialog(action)
    } else {
      // 全部导出
      await downloadCandidateExport(action, action)
    }
  } catch {
    // 用户取消
  }
}

const showExportColumnDialog = (format: string) => {
  return new Promise<void>((resolve, reject) => {
    // 使用Element Plus的Checkbox Group实现列选择
    exportColumnDialogVisible.value = true
    exportFormat.value = format
    exportResolve.value = resolve
    exportReject.value = reject
  })
}

const confirmExport = () => {
  const columns = exportColumns.value.filter(col => col.visible)
  const format = exportFormat.value

  try {
    const loading = ElLoading.service({
      lock: true,
      text: '正在导出...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 导出数据
    if (format === 'excel') {
      exportToExcel(tableData.value, columns, `candidate_stats_${Date.now()}.xls`)
    } else {
      exportToCSV(tableData.value, columns, `candidate_stats_${Date.now()}.csv`)
    }

    loading.close()
    ElMessage.success('导出成功')
    exportColumnDialogVisible.value = false

    if (exportResolve.value) {
      exportResolve.value()
    }
  } catch (error) {
    console.error('导出错误:', error)
    ElMessage.error('导出失败，请重试')

    if (exportReject.value) {
      exportReject.value()
    }
  }
}

// 全选列
const selectAllColumns = () => {
  exportColumns.value.forEach(col => {
    col.visible = true
  })
}

// 全不选列
const deselectAllColumns = () => {
  exportColumns.value.forEach(col => {
    col.visible = false
  })
}

const downloadCandidateExport = async (exportType: string, extension: string) => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '正在导出...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 直接从表格数据生成 CSV
    const csvContent = generateCsvFromTableData()
    downloadCsv(csvContent, `candidate_stats_${Date.now()}.csv`)

    loading.close()
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出错误:', error)
    loading.close()
    ElMessage.error('导出失败，请重试')
  }
}

const generateCsvFromTableData = () => {
  // CSV 头部（UTF-8 BOM）
  let csv = '\uFEFF'
  csv += 'ID,候选人姓名,编号,分组,照片,得票数,网络票,短信票,微信票,得分,状态,创建时间\n'

  // 添加数据行
  tableData.value.forEach(row => {
    csv += [
      row.id,
      row.name,
      row.candidateNo,
      row.groupName,
      row.avatar,
      row.totalVotes || 0,
      row.webVotes || 0,
      row.smsVotes || 0,
      row.wechatVotes || 0,
      row.score || 0,
      row.status === 1 ? '正常' : row.status === 0 ? '未开始' : '禁用',
      row.createTime
    ].map(escapeCsvValue).join(',') + '\n'
  })

  return csv
}

const escapeCsvValue = (value: any) => {
  if (value === null || value === undefined) {
    return ''
  }
  const str = String(value)
  if (str.includes(',') || str.includes('"') || str.includes('\n')) {
    return `"${str.replace(/"/g, '""')}"`
  }
  return str
}

const downloadCsv = (content: string, filename: string) => {
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(url)
}

// 生命周期
onMounted(() => {
  loadActivities()

  // 初始化导出列定义
  initExportColumns()

  // 从URL参数获取活动ID
  if (route.query.activityId) {
    currentActivityId.value = Number(route.query.activityId)
    loadData()
  }
})

// 排序功能
const enableSortMode = () => {
  sortMode.value = true
}

const cancelSort = () => {
  sortMode.value = false
  loadData() // 重新加载数据恢复原顺序
}

const saveOrder = async () => {
  savingOrder.value = true
  try {
    // 为每个候选人设置新的排序号
    for (let i = 0; i < tableData.value.length; i++) {
      const candidate = tableData.value[i]
      await api.candidates.updateOrder(candidate.id, i + 1)
    }
    ElMessage.success('排序保存成功')
    sortMode.value = false
    loadData()
  } catch (error) {
    console.error('保存排序失败:', error)
    ElMessage.error('保存排序失败')
  } finally {
    savingOrder.value = false
  }
}

// 上移
const moveUp = (index: number) => {
  if (index > 0) {
    const temp = tableData.value[index]
    tableData.value[index] = tableData.value[index - 1]
    tableData.value[index - 1] = temp
  }
}

// 下移
const moveDown = (index: number) => {
  if (index < tableData.value.length - 1) {
    const temp = tableData.value[index]
    tableData.value[index] = tableData.value[index + 1]
    tableData.value[index + 1] = temp
  }
}

// 初始化导出列
const initExportColumns = () => {
  exportColumns.value = createExportColumns([
    { key: 'id', label: 'ID' },
    { key: 'name', label: '候选人姓名' },
    { key: 'candidateNo', label: '编号' },
    { key: 'groupName', label: '分组' },
    { key: 'avatar', label: '照片' },
    { key: 'totalVotes', label: '得票数' },
    { key: 'webVotes', label: '网络票' },
    { key: 'smsVotes', label: '短信票' },
    { key: 'wechatVotes', label: '微信票' },
    { key: 'score', label: '得分' },
    { key: 'status', label: '状态' },
    { key: 'createTime', label: '创建时间' }
  ])
}
</script>

<style scoped lang="scss">
.candidate-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-actions {
    display: flex;
    gap: 10px;
  }

  .search-area {
    margin-bottom: 20px;
  }

  .channel-votes {
    display: flex;
    flex-direction: column;
    gap: 2px;
    font-size: 12px;
    
    span {
      margin-right: 8px;
    }
  }

  .batch-actions {
    margin: 20px 0;
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
    display: flex;
    align-items: center;
    gap: 10px;

    span {
      margin-right: 20px;
      color: #606266;
    }
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }

  .image-upload-section {
    .image-preview {
      width: 100%;
      min-height: 120px;
      border: 2px dashed #d9d9d9;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s;

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
      min-height: 100px;
      color: #909399;
    }

    .image-actions {
      margin-top: 10px;
      text-align: center;
    }
  }

  .avatar-uploader {
    :deep(.el-upload) {
      border: 2px dashed #d9d9d9;
      border-radius: 10px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: all 0.3s;

      &:hover {
        border-color: #667eea;
        background-color: #f8f9fa;
      }
    }

    :deep(.el-upload:hover .el-upload-dragger) {
      border-color: #667eea;
    }

    .avatar-uploader-icon {
      font-size: 40px;
      color: #8c939d;
      margin: 10px 0;
    }

    :deep(.el-image) {
      width: 100px;
      height: 100px;
      display: block;
    }
  }

  .upload-excel {
    .el-upload {
      width: 100%;
    }
  }

  .template-download {
    margin-top: 20px;
    text-align: center;
  }

  .export-columns-dialog {
    .column-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;
      max-height: 400px;
      overflow-y: auto;
      padding: 10px;
      border: 1px solid #eee;
      border-radius: 4px;
    }

    .el-checkbox {
      margin-right: 0;
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }

  .import-step {
    padding: 20px 0;
    min-height: 300px;

    .el-steps {
      margin-bottom: 30px;
    }
  }
}
</style>

<style scoped>
.drag-handle {
  cursor: grab;
  user-select: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.drag-handle:active {
  cursor: grabbing;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  gap: 10px;
}

.table-toolbar .el-alert {
  flex: 1;
  margin-right: 10px;
}
</style>