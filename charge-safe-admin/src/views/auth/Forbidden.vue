<template>
  <main class="forbidden-page">
    <section class="forbidden-card" aria-labelledby="forbidden-title">
      <p class="eyebrow">访问受限</p>
      <h1 id="forbidden-title">当前账号没有此页面权限</h1>
      <p>系统已保留你的登录状态。请返回角色默认首页，或联系管理员核对授权。</p>
      <el-button type="primary" size="large" @click="router.push('/')">返回首页</el-button>
    </section>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.forbidden-page {
  min-height: 100dvh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: #f4f6f3;
  color: #18231c;
}

.forbidden-card {
  width: min(560px, 100%);
  padding: 40px;
  border: 1px solid #d9e0da;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 16px 40px rgba(33, 52, 40, 0.08);
}

.eyebrow {
  margin-bottom: 10px;
  color: #8a4b20;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

h1 {
  margin-bottom: 14px;
  font-size: clamp(26px, 5vw, 38px);
  line-height: 1.2;
}

.forbidden-card > p:not(.eyebrow) {
  margin-bottom: 28px;
  color: #536158;
  font-size: 16px;
  line-height: 1.7;
}
</style>
