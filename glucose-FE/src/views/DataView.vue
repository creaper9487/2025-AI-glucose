<script setup>
import DataForm from '@/components/Forms/dataForm.vue';
import NavBar from '@/components/NavBar.vue';
import ModelTrainingProgress from '../components/ModelTrainingProgress.vue';
</script>
<!-- Warning: Remember to display the Welcome message in and only in the first entry. Achieve by asking the server if the data is empty or not. -->
<template>
    <NavBar />
    <div class="flex justify-center items-center mt-10">
        <div class="w-96">
            <ModelTrainingProgress />
            <DataForm />
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