<template>
  <div class="page">
    <el-card>
      <div class="head">
        <h2>预约订单管理</h2>
        <div class="ops">
          <el-select v-model="status" clearable placeholder="全部状态" style="width: 150px">
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
          <el-button @click="load">筛选</el-button>
        </div>
      </div>

      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="studentName" label="学生" width="140" />
        <el-table-column prop="counselorName" label="咨询师" width="140" />
        <el-table-column prop="apptTime" label="预约时间" min-width="180" />
        <el-table-column prop="duration" label="时长(分钟)" width="110" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const rows = ref([])
const status = ref()

const parseData = (res) => {
  const body = res?.data || {}
  return body.data || body
}

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/appointments', { params: { status: status.value } })
    rows.value = parseData(res) || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '预约列表加载失败')
  } finally {
    loading.value = false
  }
}

const statusText = (v) => ({ 0: '待确认', 1: '已确认', 2: '已完成', 3: '已取消' }[v] || '未知')
const statusType = (v) => ({ 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[v] || 'info')

onMounted(load)
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px; gap: 10px; }
.ops { display:flex; gap: 8px; }
</style>
