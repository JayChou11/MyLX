import request from '@/utils/request'

// 查询持久化预警记录，区别于 studentScore.js 中的实时成绩预警。
export function listWarningRecord(query) {
  return request({
    url: '/system/warning-record/list',
    method: 'get',
    params: query
  })
}

export function getWarningRecord(warningId) {
  return request({
    url: '/system/warning-record/' + warningId,
    method: 'get'
  })
}

// 暂时沿用现有 PUT 接口，后续可在此替换为专门的处理接口。
export function updateWarningRecord(data) {
  return request({
    url: '/system/warning-record',
    method: 'put',
    data
  })
}

export function delWarningRecord(warningIds) {
  return request({
    url: '/system/warning-record/' + warningIds,
    method: 'delete'
  })
}
