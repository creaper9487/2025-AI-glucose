<script setup>
import InputForm from '@/components/inputForm.vue';

</script>
<template>
  <div class="h-screen flex items-center justify-center bg-gray-800">
  <InputForm />
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
