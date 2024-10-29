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
.welcome-view {
    padding: 20px;
}

.form-group {
    margin-bottom: 15px;
}

label {
    display: block;
    margin-bottom: 5px;
}

input {
    width: 100%;
    padding: 8px;
    box-sizing: border-box;
}

button {
    padding: 10px 15px;
    background-color: #007bff;
    color: white;
    border: none;
    cursor: pointer;
}

button:hover {
    background-color: #0056b3;
}
</style>