<script setup>
import { RouterLink } from 'vue-router'
</script>
<template>
    <nav class="navbar">
        <ul>
            <li><RouterLink to="/">紀錄</RouterLink></li>
            <li><RouterLink to="/figure">資料圖表</RouterLink></li>
            <li><RouterLink to="/login">登入</RouterLink></li>
        </ul>
    </nav>
</template>


<style scoped>
.navbar {
    background-color: #333;
    overflow: hidden;
}

.navbar ul {
    list-style-type: none;
    margin: 0;
    padding: 0;
    display: flex;
}

.navbar li {
    flex: 1;
}

.navbar a {
    display: block;
    color: white;
    text-align: center;
    padding: 14px 16px;
    text-decoration: none;
    transition: background-color 0.3s, color 0.3s; /* Add transition for smooth animation */
}

.navbar a:hover {
    background-color: #ddd;
    color: black;
}
</style>