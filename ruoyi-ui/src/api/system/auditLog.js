import request from '@/utils/request'

// 查询审计日志列表
export function listAuditLog(query) {
  return request({
    url: '/system/student/audit/list',
    method: 'get',
    params: query
  })
}

// 导出审计日志
export function exportAuditLog(query) {
  return request({
    url: '/system/student/audit/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}