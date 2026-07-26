import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'

const source = async (path) => readFile(new URL(path, import.meta.url), 'utf8')

test('开始充电请求将后端 @RequestParam 放入 URL 查询参数', async () => {
  const chargingApi = await source('../../src/api/charging.js')

  assert.match(chargingApi, /url: `\/api\/charging\/start\?reservationId=\$\{encodeURIComponent\(reservationId\)\}`/)
  assert.match(chargingApi, /url: `\/api\/charging\/start-direct\?portId=\$\{encodeURIComponent\(portId\)\}`/)
})

test('AI 助手页面从统一响应的 data 字段读取回答和建议', async () => {
  const assistantPage = await source('../../src/pages/assistant/index.vue')

  assert.match(assistantPage, /const data = res\.data/)
  assert.match(assistantPage, /content: data\.answer/)
  assert.match(assistantPage, /data\.suggestions/)
})

test('车棚详情覆盖后端定义的全部九种端口状态', async () => {
  const parkingDetail = await source('../../src/pages/parking/detail.vue')

  assert.match(parkingDetail, /'COMPLETED_WAITING_PICKUP': '已完成待取车'/)
  assert.match(parkingDetail, /'OVERTIME_OCCUPIED': '超时占用'/)
})

test('预约确认页不展示后端未接收的预约时段和预估费用字段', async () => {
  const reservationConfirm = await source('../../src/pages/reservation/confirm.vue')

  assert.doesNotMatch(reservationConfirm, /mode="time"/)
  assert.doesNotMatch(reservationConfirm, /预计时长/)
  assert.doesNotMatch(reservationConfirm, /预估费用/)
  assert.match(reservationConfirm, /createReservation\(Number\(portId\.value\)\)/)
})
