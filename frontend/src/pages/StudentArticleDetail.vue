<template>
  <div class="page" v-loading="loading">
    <el-card>
      <template v-if="loaded">
        <div class="header">
          <el-button text @click="goBack">返回文章列表</el-button>
          <h1>{{ article.title }}</h1>
          <p class="meta">
            作者：{{ article.authorName || '咨询师' }}
            <span class="dot">·</span>
            发布时间：{{ formatTime(article.publishTime || article.createTime) }}
          </p>
        </div>
        <div class="cover" v-if="article.cover">
          <img :src="article.cover" alt="cover" />
        </div>
        <div class="content">{{ article.content }}</div>
      </template>

      <el-empty v-else-if="!loading" description="文章不存在或已下线" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const loaded = ref(false)
const article = reactive({
  id: null,
  title: '',
  content: '',
  cover: '',
  authorName: '',
  publishTime: '',
  createTime: ''
})

const unwrapData = (body) => {
  if (!body) return null
  const d = body.data
  if (d && typeof d === 'object' && !Array.isArray(d) && Object.prototype.hasOwnProperty.call(d, 'data')) {
    return d.data
  }
  return d
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id || Number.isNaN(id)) {
    ElMessage.warning('文章ID无效')
    router.replace('/student/articles')
    return
  }
  loading.value = true
  try {
    const res = await axios.get(`/api/student/articles/${id}`)
    const row = unwrapData(res.data || {})
    if (!row || !row.id) {
      loaded.value = false
      return
    }
    article.id = row.id
    article.title = row.title || ''
    article.content = row.content || ''
    article.cover = row.cover || ''
    article.authorName = row.authorName || '咨询师'
    article.publishTime = row.publishTime || ''
    article.createTime = row.createTime || ''
    loaded.value = true
  } catch (error) {
    loaded.value = false
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '加载文章失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => router.push('/student/articles')

const formatTime = (t) => {
  if (!t) return '-'
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return t
  return d.toLocaleString('zh-CN', { hour12: false })
}

onMounted(loadDetail)
</script>

<style scoped>
.page { max-width: 960px; margin: 0 auto; }
.header h1 { margin: 10px 0 8px; color: #111827; }
.meta { color: #6b7280; font-size: 13px; }
.dot { margin: 0 8px; }
.cover { margin: 14px 0; border-radius: 12px; overflow: hidden; }
.cover img { width: 100%; display: block; }
.content { white-space: pre-wrap; line-height: 1.9; color: #1f2937; }
</style>
