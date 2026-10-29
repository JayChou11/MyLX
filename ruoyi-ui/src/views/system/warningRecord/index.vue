<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="学生ID" prop="studentId">
        <el-input v-model="queryParams.studentId" placeholder="请输入学生ID" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="考试ID" prop="examId">
        <el-input v-model="queryParams.examId" placeholder="请输入考试ID" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="预警类型" prop="warningType">
        <el-select v-model="queryParams.warningType" placeholder="请选择预警类型" clearable style="width: 180px">
          <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警等级" prop="warningLevel">
        <el-select v-model="queryParams.warningLevel" placeholder="请选择预警等级" clearable style="width: 180px">
          <el-option v-for="item in levelOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警状态" prop="warningStatus">
        <el-select v-model="queryParams.warningStatus" placeholder="请选择预警状态" clearable style="width: 180px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="预警项目" prop="warningItem">
        <el-input v-model="queryParams.warningItem" placeholder="请输入预警项目" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="处理人" prop="handlerName">
        <el-input v-model="queryParams.handlerName" placeholder="请输入处理人" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="!ids.length || loading" @click="handleDelete()" v-hasPermi="['system:warning-record:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:warning-record:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="warningList" row-key="warningId" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="预警ID" prop="warningId" align="center" width="90" />
      <el-table-column label="学生ID" prop="studentId" align="center" width="90" />
      <el-table-column label="考试ID" prop="examId" align="center" width="90" />
      <el-table-column label="预警类型" align="center" width="120">
        <template #default="{ row }">{{ optionLabel(typeOptions, row.warningType) }}</template>
      </el-table-column>
      <el-table-column label="预警等级" align="center" width="95">
        <template #default="{ row }">
          <el-tag :type="optionTag(levelOptions, row.warningLevel)">{{ optionLabel(levelOptions, row.warningLevel) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="预警项目" prop="warningItem" align="center" min-width="110" show-overflow-tooltip />
      <el-table-column label="预警原因" prop="warningReason" min-width="240" show-overflow-tooltip />
      <el-table-column label="预警状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="optionTag(statusOptions, row.warningStatus)">{{ optionLabel(statusOptions, row.warningStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理结果" prop="handleResult" min-width="180" show-overflow-tooltip />
      <el-table-column label="处理人" prop="handlerName" align="center" width="110" />
      <el-table-column label="处理时间" align="center" width="165">
        <template #default="{ row }">{{ parseTime(row.handleTime) || '-' }}</template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" width="165">
        <template #default="{ row }">{{ parseTime(row.createTime) || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" icon="View" @click="handleView(row)" v-hasPermi="['system:warning-record:query']">详情</el-button>
          <el-button v-if="String(row.warningStatus) === '0'" link type="primary" icon="Edit" @click="openHandle(row)" v-hasPermi="['system:warning-record:edit']">处理</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:warning-record:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="预警记录详情" v-model="detailOpen" width="780px" append-to-body>
      <el-descriptions :column="2" border class="warning-detail">
        <el-descriptions-item label="预警ID">{{ detail.warningId }}</el-descriptions-item>
        <el-descriptions-item label="成绩ID">{{ detail.scoreId }}</el-descriptions-item>
        <el-descriptions-item label="学生ID">{{ detail.studentId }}</el-descriptions-item>
        <el-descriptions-item label="考试ID">{{ detail.examId }}</el-descriptions-item>
        <el-descriptions-item label="预警类型">{{ optionLabel(typeOptions, detail.warningType) }}</el-descriptions-item>
        <el-descriptions-item label="预警等级">{{ optionLabel(levelOptions, detail.warningLevel) }}</el-descriptions-item>
        <el-descriptions-item label="预警项目">{{ detail.warningItem || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预警状态">{{ optionLabel(statusOptions, detail.warningStatus) }}</el-descriptions-item>
        <el-descriptions-item label="预警原因" :span="2">{{ detail.warningReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2">{{ detail.handleResult || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ detail.handlerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ parseTime(detail.handleTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(detail.createTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ parseTime(detail.updateTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailOpen = false">关 闭</el-button></template>
    </el-dialog>

    <el-dialog title="处理预警" v-model="handleOpen" width="580px" append-to-body :close-on-click-modal="false" :close-on-press-escape="!submitting" :show-close="!submitting" @closed="resetHandle">
      <el-form ref="handleRef" :model="handleForm" :rules="handleRules" label-width="90px">
        <el-form-item label="预警ID">{{ handleForm.warningId }}</el-form-item>
        <el-form-item label="预警原因"><span class="warning-reason">{{ handleReason || '-' }}</span></el-form-item>
        <el-form-item label="处理状态" prop="warningStatus">
          <el-radio-group v-model="handleForm.warningStatus" :disabled="submitting">
            <el-radio value="1">已处理</el-radio>
            <el-radio value="2">已忽略</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理结果" prop="handleResult">
          <el-input v-model="handleForm.handleResult" type="textarea" :rows="4" maxlength="500" show-word-limit :disabled="submitting" placeholder="请填写处理措施或忽略原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="submitting" :disabled="submitting" @click="submitHandle">确 定</el-button>
        <el-button :disabled="submitting" @click="handleOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SysWarningRecord">
import { parseTime } from '@/utils/ruoyi'
import { listWarningRecord, getWarningRecord, updateWarningRecord, delWarningRecord } from '@/api/system/warningRecord'

const { proxy } = getCurrentInstance()
const showSearch = ref(true)
const loading = ref(false)
const warningList = ref([])
const total = ref(0)
const ids = ref([])
const detailOpen = ref(false)
const detail = ref({})
const handleOpen = ref(false)
const submitting = ref(false)
const handleReason = ref('')
const handleForm = ref({ warningId: undefined, warningStatus: '1', handleResult: '' })
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  studentId: undefined,
  examId: undefined,
  warningType: undefined,
  warningLevel: undefined,
  warningStatus: undefined,
  warningItem: undefined,
  handlerName: undefined
})

const typeOptions = [
  { value: 'average_low', label: '低于平均分' },
  { value: 'subject_low', label: '单科不及格' },
  { value: 'total_low', label: '总分过低' },
  { value: 'rank_drop', label: '排名下降' }
]
const levelOptions = [
  { value: '1', label: '低', tag: 'info' },
  { value: '2', label: '中', tag: 'warning' },
  { value: '3', label: '高', tag: 'danger' }
]
const statusOptions = [
  { value: '0', label: '待处理', tag: 'warning' },
  { value: '1', label: '已处理', tag: 'success' },
  { value: '2', label: '已忽略', tag: 'info' }
]
const handleRules = {
  warningStatus: [{ required: true, message: '请选择处理状态', trigger: 'change' }],
  handleResult: [
    { required: true, whitespace: true, message: '请填写处理结果或忽略原因', trigger: 'blur' },
    { max: 500, message: '处理结果不能超过500个字符', trigger: 'blur' }
  ]
}

function optionLabel(options, value) {
  return options.find(item => item.value === String(value))?.label || value || '-'
}

function optionTag(options, value) {
  return options.find(item => item.value === String(value))?.tag || 'info'
}

// 列表与导出共用筛选；不向现有后端传递尚未支持的姓名、日期等条件。
function buildQuery() {
  const params = { ...queryParams.value }
  for (const key of ['studentId', 'examId']) {
    const value = String(params[key] ?? '').trim()
    if (value && !/^[1-9]\d{0,18}$/.test(value)) {
      proxy.$modal.msgWarning(`${key === 'studentId' ? '学生' : '考试'}ID请输入正整数`)
      return null
    }
    params[key] = value || undefined
  }
  return params
}

let listRequestId = 0
async function getList() {
  const params = buildQuery()
  if (!params) return
  const requestId = ++listRequestId
  loading.value = true
  ids.value = []
  try {
    const response = await listWarningRecord(params)
    if (requestId !== listRequestId) return
    warningList.value = response.rows || []
    total.value = response.total || 0
  } catch {
    // 请求错误由统一响应拦截器提示。
  } finally {
    if (requestId === listRequestId) loading.value = false
  }
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.warningId)
}

async function handleView(row) {
  try {
    const response = await getWarningRecord(row.warningId)
    if (!response.data) {
      proxy.$modal.msgWarning('该预警记录已不存在，请刷新列表')
      getList()
      return
    }
    detail.value = response.data
    detailOpen.value = true
  } catch {
    // 请求错误由统一响应拦截器提示。
  }
}

function resetHandle() {
  handleForm.value = { warningId: undefined, warningStatus: '1', handleResult: '' }
  handleReason.value = ''
  proxy.$refs.handleRef?.clearValidate()
}

function openHandle(row) {
  resetHandle()
  handleForm.value.warningId = row.warningId
  handleReason.value = row.warningReason
  handleOpen.value = true
}

async function submitHandle() {
  if (submitting.value) return
  submitting.value = true
  try {
    await proxy.$refs.handleRef.validate()
    // 只提交处理字段，不把列表整行回传，避免修改预警类型、原因等字段。
    // 处理人和处理时间由后端确定。
    await updateWarningRecord({
      warningId: handleForm.value.warningId,
      warningStatus: handleForm.value.warningStatus,
      handleResult: handleForm.value.handleResult.trim()
    })
    proxy.$modal.msgSuccess('处理成功')
    handleOpen.value = false
    getList()
  } catch {
    // 表单校验显示在字段下方；接口错误由统一响应拦截器提示。
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  const warningIds = row ? [row.warningId] : [...ids.value]
  if (!warningIds.length) return
  try {
    await proxy.$modal.confirm(`是否确认删除预警编号为"${warningIds.join(',')}"的记录？`)
    await delWarningRecord(warningIds.join(','))
    proxy.$modal.msgSuccess('删除成功')
    const remaining = Math.max(0, total.value - warningIds.length)
    queryParams.value.pageNum = Math.min(queryParams.value.pageNum, Math.max(1, Math.ceil(remaining / queryParams.value.pageSize)))
    getList()
  } catch {
    // 取消删除无需提示；接口错误由统一响应拦截器提示。
  }
}

function handleExport() {
  const params = buildQuery()
  if (!params) return
  delete params.pageNum
  delete params.pageSize
  proxy.download('system/warning-record/export', params, `warning_record_${Date.now()}.xlsx`)
}

getList()
</script>

<style scoped>
.warning-reason,
.warning-detail :deep(.el-descriptions__content) {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
</style>
