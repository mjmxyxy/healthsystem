<template>
  <div class="page">
    <el-card>
      <div class="head">
        <h2>科普文章审核</h2>
        <div class="ops">
          <el-select v-model="status" clearable placeholder="全部状态" style="width: 170px">
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已发布" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
          <el-button @click="load">筛选</el-button>
        </div>
      </div>

      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="statusText" label="状态" width="100" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column prop="publishTime" label="发布时间" min-width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="preview(row)">预览</el-button>
            <el-button size="small" type="success" @click="review(row, 1)" :disabled="row.status === 2">通过</el-button>
            <el-button size="small" type="danger" @click="review(row, 0)" :disabled="row.status === 3">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showPreview" title="文章预览" width="760px">
      <h3>{{ current?.title }}</h3>
      <div class="meta">作者：{{ current?.authorName || '-' }}，状态：{{ current?.statusText || '-' }}</div>
      <el-divider />
      <div class="content">{{ current?.content }}</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const rows = ref([])
const status = ref()
const showPreview = ref(false)
const current = ref(null)

const parseData = (res) => {
  const body = res?.data || {}
  return body.data || body
}

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/admin/articles', { params: { status: status.value } })
    rows.value = parseData(res) || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '文章列表加载失败')
  } finally {
    loading.value = false
  }
}

const preview = (row) => {
  current.value = row
  showPreview.value = true
}

const review = async (row, approved) => {
  try {
    await axios.post(`/api/admin/articles/${row.id}/review`, { approved })
    ElMessage.success(approved ? '已通过' : '已驳回')
    await load()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '审核失败')
  }
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px; gap: 10px; }
.ops { display:flex; gap: 8px; }
.meta { color:#6b7280; font-size: 13px; }
.content { white-space: pre-wrap; line-height: 1.8; color:#111827; }
</style>
