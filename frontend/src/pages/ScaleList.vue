<template>
  <div class="page">
    <el-card>
      <div class="cover-hero">
        <div class="cover-copy">
          <div class="eyebrow">接纳自己 · 温柔前行</div>
          <h2>Love Yourself</h2>
          <p>量表不是给你贴标签，而是帮你更清楚地看见自己。</p>
        </div>
        <div class="cover-visual" aria-hidden="true"></div>
      </div>

      <div class="head">
        <h2>量表列表</h2>
        <el-button @click="goHome">返回首页</el-button>
      </div>

      <div class="search-row">
        <div class="search-pill">
          <span class="icon">🔍</span>
          <input v-model="keyword" placeholder="搜索量表名称/描述" />
          <span class="icon mic">🎤</span>
        </div>
      </div>

      <div class="category-scroll">
        <button
          v-for="item in categories"
          :key="item"
          class="category-btn"
          :class="{ active: categoryFilter === item }"
          @click="categoryFilter = item"
        >
          {{ item }}
        </button>
      </div>

      <div v-loading="loading" class="list">
        <div v-for="s in filteredScales" :key="s.id" class="scale-card card-hover">
          <div class="left-strip" :style="{ background: colorByCategory(categoryOf(s)) }"></div>
          <div class="content">
            <div class="top-row">
              <div class="name">{{ s.name }}</div>
              <span class="tag category">{{ categoryOf(s) }}</span>
            </div>
            <div class="desc">{{ s.description || '暂无量表描述' }}</div>
            <div class="meta-row">
              <span>{{ (s.questionCount || 0) + ' 题' }} • 预计{{ estimateMinutes(s.questionCount) }}分钟</span>
              <span class="difficulty" :class="difficulty(s).className">{{ difficulty(s).label }}</span>
              <el-button type="primary" class="start-btn" @click="start(s.id)">开始</el-button>
            </div>
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
const categoryFilter = ref('全部')
const router = useRouter()
const categories = ['全部', '焦虑', '抑郁', '压力', '人格', '其他']

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
    const c = categoryOf(s)
    const hitCategory = categoryFilter.value === '全部' || c === categoryFilter.value
    return hitKeyword && hitCategory
  })
})

const categoryOf = (s) => {
  const text = `${s?.name || ''} ${s?.description || ''} ${s?.code || ''}`.toLowerCase()
  if (/(anxiety|焦虑|sas|gad)/.test(text)) return '焦虑'
  if (/(depress|抑郁|phq|sds)/.test(text)) return '抑郁'
  if (/(stress|压力|pss)/.test(text)) return '压力'
  if (/(personality|人格|mbti|epi|big5)/.test(text)) return '人格'
  return '其他'
}

const estimateMinutes = (count) => Math.max(3, Math.ceil((Number(count || 0) || 10) / 2))

const difficulty = (s) => {
  const count = Number(s?.questionCount || 0)
  if (count <= 15) return { label: '简单', className: 'easy' }
  if (count <= 35) return { label: '适中', className: 'medium' }
  return { label: '深度', className: 'hard' }
}

const colorByCategory = (c) => {
  if (c === '焦虑') return 'linear-gradient(180deg, #8B9CF7 0%, #5B7CF5 100%)'
  if (c === '抑郁') return 'linear-gradient(180deg, #8ED7F2 0%, #56B4D3 100%)'
  if (c === '压力') return 'linear-gradient(180deg, #7AD7C5 0%, #4ECDC4 100%)'
  if (c === '人格') return 'linear-gradient(180deg, #BFA8FF 0%, #A78BFA 100%)'
  return 'linear-gradient(180deg, #D1D5DB 0%, #9CA3AF 100%)'
}

const start = (id) => {
  const s = scales.value.find(x => x.id === id)
  router.push({ path: `/student/test/${id}`, query: { name: s?.name || '', code: s?.code || '' } })
}

const goHome = () => router.push('/student/home')

onMounted(load)
</script>

<style scoped>
.page { max-width:1080px; margin: 0 auto; padding: 0 8px }

.cover-hero {
  margin-bottom: 16px;
  border-radius: 22px;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  min-height: 220px;
  background: linear-gradient(135deg, rgba(255,255,255,0.88), rgba(236, 253, 245, 0.92));
  border: 1px solid rgba(78, 205, 196, 0.16);
}

.cover-copy {
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.cover-copy h2 {
  margin: 8px 0 10px;
  font-size: clamp(28px, 4vw, 40px);
}

.cover-copy p {
  margin: 0;
  color: var(--color-text-secondary);
  max-width: 34ch;
  line-height: 1.7;
}

.eyebrow {
  display: inline-flex;
  align-self: flex-start;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(78, 205, 196, 0.12);
  color: #0f766e;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.cover-visual {
  min-height: 220px;
  background-image:
    linear-gradient(135deg, rgba(16, 185, 129, 0.12), rgba(245, 158, 11, 0.10)),
    url('/images/love.jpg');
  background-size: cover;
  background-position: center;
}

.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px }
.search-row { margin-bottom: 12px; }

.search-pill {
  height: 48px;
  border-radius: 24px;
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  padding: 0 12px;
  gap: 10px;
}

.search-pill input {
  border: none;
  outline: none;
  background: transparent;
  width: 100%;
  color: var(--color-text-primary);
  font-size: var(--font-size-base);
}

.icon { font-size: 16px; }
.mic { opacity: .8; }

.category-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  margin-bottom: 12px;
  padding-bottom: 2px;
}

.category-btn {
  height: 36px;
  border-radius: 18px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-primary);
  color: var(--color-text-secondary);
  padding: 0 14px;
  cursor: pointer;
  white-space: nowrap;
}

.category-btn.active {
  background: var(--color-student);
  border-color: var(--color-student);
  color: #fff;
}

.list { display:flex; flex-direction: column; gap: 12px; }
.scale-card {
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: var(--color-bg-primary);
  padding: 0;
  display:flex;
  min-height: 120px;
  overflow: hidden;
}

.left-strip {
  width: 80px;
  flex: 0 0 80px;
}

.content {
  flex: 1;
  padding: 14px;
}

.top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.name {
  margin: 0;
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.desc {
  margin: 6px 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.tag {
  border-radius: 999px;
  padding: 2px 10px;
  font-size: var(--font-size-xs);
}

.tag.category {
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
}

.difficulty {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.difficulty::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.difficulty.easy::before { background: var(--color-success); }
.difficulty.medium::before { background: var(--color-warning); }
.difficulty.hard::before { background: var(--color-danger); }

.start-btn {
  margin-left: auto;
  height: 34px;
  border-radius: 20px;
  padding: 0 14px;
}

@media (max-width: 768px) {
  .cover-hero {
    grid-template-columns: 1fr;
  }

  .cover-visual {
    min-height: 180px;
    order: -1;
  }

  .left-strip { width: 64px; flex-basis: 64px; }
  .meta-row {
    flex-wrap: wrap;
  }
}
</style>
