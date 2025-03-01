<script setup>
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';
import { onMounted } from 'vue';
import { useRoute } from 'vue-router';

const authStore = useAuthStore();
const route = useRoute();

onMounted(async () => {
    const code = route.query.code;
    if (code) {
        const decodedCode = decodeURIComponent(code);
        try {
            const response = await axios.post('/api/auth/google/', { code: decodedCode });
            console.log(response.data);
        } catch (error) {
            console.error('Error during Google OAuth:', error);
        }
    }
    authStore.fetchUser();
});

</script>

<template>
    <div>
        <!-- Your template content -->
    </div>
</template>

<style scoped>
/* Your styles here */
</style>
