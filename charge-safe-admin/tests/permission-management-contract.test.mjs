import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'

const userPage = new URL('../src/views/system/User.vue', import.meta.url)
const menuPage = new URL('../src/views/system/Menu.vue', import.meta.url)

test('用户页按后端 roleId 参数筛选，并渲染 UserVO 的角色数组', async () => {
  const source = await readFile(userPage, 'utf8')

  assert.match(source, /roleId:\s*null/)
  assert.match(source, /row\.roleNames\?\.length/)
  assert.match(source, /row\.roleNames\.join\(/)
  assert.doesNotMatch(source, /queryParams\.roleCode/)
})

test('菜单页使用后端 permissionCode 与数值菜单类型契约', async () => {
  const source = await readFile(menuPage, 'utf8')

  assert.match(source, /v-model="form\.permissionCode"/)
  assert.match(source, /prop="permissionCode"/)
  assert.match(source, /:value="1"[^>]*>目录/)
  assert.match(source, /:value="2"[^>]*>菜单/)
  assert.match(source, /:value="3"[^>]*>按钮/)
  assert.doesNotMatch(source, /form\.permission\b/)
})
