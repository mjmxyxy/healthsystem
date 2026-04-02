<template>
  <div class="page admin-page">
    <el-card>
      <div class="head">
        <h2>科普文章审核</h2>
        <div class="ops">
          <el-radio-group v-model="status" @change="load">
            <el-radio-button :label="''">全部</el-radio-button>
            <el-radio-button :label="1">待审核</el-radio-button>
            <el-radio-button :label="2">已通过</el-radio-button>
            <el-radio-button :label="3">已驳回</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="card-flow" v-loading="loading">
        <div class="article-card" v-for="row in rows" :key="row.id">
          <div class="cover">封面</div>
          <div class="info">
            <div class="title">{{ row.title }}</div>
            <div class="meta">作者：{{ row.authorName || '-' }} | {{ row.createTime || '-' }}</div>
            <div class="summary">{{ shortSummary(row.content) }}</div>
            <div class="btn-row">
              <el-button size="small" @click="preview(row)">预览</el-button>
              <el-button size="small" type="success" @click="review(row, 1)" :disabled="row.status === 2">通过</el-button>
              <el-button size="small" type="danger" @click="openReject(row)" :disabled="row.status === 3">驳回</el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="!rows.length" description="暂无文章" />
      </div>
    </el-card>

    <el-dialog v-model="showPreview" title="文章预览" width="820px">
      <h3>{{ current?.title }}</h3>
      <div class="meta">作者：{{ current?.authorName || '-' }}，状态：{{ current?.statusText || '-' }}</div>
      <el-divider />
      <div class="content">{{ current?.content }}</div>
    </el-dialog>

    <el-dialog v-model="showReject" title="驳回原因" width="460px">
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请填写驳回原因" />
      <template #footer>
        <el-button @click="showReject = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
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
const showReject = ref(false)
const rejectReason = ref('')
const pendingReject = ref(null)

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

const openReject = (row) => {
  pendingReject.value = row
  rejectReason.value = ''
  showReject.value = true
}

const confirmReject = () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  showReject.value = false
  review(pendingReject.value, 0, rejectReason.value.trim())
}

const review = async (row, approved, reason = '') => {
  try {
    await axios.post(`/api/admin/articles/${row.id}/review`, { approved, reason })
    ElMessage.success(approved ? '已通过' : '已驳回')
    await load()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '审核失败')
  }
}

const shortSummary = (txt = '') => {
  const s = String(txt || '').replace(/\s+/g, ' ').trim()
  return s.length > 100 ? `${s.slice(0, 100)}...` : s
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1200px; margin: 0 auto; }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px; gap: 10px; }
.ops { display:flex; gap: 8px; }
.meta { color:#6b7280; font-size: 13px; }
.content { white-space: pre-wrap; line-height: 1.8; color:#111827; }

.card-flow { display:grid; gap: 12px; }

.article-card {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 10px;
  display:grid;
  grid-template-columns: 120px 1fr;
  gap: 12px;
  background: #fff;
}

.cover {
  width: 120px;
  height: 80px;
  border-radius: 8px;
  background: linear-gradient(120deg, #fde68a, #fed7aa);
  display:flex;
  align-items:center;
  justify-content:center;
  color: #92400e;
}

.title { color: var(--color-text-primary); font-weight: 600; }
.summary { margin-top: 6px; color: var(--color-text-secondary); line-height: 1.7; }
.btn-row { margin-top: 8px; display:flex; gap: 8px; }

@media (max-width: 760px) {
  .article-card { grid-template-columns: 1fr; }
  .cover { width: 100%; }
}
</style>
