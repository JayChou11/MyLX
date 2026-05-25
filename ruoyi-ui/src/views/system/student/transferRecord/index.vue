<template>
  <div class="app-container">
    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="92px">
      <el-form-item label="调班后年级" prop="afterGrade">
        <el-input
          v-model="queryParams.afterGrade"
          placeholder="请输入调班后年级"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="调班后班级" prop="afterClassName">
        <el-input
          v-model="queryParams.afterClassName"
          placeholder="请输入调班后班级"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作人" prop="transferBy">
        <el-input
          v-model="queryParams.transferBy"
          placeholder="请输入操作人"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="调班时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 260px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:student:transfer:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="transferLogList">
      <el-table-column label="记录ID" align="center" prop="transferId" width="90" />
      <el-table-column label="调班人数" align="center" prop="studentCount" width="90" />
      <el-table-column label="调班前年级" align="center" prop="beforeGrade" min-width="140" show-overflow-tooltip />
      <el-table-column label="调班前班级" align="center" prop="beforeClassName" min-width="160" show-overflow-tooltip />
      <el-table-column label="调班后年级" align="center" prop="afterGrade" min-width="120" />
      <el-table-column label="调班后班级" align="center" prop="afterClassName" min-width="120" />
      <el-table-column label="调班原因" align="center" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作人" align="center" prop="transferBy" width="110" />
      <el-table-column label="调班时间" align="center" prop="transferTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.transferTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="100" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['system:student:transfer:query']">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog title="调班记录详情" v-model="detailOpen" width="760px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="记录ID">{{ detail.transferId }}</el-descriptions-item>
        <el-descriptions-item label="调班人数">{{ detail.studentCount }}</el-descriptions-item>
        <el-descriptions-item label="调班前年级">{{ detail.beforeGrade }}</el-descriptions-item>
        <el-descriptions-item label="调班前班级">{{ detail.beforeClassName }}</el-descriptions-item>
        <el-descriptions-item label="调班后年级">{{ detail.afterGrade }}</el-descriptions-item>
        <el-descriptions-item label="调班后班级">{{ detail.afterClassName }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.transferBy }}</el-descriptions-item>
        <el-descriptions-item label="调班时间">{{ parseTime(detail.transferTime) }}</el-descriptions-item>
        <el-descriptions-item label="学生ID集合" :span="2">{{ detail.studentIds }}</el-descriptions-item>
        <el-descriptions-item label="调班原因" :span="2">{{ detail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="StudentTransferRecord">
import { listStudentTransferLog, getStudentTransferLog } from "@/api/system/studentTransferLog"

const { proxy } = getCurrentInstance()

const showSearch = ref(true)
const loading = ref(false)
const total = ref(0)
const transferLogList = ref([])
const detailOpen = ref(false)
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    afterGrade: undefined,
    afterClassName: undefined,
    transferBy: undefined,
    params: {}
  },
  detail: {}
})

const { queryParams, detail } = toRefs(data)

function buildQueryParams() {
  const params = { ...queryParams.value, params: {} }
  if (dateRange.value && dateRange.value.length === 2) {
    params.params.beginTime = dateRange.value[0]
    params.params.endTime = dateRange.value[1]
  }
  return params
}

function getList() {
  loading.value = true
  listStudentTransferLog(buildQueryParams()).then(response => {
    transferLogList.value = response.rows
    total.value = response.total
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryRef")
  queryParams.value.params = {}
  handleQuery()
}

function handleView(row) {
  getStudentTransferLog(row.transferId).then(response => {
    detail.value = response.data || {}
    detailOpen.value = true
  })
}

function handleExport() {
  proxy.download("system/student/transferLog/export", buildQueryParams(), `student_transfer_record_${new Date().getTime()}.xlsx`)
}

getList()
</script>
