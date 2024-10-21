<script setup>
import { RouterLink } from 'vue-router';
</script>
<template>
    <VMenu>
  <button>Documentation</button>

  <template #popper>
    <button>Guide</button>
    <button>API Reference</button>
  </template>
</VMenu>
</template>