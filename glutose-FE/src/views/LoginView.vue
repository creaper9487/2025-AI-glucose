<script setup>
import LoginForm from '@/components/Forms/LoginForm.vue';
import registerForm from '@/components/Forms/registerForm.vue';
import NavBar from '@/components/NavBar.vue';
</script>
<template>
  <NavBar></NavBar>
  <div class="h-screen flex  justify-center flex-row">
  <div class="h-screen flex w-1/2 items-center justify-center flex-col bg-gray-800">
  <registerForm/>
  </div>
  <div class="h-screen flex w-1/2 items-center justify-center flex-col bg-gray-500">
    <LoginForm/>
  </div>
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
