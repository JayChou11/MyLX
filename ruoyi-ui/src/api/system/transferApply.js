import request from '@/utils/request'

// 查询转班申请列表
export function listTransferApply(query) {
  return request({
    url: '/system/student/transferApply/list',
    method: 'get',
    params: query
  })
}

// 查询待我审批的申请列表
export function listMyApprove(approveLevel, query) {
  return request({
    url: '/system/student/transferApply/myApprove/' + approveLevel,
    method: 'get',
    params: query
  })
}

// 查询转班申请详细
export function getTransferApply(applyId) {
  return request({
    url: '/system/student/transferApply/' + applyId,
    method: 'get'
  })
}

// 新增转班申请
export function addTransferApply(data) {
  return request({
    url: '/system/student/transferApply',
    method: 'post',
    data: data
  })
}

// 审批转班申请
export function approveTransferApply(data) {
  return request({
    url: '/system/student/transferApply/approve',
    method: 'put',
    data: data
  })
}

// 删除转班申请
export function delTransferApply(applyIds) {
  return request({
    url: '/system/student/transferApply/' + applyIds,
    method: 'delete'
  })
}

// 导出转班申请
export function exportTransferApply(query) {
  return request({
    url: '/system/student/transferApply/export',
    method: 'post',
    params: query
  })
}

// 统计待审批数量
export function getPendingCount(approveLevel) {
  return request({
    url: '/system/student/transferApply/pendingCount/' + approveLevel,
    method: 'get'
  })
}
