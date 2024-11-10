<script setup>
import DataForm from '@/components/Forms/dataForm.vue';
import NavBar from '@/components/NavBar.vue';


</script>
//Waring: Remember to display the Welcome message in and only in the first entry. Achive by asking the server if the data is empty or not.
<template>
<NavBar />
<DataForm />

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