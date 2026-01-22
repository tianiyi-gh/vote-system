<template>
  <div class="empty-state">
    <div class="empty-icon">
      <slot name="icon">
        <!-- 默认空盒子图标 -->
        <van-icon name="bag-o" size="60" color="#dcdee0" />
      </slot>
    </div>
    <div class="empty-text">
      <slot name="text">
        {{ text }}
      </slot>
    </div>
    <div class="empty-description" v-if="description">
      <slot name="description">
        {{ description }}
      </slot>
    </div>
    <div class="empty-actions" v-if="$slots.action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  text?: string
  description?: string
}

withDefaults(defineProps<Props>(), {
  text: '暂无数据',
  description: ''
})
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background-color: #fff;
  margin: 12px;
  border-radius: 8px;

  .empty-icon {
    margin-bottom: 16px;

    :deep(.van-icon) {
      font-size: 60px;
      color: #dcdee0;
    }
  }

  .empty-text {
    font-size: 14px;
    color: #969799;
    margin-bottom: 8px;
    text-align: center;
  }

  .empty-description {
    font-size: 12px;
    color: #c8c9cc;
    margin-bottom: 20px;
    text-align: center;
    max-width: 300px;
  }

  .empty-actions {
    margin-top: 8px;
  }
}
</style>
