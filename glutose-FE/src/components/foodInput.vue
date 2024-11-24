<script setup>
import { ref } from 'vue';
import axios from 'axios';

const imageFile = ref(null);

const handleFileChange = (event) => {
    imageFile.value = event.target.files[0];
};

const sendImage = async () => {
    if (!imageFile.value) return;

    const formData = new FormData();
    formData.append('image', imageFile.value);

    try {
        const response = await axios.post('/api/sense', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        console.log(response.data);
    } catch (error) {
        console.error('Error uploading image:', error);
    }
};
</script>
<template>
    <div>
        <input type="file" @change="handleFileChange" accept="image/*" />
        <button @click="sendImage">Send Image</button>
    </div>
</template>

