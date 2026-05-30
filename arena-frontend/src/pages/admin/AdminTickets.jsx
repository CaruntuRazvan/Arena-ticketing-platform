import React, { useState, useEffect } from 'react';
import adminTicketsService from '../../services/adminTicketsService.js';
import adminMatchesService from '../../services/adminMatchesService.js';
import TicketRow from '../../components/admin/TicketRow.jsx';
import Pagination from '../../components/admin/Pagination.jsx'; // Păstrăm componenta refolosibilă de paginare
import { Ticket, Search, Filter, TicketCheck, X } from 'lucide-react';

const AdminTickets = () => {
    const [ticketsPage, setTicketsPage] = useState(null);
    const [matches, setMatches] = useState([]); // Pentru filtrul dropdown
    const [selectedMatchId, setSelectedMatchId] = useState(""); // Gol înseamnă "Toate Meciurile"
    const [currentPage, setCurrentPage] = useState(0);
    const [loading, setLoading] = useState(true);

    // 1. Încărcăm catalogul de meciuri pentru dropdown-ul de filtrare
    useEffect(() => {
        const fetchFilterData = async () => {
            try {
                const data = await adminMatchesService.getAllMatches(0, 100); // aducem meciurile disponibile
                setMatches(data?.content || []);
            } catch (err) {
                console.error("Eroare la încărcarea listei de meciuri pentru filtru:", err);
            }
        };
        fetchFilterData();
    }, []);

    // 2. Încărcăm biletele bazat pe pagină și pe meciul selectat
    useEffect(() => {
        fetchTickets();
    }, [currentPage, selectedMatchId]);

    const fetchTickets = async () => {
        setLoading(true);
        try {
            let data;
            if (selectedMatchId === "") {
                // Nu e selectat niciun meci specific -> cerem tot catalogul de bilete
                data = await adminTicketsService.getAllTickets(currentPage, 15);
            } else {
                // S-a selectat un meci -> cerem biletele dedicate
                data = await adminTicketsService.getTicketsByMatch(selectedMatchId, currentPage, 15);
            }
            setTicketsPage(data);
        } catch (err) {
            console.error("Eroare la încărcarea biletelor:", err);
        } finally {
            setLoading(false);
        }
    };

    const handleMatchFilterChange = (e) => {
        setSelectedMatchId(e.target.value);
        setCurrentPage(0); // Resetăm întotdeauna la prima pagină când se schimbă filtrul
    };

    if (loading && currentPage === 0) return (
        <div className="min-h-screen flex items-center justify-center bg-slate-50 font-sans">
            <div className="flex flex-col items-center gap-4">
                <div className="w-12 h-12 border-4 border-slate-200 border-t-blue-600 rounded-full animate-spin"></div>
                <p className="text-[10px] font-black uppercase text-slate-400 tracking-[0.3em]">Scanare Vânzări Bilete...</p>
            </div>
        </div>
    );

    return (
        <div className="min-h-screen bg-slate-50 p-8 pt-24 font-sans text-slate-900">
            <div className="max-w-7xl mx-auto">

                {/* Header & Filter Controls */}
                <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-6 mb-10">
                    <div>
                        <div className="flex items-center gap-3 mb-2">
                            <div className="bg-slate-900 p-2 rounded-xl">
                                <Ticket className="w-6 h-6 text-white" />
                            </div>
                            <h1 className="text-3xl font-black text-slate-900 uppercase italic tracking-tighter leading-none">
                                Monitorizare <span className="text-blue-600">Bilete</span>
                            </h1>
                        </div>
                        <p className="text-slate-400 font-bold text-[10px] uppercase tracking-widest ml-1">
                            Registru vânzări, locuri ocupate și decontări
                        </p>
                    </div>

                    {/* Filter Dropdown */}
                    <div className="relative w-full md:w-80 group">
                        <div className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none">
                            <Filter className="w-4 h-4" />
                        </div>
                        <select
                            value={selectedMatchId}
                            onChange={handleMatchFilterChange}
                            className="w-full bg-white border-2 border-slate-100 rounded-2xl py-4 pl-11 pr-10 outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-600/5 shadow-sm text-xs font-black uppercase tracking-wider cursor-pointer appearance-none text-slate-700"
                        >
                            <option value="">Filtrează după: Toate Meciurile</option>
                            {matches.map(match => (
                                <option key={match.id} value={match.id}>
                                    România vs {match.opponentName}
                                </option>
                            ))}
                        </select>
                        <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400 font-bold text-[8px]">
                            ▼
                        </div>
                    </div>
                </div>

                {/* Main Table Layout */}
                <div className="bg-white rounded-[2.5rem] border border-slate-100 shadow-2xl overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="bg-slate-950 text-white">
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest text-center border-r border-white/5">ID Bilet</th>
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest border-r border-white/5">Suporter</th>
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest border-r border-white/5">Eveniment</th>
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest text-center border-r border-white/5">Poziționare Stadion</th>
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest text-right">Preț</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-50">
                            {ticketsPage?.content && ticketsPage.content.length > 0 ? (
                                ticketsPage.content.map((ticket) => (
                                    <TicketRow key={ticket.id} ticket={ticket} />
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="5" className="p-20 text-center">
                                        <div className="flex flex-col items-center opacity-20">
                                            <TicketCheck size={48} className="mb-4" />
                                            <p className="font-black uppercase italic text-xl">Niciun bilet vândut</p>
                                            <p className="text-xs font-bold uppercase tracking-widest mt-2">
                                                {selectedMatchId ? "Nu s-au cumpărat bilete la acest meci" : "Registrul de comenzi este gol"}
                                            </p>
                                        </div>
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>

                    {/* Pagination Interface */}
                    {ticketsPage?.totalPages > 1 && (
                        <Pagination
                            currentPage={currentPage}
                            totalPages={ticketsPage?.totalPages}
                            totalElements={ticketsPage?.totalElements}
                            onPageChange={setCurrentPage}
                        />
                    )}
                </div>
            </div>
        </div>
    );
};

export default AdminTickets;