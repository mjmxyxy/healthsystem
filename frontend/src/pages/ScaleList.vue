<template>
  <div class="page">
    <el-card>
      <div class="head">
        <h2>量表列表</h2>
        <el-button @click="goHome">返回首页</el-button>
      </div>

      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索量表名称/描述" clearable />
        <el-select v-model="codeFilter" clearable placeholder="量表编码筛选" style="width: 220px">
          <el-option v-for="code in codeOptions" :key="code" :label="code" :value="code" />
        </el-select>
      </div>

      <div v-loading="loading" class="grid">
        <div v-for="s in filteredScales" :key="s.id" class="scale-card card-hover">
          <div>
            <h3 class="name">{{ s.name }}</h3>
            <p class="desc">{{ s.description }}</p>
            <div class="tags">
              <span class="tag tag-primary">{{ s.code || '通用量表' }}</span>
              <span class="tag">{{ (s.questionCount || 0) + ' 题' }}</span>
            </div>
          </div>
          <div>
            <el-button type="primary" class="start-btn" @click="start(s.id)">开始测评</el-button>
          </div>
        </div>
        <el-empty v-if="!loading && filteredScales.length === 0" description="暂无匹配量表" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const scales = ref([])
const loading = ref(false)
const keyword = ref('')
const codeFilter = ref('')
const router = useRouter()

const load = async () => {
  loading.value = true
  try {
    const r = await axios.get('/api/scale')
    const body = r.data || {}
    const list = body.data || body
    scales.value = Array.isArray(list) ? list : []
  } finally {
    loading.value = false
  }
}

const filteredScales = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  return scales.value.filter((s) => {
    const hitKeyword = !k || String(s.name || '').toLowerCase().includes(k) || String(s.description || '').toLowerCase().includes(k)
    const hitCode = !codeFilter.value || String(s.code || '') === codeFilter.value
    return hitKeyword && hitCode
  })
})

const codeOptions = computed(() => {
  const set = new Set()
  scales.value.forEach((s) => {
    if (s.code) set.add(String(s.code))
  })
  return Array.from(set)
})

const start = (id) => {
  const s = scales.value.find(x => x.id === id)
  router.push({ path: `/student/test/${id}`, query: { name: s?.name || '', code: s?.code || '' } })
}

const goHome = () => router.push('/student/home')

onMounted(load)
</script>

<style scoped>
.page { max-width:960px; margin: 32px auto; padding: 0 16px }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px }
.filters { display:flex; gap:12px; margin-bottom: 12px }
.grid { display:grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px }
.scale-card {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: #fff;
  padding: 16px;
  display:flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 210px;
}
.name { margin: 0 0 8px; color:#111827; font-size: 18px; font-weight: 600 }
.desc {
  margin:0;
  color:#6b7280;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.tags { margin-top: 12px; display:flex; gap:8px; flex-wrap: wrap }
.tag { border-radius: 999px; padding: 3px 10px; background:#f3f4f6; color:#374151; font-size: 12px }
.tag-primary { background: #dbeafe; color:#1d4ed8 }
.start-btn { width: 100%; margin-top: 16px }

@media (max-width: 768px) {
  .filters { flex-direction: column }
}
</style>
