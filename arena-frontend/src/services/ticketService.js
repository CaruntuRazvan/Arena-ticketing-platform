import api from '../api/axios';

const ticketService = {
    /**
     * PASUL 1: Rezervarea locurilor (buyTickets în backend)
     * Status: PENDING (valabil 15 minute)
     */

    bookTickets: async (bookingData) => {
        try {
            // Trimite matchId, userId, seatIds și useLoyaltyPoints
            const response = await api.post('/ticketing/buy', bookingData);
            return response.data; // Returnează List<TicketResponseDTO>
        } catch (error) {
            console.error("Eroare la rezervarea biletelor:", error);
            throw error;
        }
    },

    /**
     * PASUL 2: Confirmarea plății (confirmPayment în backend)
     * Status: CONFIRMED + Trimitere Mail + Alocare Puncte
     */
    confirmPayment: async (ticketIds) => {
        try {
            // Trimite lista de ID-uri de bilet (Long) primite la pasul 1
            const response = await api.post('/ticketing/confirm', ticketIds);
            return response.data;
        } catch (error) {
            console.error("Eroare la confirmarea plății:", error);
            throw error;
        }
    },

    getMyTickets: async (userId, page = 0, size = 5) => {
        try {
            const response = await api.get(`/ticketing/user/${userId}`, {
                params: { page, size, sort: 'purchaseDate,desc' }
            });
            return response.data;
        } catch (error) {
            console.error("Eroare la preluarea biletelor utilizatorului:", error);
            throw error;
        }
    }
};

export default ticketService;