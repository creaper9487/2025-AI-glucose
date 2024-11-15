<script setup>

import InputForm from '@/components/Forms/inputForm.vue';
import NavBar from '@/components/NavBar.vue';
</script>
<template>
  <NavBar />
  <div class="h-screen flex items-center justify-center flex-row bg-gray-800">
  <InputForm class="mx-10" />

  </div>
</template>


<style scoped>
input:focus ~ label,
input:valid ~ label {
  top: -20px;
  left: 0;
  color: #03e9f4;
  font-size: 12px;
}
</style>
