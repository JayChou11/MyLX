import request from '@/utils/request'

// 查询考试批次列表
export function listExam(query) {
  return request({
    url: '/system/exam/list',
    method: 'get',
    params: query
  })
}

