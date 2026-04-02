<template>
  <div class="page">
    <el-card>
      <div class="head">
        <h2>心理科普文章</h2>
        <p>由平台咨询师发布的心理健康知识，帮助你更好地理解和照顾自己。</p>
      </div>

      <el-empty v-if="!loading && list.length === 0" description="暂无已发布文章" />

      <div v-else class="grid" v-loading="loading">
        <div class="article-card" v-for="item in list" :key="item.id" @click="openDetail(item.id)">
          <div class="cover" :class="{ placeholder: !item.cover }">
            <img v-if="item.cover" :src="item.cover" alt="cover" />
            <span v-else>科普</span>
          </div>
          <div class="body">
            <h3>{{ item.title }}</h3>
            <p class="meta">作者：{{ item.authorName || '咨询师' }}</p>
            <p class="meta">发布时间：{{ formatTime(item.publishTime || item.createTime) }}</p>
            <p class="excerpt">{{ excerpt(item.content) }}</p>
          </div>
        </div>
      </div>
    </el-card>

  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const list = ref([])

const unwrapData = (body) => {
  if (!body) return null
  const d = body.data
  if (d && typeof d === 'object' && !Array.isArray(d) && Object.prototype.hasOwnProperty.call(d, 'data')) {
    return d.data
  }
  return d
}

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/student/articles')
    const body = res.data || {}
    const payload = unwrapData(body)
    list.value = Array.isArray(payload) ? payload : []
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '加载文章失败')
  } finally {
    loading.value = false
  }
}

const openDetail = async (id) => {
  if (!id || Number.isNaN(Number(id))) {
    ElMessage.warning('文章ID无效，请刷新后重试')
    return
  }
  router.push(`/student/articles/${id}`)
}

const excerpt = (text) => {
  if (!text) return '暂无内容'
  return text.length > 72 ? `${text.slice(0, 72)}...` : text
}

const formatTime = (t) => {
  if (!t) return '-'
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return t
  return d.toLocaleString('zh-CN', { hour12: false })
}

onMounted(load)
</script>

<style scoped>
.page { max-width: 1120px; margin: 0 auto; }
.head p { color: #6b7280; margin-top: 6px; }
.grid {
  margin-top: 14px;
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
}
.article-card {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  background: #fff;
  transition: all .2s ease;
}
.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 26px rgba(15, 23, 42, .1);
}
.cover {
  height: 150px;
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.cover img { width: 100%; height: 100%; object-fit: cover; }
.cover.placeholder span { color: #1e40af; font-weight: 700; letter-spacing: 1px; }
.body { padding: 14px; }
.body h3 { margin: 0 0 8px; color: #111827; }
.meta { margin: 0; color: #6b7280; font-size: 12px; }
.excerpt { margin: 10px 0 0; color: #374151; line-height: 1.6; }
</style>
