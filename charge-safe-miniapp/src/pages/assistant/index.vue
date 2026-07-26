<template>
  <view class="assistant-page">
    <!-- 顶部说明 -->
    <view class="header">
      <view class="header-title">充电帮助</view>
      <view class="header-desc">有问题可以问我，例如预约、充电、报修、订单、安全注意事项</view>
    </view>

    <!-- 快捷问题 -->
    <view class="quick-questions">
      <view class="section-title">常见问题</view>
      <view class="question-tags">
        <view
          v-for="(q, index) in quickQuestions"
          :key="index"
          class="question-tag"
          @click="askQuickQuestion(q)"
        >
          {{ q }}
        </view>
      </view>
    </view>

    <!-- 聊天消息列表 -->
    <scroll-view
      class="chat-list"
      scroll-y
      :scroll-into-view="scrollToView"
      scroll-with-animation
    >
      <view
        v-for="(msg, index) in messages"
        :key="index"
        :id="'msg-' + index"
        :class="['message-item', msg.role === 'user' ? 'user-message' : 'assistant-message']"
      >
        <view class="message-avatar">
          <text v-if="msg.role === 'user'">我</text>
          <text v-else>AI</text>
        </view>
        <view class="message-content">
          <view class="message-text">{{ msg.content }}</view>
          <view v-if="msg.role === 'assistant' && msg.sourceType" class="source-label">
            {{ sourceLabel(msg.sourceType) }}
          </view>
          <view v-if="msg.role === 'assistant' && msg.loading" class="loading-indicator">
            <text class="loading-dot">.</text>
            <text class="loading-dot">.</text>
            <text class="loading-dot">.</text>
          </view>
        </view>
      </view>

      <!-- 推荐操作 -->
      <view v-if="suggestions.length > 0" class="suggestions-section">
        <view class="section-title">你可以</view>
        <view class="suggestion-tags">
          <view
            v-for="(s, index) in suggestions"
            :key="index"
            class="suggestion-tag"
            @click="handleSuggestion(s)"
          >
            {{ s }}
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入框 -->
    <view class="input-area">
      <input
        class="input-box"
        v-model="inputText"
        placeholder="输入你的问题..."
        confirm-type="send"
        @confirm="sendMessage"
        :disabled="isLoading"
      />
      <view
        class="send-btn"
        :class="{ 'btn-disabled': !inputText.trim() || isLoading }"
        @click="sendMessage"
      >
        发送
      </view>
    </view>
  </view>
</template>

<script>
import { chatWithAssistant } from '@/api/assistant'

