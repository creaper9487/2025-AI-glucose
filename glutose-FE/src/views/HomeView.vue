<template>
  <div class="h-screen flex items-center justify-center bg-gray-800">
    <div class="w-96 p-10 bg-slate-700 shadow-lg rounded-lg">
      <h2 class="text-2xl text-white text-center mb-8">Login</h2>
      <form>
        <div class="relative mb-8">
          <input type="text" name="" required class="w-full py-2 text-lg text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
          <label class="absolute top-0 left-0 py-2 text-lg text-white transition-all duration-500 pointer-events-none">Username</label>
        </div>
        <div class="relative mb-8">
          <input type="password" name="" required class="w-full py-2 text-lg text-white bg-transparent border-b border-white outline-none focus:border-cyan-400">
          <label class="absolute top-0 left-0 py-2 text-lg text-white transition-all duration-500 pointer-events-none">Password</label>
        </div>
        <a href="#" class="relative inline-block px-5 py-2 text-lg text-cyan-400 uppercase transition-all duration-500 mt-10 tracking-wider">Submit</a>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeView',
};
</script>

<style scoped>
/* Additional styles if needed */
</style>
