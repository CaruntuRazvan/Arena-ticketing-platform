import api from '../api/axios';

const AdminMatchService = {
    getStadiumSectors: async (stadiumId) => {
        const response = await api.get(`/catalog/stadiums/${stadiumId}/sectors`);
        return response.data;
    },
    getAllMatches: async (page = 0, size = 10) => {
        try {
            // Am adăugat /catalog conform exemplului tău care funcționează
            const response = await api.get('/catalog/matches', {
                params: { page, size, sort: 'matchDate,desc' }
            });
            return response.data;
        } catch (error) {
            console.error("Eroare AdminMatchService (getAll):", error);
            throw error;
        }
    },
    getSectorPrice: async (matchId, sectorId) => {
        try {
            const response = await api.get(`/catalog/matches/${matchId}/prices/${sectorId}`);
            return response.data; // Întoarce valoarea numerică (Double)
        } catch (error) {
            // Logăm discret, dar nu blocăm aplicația (în caz că sectorul nu are încă preț configurat)
            console.warn(`Prețul pentru sectorul ${sectorId} la meciul ${matchId} nu este încă setat.`);
            return '';
        }
    },

    createMatch: async (matchData) => {
        const response = await api.post('/catalog/matches', matchData);
        return response.data;
    },

    setMatchPrices: async (prices) => {
        // prices este o listă de PriceRequestDTO
        const response = await api.post('/catalog/matches/prices', prices);
        return response.data;
    },

    publishMatch: async (matchId) => {
        try {
            const response = await api.post(`/catalog/matches/${matchId}/publish`);
            return response.data;
        } catch (error) {
            console.error(`Eroare la publicarea meciului ${matchId}:`, error);
            throw error;
        }
    },

    updateMatchStatus: async (matchId, status) => {
        try {
            const response = await api.patch(`/catalog/matches/${matchId}/status`, null, {
                params: { status }
            });
            return response.data;
        } catch (error) {
            console.error(`Eroare la schimbarea statusului pentru meciul ${matchId}:`, error);
            throw error;
        }
    },

    updateMatch: async (matchId, matchData) => {
        try {
            const response = await api.put(`/catalog/matches/${matchId}`, matchData);
            return response.data;
        } catch (error) {
            console.error(`Eroare la editarea meciului ${matchId}:`, error);
            throw error;
        }
    },

    deleteMatch: async (matchId) => {
        try {
            const response = await api.delete(`/catalog/matches/${matchId}`);
            return response.data;
        } catch (error) {
            console.error(`Eroare la ștergerea meciului cu ID ${matchId}:`, error);
            throw error;
        }
    }
};

export default AdminMatchService;