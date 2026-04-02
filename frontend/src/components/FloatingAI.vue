<template>
  <div class="floating-ai" @click="$emit('open')">
    <el-button type="primary" circle>智</el-button>
  </div>
</template>

<script setup>
defineEmits(['open'])
</script>

<style scoped>
.floating-ai { position:fixed; right:20px; bottom:20px; z-index:9999 }
</style>
