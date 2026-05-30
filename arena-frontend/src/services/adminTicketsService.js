import api from '../api/axios';

const adminTicketsService = {
    // toate biletele omise
    getAllTickets: async (page = 0, size = 20) => {
        try {
            const response = await api.get('/ticketing', {
                params: { page, size }
            });
            return response.data;
        } catch (error) {
            console.error("Eroare la preluarea tuturor biletelor:", error);
            throw error;
        }
    },

    // biletele pentru un meci specific, cu paginare
    getTicketsByMatch: async (matchId, page = 0, size = 20) => {
        try {
            const response = await api.get(`/ticketing/match/${matchId}`, {
                params: { page, size }
            });
            return response.data;
        } catch (error) {
            console.error(`Eroare la preluarea biletelor pentru meciul ${matchId}:`, error);
            throw error;
        }
    },

    getMatchAnalytics: async (matchId) => {
        try {
            const response = await api.get(`/ticketing/analytics/match/${matchId}`);
            return response.data;
        } catch (error) {
            console.error(`Eroare la preluarea statisticilor pentru meciul ${matchId}:`, error);
            throw error;
        }
    }
};

export default adminTicketsService;