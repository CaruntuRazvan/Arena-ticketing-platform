import api from '../api/axios'; // Importăm instanța ta cu interceptor

const adminUsersService = {

    getAllUsers: async (page = 0, size = 20) => {
        try {
            const response = await api.get('/users', {
                params: {
                    page,
                    size,
                    sort: 'username,asc' // Sortarea default din backend-ul tău
                }
            });
            return response.data; // Returnează Page<UserResponseDTO>
        } catch (error) {
            console.error("Eroare la preluarea listei de utilizatori:", error);
            throw error;
        }
    },

    deleteUser: async (userId) => {
        try {
            const response = await api.delete(`/users/${userId}`);
            return response.data; // Îți va întoarce string-ul de succes de pe backend
        } catch (error) {
            console.error(`Eroare la ștergerea utilizatorului cu ID ${userId}:`, error);
            throw error;
        }
    }


};

export default adminUsersService;