export default {
  data() {
    return {
      inputText: '',
      messages: [],
      suggestions: [],
      isLoading: false,
      scrollToView: '',
      quickQuestions: [
        '怎么预约充电？',
        '预约后多久失效？',
        '端口故障怎么办？',
        '怎么查看我的订单？',
        '怎么提交报修？',
        '充电时要注意什么？'
      ]
    }
  },

  onLoad() {
    // 欢迎消息
    this.messages.push({
      role: 'assistant',
      content: '你好！我是 ChargeSafe 智能充电助手。\n\n我可以帮你解答关于预约、充电、订单、报修等问题。有什么需要帮助的吗？'
    })
  },

  methods: {
    // 发送消息
    async sendMessage() {
      const question = this.inputText.trim()
      if (!question || this.isLoading) return

      // 添加用户消息
      this.messages.push({ role: 'user', content: question })
      this.inputText = ''
      this.scrollToBottom()

      // 显示加载状态
      this.isLoading = true
      this.messages.push({ role: 'assistant', content: '', loading: true })
      this.scrollToBottom()

      try {
        const res = await chatWithAssistant(question, this.getPageContext())
        const data = res.data

        // 移除加载状态的消息
        this.messages.pop()

        // 添加 AI 回复
        this.messages.push({
          role: 'assistant',
          content: data.answer,
          sourceType: data.sourceType
        })

        // 更新推荐操作
        if (data.suggestions && data.suggestions.length > 0) {
          this.suggestions = data.suggestions
        }
      } catch (error) {
        // 移除加载状态的消息
        this.messages.pop()

        // 添加错误提示
        this.messages.push({
          role: 'assistant',
          content: '抱歉，我暂时无法回答这个问题。请稍后再试或联系物业寻求帮助。'
        })
      } finally {
        this.isLoading = false
        this.scrollToBottom()
      }
    },

    // 快捷提问
    askQuickQuestion(question) {
      this.inputText = question
      this.sendMessage()
    },

    // 处理推荐操作
    handleSuggestion(suggestion) {
      // 根据推荐内容跳转到对应页面
      const pageMap = {
        '查看车棚详情': '/pages/parking/index',
        '查看我的订单': '/pages/order/list',
        '提交设备报修': '/pages/repair/submit',
        '查看车棚': '/pages/parking/index',
        '查看预约状态': '/pages/reservation/index',
        '取消预约': '/pages/reservation/index',
        '结束充电': '/pages/charging/charging',
        '查看订单': '/pages/order/list',
        '查看订单详情': '/pages/order/list',
        '提交报修': '/pages/repair/submit',
        '查看报修进度': '/pages/repair/list',
        '联系物业': '',
        '预约充电': '/pages/parking/index',
        '查看端口状态': '/pages/parking/index'
      }

      const url = pageMap[suggestion]
      if (url) {
        uni.navigateTo({ url })
      }
    },

    // 获取当前页面上下文
    getPageContext() {
      const pages = getCurrentPages()
      if (pages.length > 0) {
        const currentPage = pages[pages.length - 1]
        return currentPage.route || ''
      }
      return ''
    },

    sourceLabel(sourceType) {
      return {
        LLM: '大模型回答',
        FAQ: '常见问题回答',
        FALLBACK: '常见问题降级回答'
      }[sourceType] || ''
    },

    // 滚动到底部
    scrollToBottom() {
      this.$nextTick(() => {
        this.scrollToView = 'msg-' + (this.messages.length - 1)
      })
    }
  }
}
</script>

<style scoped>
.assistant-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 30rpx;
  color: #fff;
}

.header-title {
  font-size: 40rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.header-desc {
  font-size: 26rpx;
  opacity: 0.9;
}

.quick-questions {
  background: #fff;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #eee;
}

.section-title {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 15rpx;
}

.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.question-tag {
  background: #f0f0f0;
  color: #333;
  padding: 12rpx 24rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
}

.question-tag:active {
  background: #e0e0e0;
}

.chat-list {
  flex: 1;
  padding: 20rpx 30rpx;
  overflow-y: auto;
}

.message-item {
  display: flex;
  margin-bottom: 30rpx;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: bold;
  flex-shrink: 0;
}

.user-message .message-avatar {
  background: #667eea;
  color: #fff;
}

.assistant-message .message-avatar {
  background: #764ba2;
  color: #fff;
}

.message-content {
  max-width: 70%;
  margin: 0 15rpx;
}

.message-text {
  background: #fff;
  padding: 20rpx 25rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  white-space: pre-wrap;
}

.source-label {
  margin: 8rpx 25rpx 0;
  font-size: 20rpx;
  color: #94a3b8;
}

.user-message .message-text {
  background: #667eea;
  color: #fff;
}

.loading-indicator {
  display: flex;
  padding: 20rpx 25rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.loading-dot {
  animation: blink 1.4s infinite both;
  font-size: 32rpx;
  color: #999;
}

.loading-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {
  0%, 80%, 100% {
    opacity: 0;
  }
  40% {
    opacity: 1;
  }
}

.suggestions-section {
  margin-top: 20rpx;
  padding: 20rpx;
  background: #fff;
  border-radius: 20rpx;
}

.suggestion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.suggestion-tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 12rpx 24rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
}

.suggestion-tag:active {
  opacity: 0.8;
}

.input-area {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
}

.input-box {
  flex: 1;
  height: 80rpx;
  background: #f5f5f5;
  border-radius: 40rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
}

.send-btn {
  margin-left: 20rpx;
  padding: 0 40rpx;
  height: 80rpx;
  line-height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
}

.send-btn:active {
  opacity: 0.8;
}

.btn-disabled {
  opacity: 0.5;
}
</style>
