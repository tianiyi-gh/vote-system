<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>活动列表</span>
          <el-button type="primary" @click="handleCreate">创建活动</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="活动名称" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="voteLimit" label="投票限制" width="100">
          <template #default="{ row }">
            {{ row.voteLimit }}票/人
          </template>
        </el-table-column>
        <el-table-column prop="candidateCount" label="候选人数" width="100" />
        <el-table-column prop="totalVotes" label="总票数" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleManageCandidates(row)">管理候选人</el-button>
            <el-button size="small" @click="handleCopy(row)">复制</el-button>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'
import type { Activity } from '@/api/types'

const router = useRouter()
const tableData = ref<Activity[]>([])
const loading = ref(false)

const loadActivities = async () => {
  loading.value = true
  try {
    const response = await api.activities.list()
    tableData.value = response.data || []
  } catch (error) {
    console.error('加载活动列表失败:', error)
    ElMessage.error('加载活动列表失败')
    // 使用模拟数据
    tableData.value = [
      {
        id: 1,
        title: '2024年度优秀员工评选',
        status: 1,
        voteLimit: 3,
        candidateCount: 4,
        totalVotes: 41,
        createdAt: '2024-12-15 08:30:00',
        updatedAt: '2024-12-16 09:20:00'
      },
      {
        id: 2,
        title: '最佳团队投票',
        status: 2,
        voteLimit: 1,
        candidateCount: 3,
        totalVotes: 65,
        createdAt: '2024-12-10 10:00:00',
        updatedAt: '2024-12-16 15:30:00'
      }
    ]
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: number) => {
  const types = {
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts = {
    1: '进行中',
    2: '已结束',
    3: '已取消'
  }
  return texts[status] || '未知'
}

const handleCreate = () => {
  router.push('/activity/create')
}

const handleEdit = (row: Activity) => {
  router.push(`/activity/edit/${row.id}`)
}

const handleManageCandidates = (row: Activity) => {
  // 跳转到候选人列表页面，并传递活动ID
  router.push({
    path: '/candidate/list',
    query: { activityId: row.id }
  })
}

const handleCopy = async (row: Activity) => {
  try {
    await ElMessageBox.confirm(`确定要复制活动"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    try {
      await api.activities.copy(row.id)
      ElMessage.success('复制成功')
      await loadActivities()
    } catch (error) {
      ElMessage.error('复制失败')
    }
  } catch {
    // 用户取消复制
  }
}

const handleDelete = async (row: Activity) => {
  try {
    await ElMessageBox.confirm(`确定要删除活动"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    try {
      await api.activities.delete(row.id)
      ElMessage.success('删除成功')
      await loadActivities()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  } catch {
    // 用户取消删除
  }
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-container {
  padding: 20px;
}
</style>