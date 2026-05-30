import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ticketService from '../services/ticketService';
import { QRCodeSVG } from 'qrcode.react'; // Librăria pentru QR curat
import {
    Ticket, MapPin, ChevronRight, ChevronLeft,
    Info, AlertCircle, X, Maximize2
} from 'lucide-react';

const MyTicketsPage = () => {
    const { profile, loading: authLoading } = useAuth();
    const { pageNumber } = useParams();
    const navigate = useNavigate();

    const [ticketsPage, setTicketsPage] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedTicket, setSelectedTicket] = useState(null);

    const currentPage = pageNumber ? parseInt(pageNumber) - 1 : 0;

    const handlePageChange = (newPage) => {
        navigate(`/my-tickets/${newPage + 1}`);
    };

    useEffect(() => {
        const fetchTickets = async () => {
            if (authLoading) return;
            if (!profile?.id) {
                setLoading(false);
                return;
            }
            try {
                setLoading(true);
                const data = await ticketService.getMyTickets(profile.id, currentPage);
                setTicketsPage(data);
                window.scrollTo({ top: 0, behavior: 'smooth' });
            } catch (err) {
                setError("Momentan nu putem încărca biletele tale.");
            } finally {
                setLoading(false);
            }
        };
        fetchTickets();
    }, [profile?.id, authLoading, currentPage]);

    if (authLoading || loading) return (
        <div className="min-h-[60vh] flex flex-col items-center justify-center">
            <div className="w-10 h-10 border-4 border-slate-200 border-t-blue-900 rounded-full animate-spin mb-4"></div>
            <p className="text-[10px] font-black text-blue-900 uppercase tracking-[0.3em]">Se încarcă...</p>
        </div>
    );

    const tickets = ticketsPage?.content || [];
    const totalPages = ticketsPage?.totalPages || 0;

    return (
        <main className="max-w-4xl mx-auto mt-10 p-6 relative">

            {/* --- MODAL PENTRU QR CODE (STILIZAT CA ÎN POZĂ) --- */}
            {selectedTicket && (
                <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 animate-in fade-in duration-300">
                    <div className="absolute inset-0 bg-blue-950/80 backdrop-blur-md" onClick={() => setSelectedTicket(null)}></div>

                    <div className="relative bg-white w-full max-w-sm rounded-[3.5rem] overflow-hidden shadow-2xl animate-in zoom-in-95 duration-300">
                        <button onClick={() => setSelectedTicket(null)} className="absolute top-8 right-8 p-2 hover:bg-slate-100 rounded-full transition-colors">
                            <X className="w-6 h-6 text-slate-400" />
                        </button>

                        <div className="p-12 flex flex-col items-center">
                            <h3 className="text-xl font-black text-blue-900 uppercase italic mb-1 tracking-tight">
                                România vs {selectedTicket.opponentName}
                            </h3>
                            <p className="text-slate-400 font-bold text-[10px] uppercase mb-10 tracking-widest">
                                {selectedTicket.sectorName} • R {selectedTicket.row} • L {selectedTicket.seat}
                            </p>

                            {/* GENERATOR QR - STRICT CA ÎN POZĂ */}
                            <div className="bg-white p-4 rounded-xl">
                                <QRCodeSVG
                                    value={selectedTicket.ticketCode || "ARENA-GENERIC"}
                                    size={220} // Dimensiune mare
                                    level="H"   // High error correction (pentru aspect dens)
                                    includeMargin={false}
                                    fgColor="#000000" // Negru pur ca în poză
                                />
                            </div>

                            {/* Fără codul text dedesubt, conform cerinței */}
                        </div>

                        <div className="bg-slate-50 p-8 text-center">
                            <p className="text-[10px] font-bold text-slate-400 uppercase leading-relaxed tracking-widest">
                                SCANĂRILE SUNT UNICE PER BILET
                            </p>
                        </div>
                    </div>
                </div>
            )}

            <header className="mb-12 flex flex-col md:flex-row justify-between items-start md:items-end gap-4">
                <div>
                    <div className="flex items-center gap-3 mb-2">
                        <div className="bg-red-600 p-2 rounded-xl">
                            <Ticket className="w-6 h-6 text-white" />
                        </div>
                        <h1 className="text-3xl font-black text-blue-900 uppercase italic tracking-tighter">BILETELE MELE</h1>
                    </div>
                </div>
            </header>

            <div className="space-y-8">
                {tickets.map((ticket) => (
                    <div key={ticket.id} className="relative flex flex-col md:flex-row bg-white rounded-[2.5rem] border border-slate-100 shadow-sm hover:shadow-xl transition-all duration-500 overflow-hidden">
                        <div className={`w-full md:w-3 ${ticket.used ? 'bg-slate-200' : 'bg-blue-900'}`}></div>

                        <div className="flex-grow p-10">
                            <div className="flex items-center justify-between mb-8">
                                <div className={`flex items-center gap-2 px-3 py-1 rounded-full ${ticket.used ? 'bg-slate-100 text-slate-400' : 'bg-blue-50 text-blue-700'}`}>
                                    <span className="text-[9px] font-black uppercase tracking-widest">{ticket.used ? 'Utilizat' : 'Activ'}</span>
                                </div>
                            </div>

                            <h2 className="text-3xl font-black text-blue-900 mb-10 italic uppercase tracking-tighter">
                                ROMÂNIA <span className="text-red-600 text-sm italic">VS</span> {ticket.opponentName}
                            </h2>

                            <div className="grid grid-cols-2 gap-8">
                                <div className="space-y-1 text-sm">
                                    <p className="text-[9px] font-black text-slate-300 uppercase tracking-widest">Locație</p>
                                    <div className="flex items-center gap-2 font-bold text-slate-800 uppercase italic">
                                        <MapPin className="w-4 h-4 text-blue-600 shrink-0" />
                                        {ticket.stadiumName || 'Arena Națională'}
                                    </div>
                                </div>
                                <div className="space-y-1 text-sm">
                                    <p className="text-[9px] font-black text-slate-300 uppercase tracking-widest">Locul Tău</p>
                                    <div className="flex items-center gap-2 font-bold text-slate-800 uppercase italic">
                                        <Info className="w-4 h-4 text-slate-400 shrink-0" />
                                        {ticket.sectorName}, R {ticket.row}, L {ticket.seat}
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="bg-slate-50 md:w-72 p-10 flex flex-col items-center justify-center border-t md:border-t-0 md:border-l-2 border-dashed border-slate-200 relative">
                            {/* Semincercurile de bilet lateral */}
                            <div className="hidden md:block absolute -left-5 top-0 w-10 h-10 bg-slate-50 border-b-2 border-slate-100 rounded-full -translate-y-1/2"></div>
                            <div className="hidden md:block absolute -left-5 bottom-0 w-10 h-10 bg-slate-50 border-t-2 border-slate-100 rounded-full translate-y-1/2"></div>

                            <div className="mb-8 text-center">
                                <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1 font-sans">Preț Bilet</p>
                                <p className="text-3xl font-black text-blue-950 italic">{(ticket.price || ticket.finalPrice)?.toFixed(2)} RON</p>
                            </div>

                            <button
                                disabled={ticket.used}
                                onClick={() => setSelectedTicket(ticket)}
                                className={`w-full py-5 rounded-[2rem] font-black text-[11px] uppercase tracking-widest flex items-center justify-center gap-3 transition-all shadow-xl active:scale-95 ${
                                    ticket.used ? 'bg-slate-200 text-slate-400 shadow-none' : 'bg-blue-900 text-white hover:bg-red-600'
                                }`}
                            >
                                {ticket.used ? 'INACTIV / EXPIRAT' : <><Maximize2 size={16}/> VEZI QR CODE</>}
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            {/* Paginare... */}
            {totalPages > 1 && (
                <div className="mt-20 flex items-center justify-center gap-4">
                    <button onClick={() => handlePageChange(Math.max(0, currentPage - 1))} disabled={currentPage === 0} className="p-3 rounded-2xl border border-slate-200 bg-white hover:bg-blue-50 hover:border-blue-900 hover:scale-105 disabled:opacity-20 transition-all shadow-sm group"><ChevronLeft/></button>
                    <div className="flex gap-2">
                        {[...Array(totalPages)].map((_, idx) => (
                            <button key={idx} onClick={() => handlePageChange(idx)} className={`w-12 h-12 rounded-2xl font-black text-xs transition-all ${
                                currentPage === idx
                                    ? 'bg-blue-900 text-white shadow-xl scale-110'
                                    : 'bg-white border border-slate-200 text-slate-400 hover:bg-blue-50 hover:border-blue-900 hover:text-blue-900 hover:scale-105'
                            }`}>{idx + 1}</button>
                        ))}
                    </div>
                    <button onClick={() => handlePageChange(Math.min(totalPages - 1, currentPage + 1))} disabled={currentPage === totalPages - 1} className="p-3 rounded-2xl border border-slate-200 bg-white hover:bg-blue-50 hover:border-blue-900 hover:scale-105 disabled:opacity-20 transition-all shadow-sm group"><ChevronRight/></button>
                </div>
            )}
        </main>
    );
};

export default MyTicketsPage;