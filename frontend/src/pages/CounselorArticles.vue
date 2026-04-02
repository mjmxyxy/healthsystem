<template>
  <div class="page">
    <el-card>
      <div class="head">
        <h2>科普文章发布</h2>
        <div>
          <el-button @click="resetForm">新建文章</el-button>
          <el-button type="primary" @click="saveDraft">保存草稿</el-button>
          <el-button type="success" @click="publishArticle">提交审核</el-button>
        </div>
      </div>

      <el-row :gutter="16">
        <el-col :xs="24" :md="10">
          <el-form :model="form" label-width="80px">
            <el-form-item label="标题">
              <el-input v-model="form.title" placeholder="请输入文章标题" />
            </el-form-item>
            <el-form-item label="封面URL">
              <el-input v-model="form.cover" placeholder="可选" />
            </el-form-item>
            <el-form-item label="正文">
              <el-input v-model="form.content" type="textarea" :rows="14" placeholder="请输入科普文章内容" />
            </el-form-item>
          </el-form>
        </el-col>

        <el-col :xs="24" :md="14">
          <el-table :data="list" v-loading="loading" height="560">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="statusText" label="状态" width="100" />
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column prop="publishTime" label="发布时间" width="170" />
            <el-table-column label="操作" width="240">
              <template #default="{ row }">
                <div class="table-actions cols-3">
                  <el-button size="small" @click="edit(row)">编辑</el-button>
                  <el-button size="small" type="success" @click="publish(row)">提交审核</el-button>
                  <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const form = reactive({ id: null, title: '', cover: '', content: '' })

const load = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/counselor/articles')
    const body = res.data || {}
    list.value = body.data || body
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '文章列表加载失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.id = null
  form.title = ''
  form.cover = ''
  form.content = ''
}

const edit = (row) => {
  form.id = row.id
  form.title = row.title || ''
  form.cover = row.cover || ''
  form.content = row.content || ''
}

const saveDraft = async () => {
  try {
    await axios.post('/api/counselor/articles', { ...form, status: 0 })
    ElMessage.success('草稿已保存')
    resetForm()
    await load()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '保存草稿失败')
  }
}

const publishArticle = async () => {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.error('标题和正文不能为空')
    return
  }
  try {
    await axios.post('/api/counselor/articles', { ...form, status: 1 })
    ElMessage.success('已提交审核')
    resetForm()
    await load()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '提交审核失败')
  }
}

const publish = async (row) => {
  try {
    await axios.post(`/api/counselor/articles/${row.id}/publish`)
    ElMessage.success('已提交审核')
    await load()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '提交失败')
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除这篇文章吗？', '提示', { type: 'warning' })
    await axios.delete(`/api/counselor/articles/${row.id}`)
    ElMessage.success('已删除')
    await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '删除失败')
    }
  }
}

onMounted(load)
</script>

<style scoped>
.page { max-width:1080px; margin:0 auto }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px }
</style>
