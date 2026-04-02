<template>
  <div class="page">
    <el-row :gutter="14">
      <el-col :xs="24" :md="9">
        <el-card>
          <div class="head">
            <h2>量表管理</h2>
            <el-button type="primary" @click="openScale(null)">新建量表</el-button>
          </div>
          <el-table :data="scales" v-loading="loadingScales" height="540" @row-click="pickScale">
            <el-table-column prop="code" label="编码" width="120" />
            <el-table-column prop="name" label="名称" min-width="130" />
            <el-table-column prop="questionCount" label="题数" width="70" />
            <el-table-column width="90" label="操作">
              <template #default="{ row }">
                <el-button text type="primary" @click.stop="openScale(row)">编辑</el-button>
                <el-button text type="danger" @click.stop="removeScale(row)">删</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="15">
        <el-card>
          <div class="head">
            <h2>题目管理</h2>
            <el-button type="primary" :disabled="!currentScale" @click="openQuestion(null)">新增题目</el-button>
          </div>
          <div v-if="!currentScale" class="muted">请先选择左侧量表</div>
          <el-table v-else :data="questions" v-loading="loadingQuestions" height="540">
            <el-table-column prop="seq" label="序号" width="70" />
            <el-table-column prop="text" label="题干" min-width="220" />
            <el-table-column prop="dimension" label="维度" width="110" />
            <el-table-column prop="reverseScore" label="反向" width="80" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button size="small" @click="openQuestion(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="removeQuestion(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showScaleDialog" :title="scaleForm.id ? '编辑量表' : '新建量表'" width="520px">
      <el-form :model="scaleForm" label-width="90px">
        <el-form-item label="编码"><el-input v-model="scaleForm.code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="scaleForm.name" /></el-form-item>
        <el-form-item label="描述"><el-input type="textarea" :rows="3" v-model="scaleForm.description" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showScaleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveScale">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showQuestionDialog" :title="questionForm.id ? '编辑题目' : '新增题目'" width="620px">
      <el-form :model="questionForm" label-width="90px">
        <el-form-item label="序号"><el-input-number v-model="questionForm.seq" :min="1" /></el-form-item>
        <el-form-item label="题干"><el-input v-model="questionForm.text" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="选项JSON"><el-input v-model="questionForm.options" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="维度"><el-input v-model="questionForm.dimension" /></el-form-item>
        <el-form-item label="反向计分"><el-switch v-model="reverseSwitch" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showQuestionDialog = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const scales = ref([])
const questions = ref([])
const currentScale = ref(null)
const loadingScales = ref(false)
const loadingQuestions = ref(false)
const showScaleDialog = ref(false)
const showQuestionDialog = ref(false)
const reverseSwitch = ref(false)

const scaleForm = reactive({ id: null, code: '', name: '', description: '' })
const questionForm = reactive({ id: null, seq: 1, text: '', options: '[]', dimension: '', reverseScore: 0 })

const parseData = (res) => {
  const body = res?.data || {}
  return body.data || body
}

const loadScales = async () => {
  loadingScales.value = true
  try {
    const res = await axios.get('/api/admin/scales')
    scales.value = parseData(res) || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '量表加载失败')
  } finally {
    loadingScales.value = false
  }
}

const pickScale = async (row) => {
  currentScale.value = row
  await loadQuestions(row.id)
}

const loadQuestions = async (scaleId) => {
  loadingQuestions.value = true
  try {
    const res = await axios.get(`/api/admin/scales/${scaleId}/questions`)
    questions.value = parseData(res) || []
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '题目加载失败')
  } finally {
    loadingQuestions.value = false
  }
}

const openScale = (row) => {
  scaleForm.id = row?.id || null
  scaleForm.code = row?.code || ''
  scaleForm.name = row?.name || ''
  scaleForm.description = row?.description || ''
  showScaleDialog.value = true
}

const saveScale = async () => {
  try {
    await axios.post('/api/admin/scales', { ...scaleForm })
    ElMessage.success('保存成功')
    showScaleDialog.value = false
    await loadScales()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '保存失败')
  }
}

const removeScale = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除量表 ${row.name} 吗？`, '提示', { type: 'warning' })
    await axios.delete(`/api/admin/scales/${row.id}`)
    ElMessage.success('删除成功')
    if (currentScale.value?.id === row.id) {
      currentScale.value = null
      questions.value = []
    }
    await loadScales()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '删除失败')
    }
  }
}

const openQuestion = (row) => {
  questionForm.id = row?.id || null
  questionForm.seq = row?.seq || 1
  questionForm.text = row?.text || ''
  questionForm.options = row?.options || '[]'
  questionForm.dimension = row?.dimension || ''
  questionForm.reverseScore = row?.reverseScore || 0
  reverseSwitch.value = !!questionForm.reverseScore
  showQuestionDialog.value = true
}

const saveQuestion = async () => {
  if (!currentScale.value) return
  questionForm.reverseScore = reverseSwitch.value ? 1 : 0
  try {
    if (!questionForm.id) {
      await axios.post(`/api/admin/scales/${currentScale.value.id}/questions`, { ...questionForm })
    } else {
      await axios.put(`/api/admin/questions/${questionForm.id}`, { ...questionForm })
    }
    ElMessage.success('保存成功')
    showQuestionDialog.value = false
    await loadQuestions(currentScale.value.id)
    await loadScales()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '保存失败')
  }
}

const removeQuestion = async (row) => {
  try {
    await axios.delete(`/api/admin/questions/${row.id}`)
    ElMessage.success('删除成功')
    await loadQuestions(currentScale.value.id)
    await loadScales()
  } catch (error) {
    ElMessage.error(error?.response?.data?.msg || error?.response?.data?.error || '删除失败')
  }
}

onMounted(loadScales)
</script>

<style scoped>
.page { max-width: 1220px; margin: 0 auto; }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom: 12px; }
.muted { color: #6b7280; padding: 12px 0; }
</style>
