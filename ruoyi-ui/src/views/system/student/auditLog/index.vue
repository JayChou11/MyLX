<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="业务类型" prop="businessType">
        <el-select v-model="queryParams.businessType" placeholder="请选择业务类型" clearable style="width: 200px">
          <el-option label="调班" value="TRANSFER" />
          <el-option label="升年级" value="UPGRADE" />
          <el-option label="删除学生" value="DELETE" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作人" prop="operName">
        <el-input v-model="queryParams.operName" placeholder="请输入操作人" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="操作状态" clearable style="width: 200px">
          <el-option label="成功" value="0" />
          <el-option label="失败" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间" style="width: 308px">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:student:audit:export']">导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="auditLogList">
      <el-table-column label="ID" align="center" prop="auditId" width="80" />
      <el-table-column label="业务类型" align="center" prop="businessType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.businessType === 'TRANSFER'" type="warning">调班</el-tag>
          <el-tag v-else-if="scope.row.businessType === 'UPGRADE'" type="success">升年级</el-tag>
          <el-tag v-else-if="scope.row.businessType === 'DELETE'" type="danger">删除</el-tag>
          <el-tag v-else>{{ scope.row.businessType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="业务描述" align="center" prop="detail" min-width="100" />
      <el-table-column label="变更摘要" align="center" prop="changeSummary" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作人" align="center" prop="operName" width="100" />
      <el-table-column label="操作IP" align="center" prop="operIp" width="130" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="success">成功</el-tag>
          <el-tag v-else type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作时间" align="center" prop="operTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.operTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="80">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详细</el-button>
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

    <!-- 审计日志详情对话框 -->
    <el-dialog title="审计日志详情" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="业务类型">
          <el-tag v-if="detailRow.businessType === 'TRANSFER'" type="warning">调班</el-tag>
          <el-tag v-else-if="detailRow.businessType === 'UPGRADE'" type="success">升年级</el-tag>
          <el-tag v-else-if="detailRow.businessType === 'DELETE'" type="danger">删除</el-tag>
          <el-tag v-else>{{ detailRow.businessType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="业务描述">{{ detailRow.detail }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailRow.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ detailRow.operIp }}</el-descriptions-item>
        <el-descriptions-item label="操作方法" :span="2">{{ detailRow.method }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ parseTime(detailRow.operTime) }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag v-if="detailRow.status === 0" type="success">成功</el-tag>
          <el-tag v-else type="danger">失败</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="变更摘要" :span="2">{{ detailRow.changeSummary }}</el-descriptions-item>
        <el-descriptions-item label="变更前数据" :span="2">
          <pre class="json-data">{{ formatJson(detailRow.beforeData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="变更后数据" :span="2">
          <pre class="json-data">{{ formatJson(detailRow.afterData) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailRow.errorMsg" label="错误消息" :span="2">
          <span style="color: #f56c6c">{{ detailRow.errorMsg }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="StudentAuditLog">
import { listAuditLog } from "@/api/system/auditLog"

const { proxy } = getCurrentInstance()

const auditLogList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const detailRow = ref({})
const dateRange = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    businessType: undefined,
    operName: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listAuditLog(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    auditLogList.value = response.rows
    total.value = response.total
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
  handleQuery()
}

function handleDetail(row) {
  detailRow.value = row
  detailOpen.value = true
}

function handleExport() {
  proxy.download("system/student/audit/export", {
    ...queryParams.value
  }, `audit_log_${new Date().getTime()}.xlsx`)
}

function formatJson(jsonStr) {
  if (!jsonStr) return ''
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch (e) {
    return jsonStr
  }
}

getList()
</script>

<style scoped>
.json-data {
  max-height: 200px;
  overflow: auto;
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}
</style>