// src/services/matchService.js
import api from '../api/axios';

const matchService = {
    // Apelăm endpoint-ul /upcoming care returnează Page<MatchDTO>
    getUpcomingMatches: async (page = 0, size = 6) => {
        try {
            const response = await api.get('/catalog/matches/upcoming', {
                params: { page, size , sort: 'matchDate,asc'}
            });
            // response.data va conține: { content: [...], totalPages: X, totalElements: Y }
            return response.data;
        } catch (error) {
            console.error("Eroare matchService:", error);
            throw error;
        }
    },
    getStadiums: async () => {
        try {
            const response = await api.get('/catalog/stadiums');
            return response.data; // Returnează List<StadiumDTO>
        } catch (error) {
            console.error("Eroare la încărcarea stadionului:", error);
            throw error;
        }
    },
    getMatchTrivia: async (matchId) => {
        try {
            const response = await api.get(`/catalog/matches/${matchId}/ai-trivia`);
            return response.data; // Returnează direct string-ul cu trivia
        } catch (error) {
            console.error(`Eroare matchService (trivia) pentru ID ${matchId}:`, error);
            throw error;
        }
    },

    getSectorDetails: async (matchId, sectorName) => {
        const response = await api.get(`/catalog/matches/${matchId}/sectors/${sectorName}`);
        return response.data;
    },

    getSeatsBySector: async (matchId, sectorId) => {
        const response = await api.get(`/catalog/matches/${matchId}/sectors/${sectorId}/seats`);
        return response.data;
    }
};

export default matchService;