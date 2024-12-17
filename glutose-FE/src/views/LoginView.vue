<script setup>
import LoginForm from '@/components/Forms/loginForm.vue';
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
