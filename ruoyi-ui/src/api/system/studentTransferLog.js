import request from '@/utils/request'

// 查询学生调班记录列表
export function listStudentTransferLog(query) {
  return request({
    url: '/system/student/transferLog/list',
    method: 'get',
    params: query
  })
}

// 查询学生调班记录详细
export function getStudentTransferLog(transferId) {
  return request({
    url: '/system/student/transferLog/' + transferId,
    method: 'get'
  })
}
