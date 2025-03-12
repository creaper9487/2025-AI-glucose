<script setup>
import RegisterForm from '@/components/Forms/registerForm.vue';
import NavBar from '@/components/NavBar.vue';
import aboutUsBtn from '@/components/aboutUsBtn.vue';
</script>

<template>
  <NavBar></NavBar>
  <div class="h-screen flex items-center justify-center bg-gray-800">
    <div class="flex flex-col items-center">
      <RegisterForm />
    </div>
  </div>
  <div class="fixed bottom-0 right-0 p-4 mr-8 mb-4"><aboutUsBtn /></div>
</template>