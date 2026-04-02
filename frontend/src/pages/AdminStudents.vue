<template>
  <div class="page admin-page">
    <el-card>
      <div class="head">
        <h2>学生管理</h2>
        <div class="ops">
          <el-input v-model="keyword" placeholder="账号/姓名/学号" clearable style="width:220px" @keyup.enter="load" />
          <el-button @click="load">查询</el-button>
          <el-button type="success" @click="exportCsv">导出CSV</el-button>
        </div>
      </div>

      <div class="batch-floating" v-if="selectedRows.length">
        已选中 {{ selectedRows.length }} 项
        <el-button size="small" type="danger" @click="batchDisable">批量禁用</el-button>
      </div>

      <el-table :data="rows" v-loading="loading" @selection-change="selectedRows = $event">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="account" label="账号" width="140" />
        <el-table-column prop="name" label="姓名" width="130" />
        <el-table-column prop="studentId" label="学号" width="150" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="verified" label="认证" width="90">
          <template #default="{ row }">
            <el-tag :type="row.verified ? 'success' : 'warning'">{{ row.verified ? '已认证' : '未认证' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="disabled" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.disabled ? 'danger' : 'success'">{{ row.disabled ? '禁用' : '正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <div class="table-actions cols-2">
              <el-button size="small" @click="viewDetail(row)">详情</el-button>
              <el-button size="small" :type="row.disabled ? 'success' : 'danger'" @click="toggleDisabled(row)">
                {{ row.disabled ? '恢复' : '禁用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDetail" title="学生详情" width="520px">
      <el-descriptions :column="1" border v-if="detail">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ detail.account }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detail.studentId }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ detail.gender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="认证状态">{{ detail.verified ? '已认证' : '未认证' }}</el-descriptions-item>
        <el-descriptions-item label="账号状态">{{ detail.disabled ? '禁用' : '正常' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const rows = ref([])
const keyword = ref('')
const showDetail = ref(false)
const detail = ref(null)
const selectedRows = ref([])

const parseData = (res) => {
  const body = res?.data || {}
  return body.data || body
}

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/students', { params: { keyword: keyword.value || undefined } })
    rows.value = parseData(res) || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '学生列表加载失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await axios.get(`/api/admin/students/${row.id}`)
    detail.value = parseData(res)
    showDetail.value = true
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '详情加载失败')
  }
}

const toggleDisabled = async (row) => {
  const disabled = row.disabled ? 0 : 1
  try {
    await axios.post(`/api/admin/students/${row.id}/disable`, { disabled })
    ElMessage.success(disabled ? '已禁用' : '已恢复')
    await load()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '操作失败')
  }
}

const exportCsv = () => {
  window.open('/api/admin/students/export', '_blank')
}

const batchDisable = async () => {
  for (const row of selectedRows.value) {
    await axios.post(`/api/admin/students/${row.id}/disable`, { disabled: 1 })
  }
  ElMessage.success('批量禁用完成')
  await load()
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px; gap: 12px; }
.ops { display:flex; gap: 8px; flex-wrap: wrap; }
</style>
