import api from '../api/axios';

const authService = {
    login: async (username, password, rememberMe = false) => { // Adăugăm parametrul
        const response = await api.post('/users/login', {
            username,
            password,
            rememberMe: rememberMe // Trimitem valoarea reală (true/false)
        });

        if (response.data.accessToken) {
            localStorage.setItem('token', response.data.accessToken);
            localStorage.setItem('username', response.data.username);

            // Dacă backend-ul trimite și un refreshToken, îl putem salva
            if (response.data.refreshToken) {
                localStorage.setItem('refreshToken', response.data.refreshToken);
            }
        }
        return response.data;
    },

    // În src/services/authService.js
    register: async (userData) => {
        const response = await api.post('/users/register', userData);
        return response.data;
    },

    verifyAccount: async (email, code) => {
        // Folosim params pentru că în Java ai @RequestParam
        const response = await api.post('/users/verify', null, {
            params: { email, code }
        });
        return response.data;
    },

    resendCode: async (email) => {
        const response = await api.post('/users/resend-code', null, {
            params: { email }
        });
        return response.data;
    },

    getProfile: async () => {
        try {
            const response = await api.get('/users/profile');
            return response.data; // Returnează UserResponseDTO (id, username, email, firstName, lastName, loyaltyPoints)
        } catch (error) {
            console.error("Eroare la recuperarea profilului:", error);
            throw error;
        }
    },

    logout: async () => {
        try {
            // Token-ul e încă în localStorage → interceptorul îl pune în header → Gateway îl acceptă
            await api.post('/users/logout');
            //console.log("✅ Logout backend reușit");
        } catch (error) {
            console.warn("Logout backend eșuat:", error.response?.status);
        } finally {
            localStorage.removeItem('token');
            localStorage.removeItem('username');
        }
    },

    getCurrentUser: () => localStorage.getItem('username')
};

export default authService;