import { chromium } from 'playwright'
import { spawn, exec, execSync } from 'node:child_process'
import { setTimeout as sleep } from 'node:timers/promises'
import { promisify } from 'node:util'
const execAsync = promisify(exec)
import test from 'node:test'
import assert from 'node:assert/strict'

const DEV_PORT = 3188
// Vite 在当前 Windows 环境默认仅监听 localhost 的 IPv6 回环地址（::1），
// 因此测试基址必须与其监听主机保持一致。
const BASE = `http://localhost:${DEV_PORT}`

const ROUTES = [
  { path: '/login', label: '登录页', checkLoginBtn: true },
  { path: '/403', label: '无权访问页' },
  { path: '/permission/dashboard', label: '系统总览' },
  { path: '/permission/system/user', label: '用户管理' },
  { path: '/permission/system/role', label: '角色管理' },
  { path: '/permission/system/menu', label: '菜单管理' },
  { path: '/permission/system/dept', label: '部门管理' },
  { path: '/permission/device-category', label: '设备分类' },
  { path: '/permission/simulator', label: '设备模拟' },
  { path: '/permission/log', label: '操作日志' },
  { path: '/permission/notification', label: '消息通知' },
  { path: '/permission/statistics', label: '数据统计' },
  { path: '/ops/dashboard', label: '运维工作台' },
  { path: '/ops/community', label: '社区管理' },
  { path: '/ops/parking-area', label: '车棚管理' },
  { path: '/ops/device', label: '充电设备' },
  { path: '/ops/port', label: '充电端口' },
  { path: '/ops/reservation', label: '预约记录' },
  { path: '/ops/order', label: '订单监控' },
  { path: '/ops/repair-report', label: '报修记录' },
  { path: '/ops/repair-ticket', label: '维修工单' },
  { path: '/ops/inspection-task', label: '巡检任务' },
  { path: '/ops/hazard', label: '隐患整改' },
  { path: '/ops/statistics', label: '运维统计' },
  { path: '/ops/notification', label: '消息通知(ops)' },
  { path: '/bigscreen', label: '2D 管理大屏' },
  { path: '/bigscreen3d', label: '3D 管理大屏' },
  { path: '/resident/dashboard', label: '社区充电首页' },
]

// Windows 下通过 taskkill 强制结束整个进程树（SIGTERM 对 npx.cmd 派生的 shell 无效）
function killTree(pid) {
  return new Promise((resolve) => {
    if (process.platform === 'win32') {
      exec(`taskkill /PID ${pid} /F /T`, () => resolve())
    } else {
      try { process.kill(-pid, 'SIGKILL') } catch {}
      try { process.kill(pid, 'SIGKILL') } catch {}
      resolve()
    }
  })
}

let viteProcess
let browser

async function startVite() {
  // 清理可能的遗留进程
  try {
    execSync(`npx.cmd kill-port ${DEV_PORT}`, { stdio: 'ignore', timeout: 5000 })
  } catch { /* 端口空闲则无操作 */ }
  await sleep(800)

  // 直接运行本地 Vite CLI，避免 Windows 下 `npx.cmd + shell: true` 的进程树
  // 被 taskkill 误判，进而中断 Node 测试运行器。
  viteProcess = spawn(process.execPath, ['node_modules/vite/bin/vite.js', '--port', String(DEV_PORT), '--strictPort'], {
    cwd: process.cwd(),
    stdio: ['ignore', 'pipe', 'pipe'],
    shell: false,
    windowsHide: true,
    env: { ...process.env, VITE_USE_MOCK: 'false' }
  })

  viteProcess.stdout.on('data', (d) => process.stdout.write(`[vite] ${d}`))
  viteProcess.stderr.on('data', (d) => process.stderr.write(`[vite] ${d}`))

  // 条件轮询替代固定时间等待，最多等 15s
  const deadline = Date.now() + 15000
  while (Date.now() < deadline) {
    await sleep(400)
    try {
      const resp = await fetch(`${BASE}/login`, { signal: AbortSignal.timeout(1500) })
      if (resp.status === 200) return
    } catch { /* 尚未就绪 */ }
  }
  await stopVite()
  throw new Error('Vite dev server 未能启动')
}

async function stopVite() {
  if (!viteProcess) return
  const proc = viteProcess
  viteProcess = null
  try {
    if (!proc.killed) proc.kill()
  } catch { /* ignore */ }
  await killTree(proc.pid)
  // 给 OS 释放端口的时间
  await sleep(300)
}

test.before(async () => {
  console.log('Starting Vite…')
  await startVite()
  browser = await chromium.launch({ headless: true })
})

test.after(async () => {
  try { await browser?.close() } catch {}
  await stopVite()
})

for (const { path, label, checkLoginBtn } of ROUTES) {
  test(label, async (t) => {
    if (!browser) throw new Error('browser 未初始化（before hook 可能失败）')
    const ctx = await browser.newContext({ ignoreHTTPSErrors: true })
    const page = await ctx.newPage()
    const errs = []
    page.on('pageerror', (e) => errs.push(e.message))
    page.on('console', (msg) => { if (msg.type() === 'error') errs.push(`[console] ${msg.text()}`) })

    try {
      const resp = await page.goto(`${BASE}${path}`, {
        waitUntil: 'domcontentloaded',
        timeout: 15000
      })
      assert.ok(resp, `${label}: 无 HTTP 响应`)
      assert.equal(resp.status(), 200, `${label}: HTTP ${resp.status()}`)

      // Vue SPA 挂载 + 路由初始化等待
      try {
        await page.waitForFunction(
          () => !!document.querySelector('#app') && document.querySelector('#app').children.length > 0,
          { timeout: 8000 }
        )
      } catch {
        t.diagnostic(`${label}: #app 8s 内未挂载`)
      }

      const html = await page.evaluate(() => document.body?.innerHTML?.trim?.() ?? '')
      assert.ok(html.length > 0, `${label}: body 为空（Vue mount 失败）`)

      if (checkLoginBtn) {
        const btn = await page.$('button:has-text("登录"), .login-button, [type=submit], .el-button--primary')
        assert.ok(btn, `${label}: 应含登录按钮`)
      }

      if (errs.length) t.diagnostic(`${label} JS errors: ${JSON.stringify(errs).slice(0, 500)}`)
    } catch (err) {
      // 打印真实错误便于诊断，再重新抛出让测试标记失败
      t.diagnostic(`${label} 失败详情: ${err?.message ?? String(err)}`)
      throw err
    } finally {
      await ctx.close()
    }
  })
}
