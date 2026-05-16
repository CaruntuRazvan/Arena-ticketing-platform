import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api', // portul API GATEWAY
    headers: {
        'Content-Type': 'application/json'
    }
});

// Adăugăm un interceptor pentru a pune automat Token-ul în header dacă există
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;