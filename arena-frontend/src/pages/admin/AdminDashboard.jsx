import React, { useState, useEffect } from 'react';
import adminMatchesService from '../../services/adminMatchesService.js';
import {
    Users, Ticket, TrendingUp, Activity,
    Calendar, ArrowUpRight, Loader2, MapPin
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const AdminDashboard = () => {
    const navigate = useNavigate();
    const [matches, setMatches] = useState([]);
    const [loadingMatches, setLoadingMatches] = useState(true);

    // Înlocuit datele hardcodate cu placeholdere dinamice standard
    const stats = [
        { label: 'Utilizatori Noi', value: '--', icon: Users, color: 'text-blue-600', bg: 'bg-blue-50' },
        { label: 'Bilete Vândute', value: '--', icon: Ticket, iconColor: 'text-emerald-600', bg: 'bg-emerald-50' },
        { label: 'Venit Total', value: '0.00 RON', icon: TrendingUp, iconColor: 'text-amber-600', bg: 'bg-amber-50' },
        { label: 'Rata de Ocupare', value: '-- %', icon: Activity, iconColor: 'text-purple-600', bg: 'bg-purple-50' },
    ];

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const data = await adminMatchesService.getAllMatches(0, 4);
                setMatches(data.content);
            } catch (err) {
                console.error("Eroare la încărcarea datelor din dashboard", err);
            } finally {
                setLoadingMatches(false);
            }
        };
        fetchDashboardData();
    }, []);

    return (
        <div className="min-h-screen bg-slate-50 p-8 pt-24 font-sans text-slate-900">
            <div className="max-w-7xl mx-auto">
                {/* Header Dashboard */}
                <div className="mb-10">
                    <h1 className="text-3xl font-black text-slate-900 uppercase tracking-tighter italic leading-none">
                        Panou de Control <span className="text-blue-600">Admin</span>
                    </h1>
                    <p className="text-slate-400 font-bold text-[10px] uppercase tracking-widest mt-2">Monitorizare platformă și sincronizare servicii</p>
                </div>

                {/* Grid Statistici cu Placeholdere */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
                    {stats.map((stat, idx) => (
                        <div key={idx} className="bg-white p-6 rounded-[2rem] border border-slate-100 shadow-sm hover:shadow-md transition-all">
                            <div className="flex justify-between items-start mb-4">
                                <div className={`p-3 rounded-2xl ${stat.bg}`}>
                                    <stat.icon className={`w-6 h-6 ${stat.iconColor || stat.color}`} />
                                </div>
                                <span className="flex items-center text-[9px] font-black text-slate-400 bg-slate-50 px-2 py-1 rounded-lg uppercase tracking-wider">
                                    Live
                                </span>
                            </div>
                            <p className="text-slate-400 text-[10px] font-black uppercase tracking-widest">{stat.label}</p>
                            <h3 className="text-2xl font-black text-slate-900 mt-1">{stat.value}</h3>
                        </div>
                    ))}
                </div>

                <div className="grid lg:grid-cols-3 gap-8">
                    {/* Secțiune MECIURI REALE */}
                    <div className="lg:col-span-2 bg-white rounded-[2.5rem] p-8 border border-slate-100 shadow-sm">
                        <div className="flex justify-between items-center mb-8">
                            <h2 className="text-xl font-black text-slate-900 uppercase italic">Meciuri Active</h2>
                            <button
                                onClick={() => navigate('/admin/matches')}
                                className="text-[10px] font-black text-blue-600 uppercase tracking-widest hover:underline"
                            >
                                Vezi Tot Catalogul
                            </button>
                        </div>

                        <div className="space-y-4">
                            {loadingMatches ? (
                                <div className="flex justify-center py-10">
                                    <Loader2 className="animate-spin text-slate-300" size={32} />
                                </div>
                            ) : matches && matches.length > 0 ? (
                                matches.map((match) => (
                                    <div key={match.id} className="flex items-center justify-between p-4 rounded-2xl border border-slate-50 bg-slate-50/50 hover:bg-white hover:border-slate-200 transition-all cursor-pointer group">
                                        <div className="flex items-center gap-4">
                                            <div className="w-12 h-12 rounded-xl bg-slate-100 flex items-center justify-center text-slate-400 border border-slate-200 overflow-hidden shadow-sm">
                                                {match.matchImageUrl ? (
                                                    <img src={match.matchImageUrl} alt="match" className="w-full h-full object-cover" />
                                                ) : (
                                                    <Calendar className="w-5 h-5" />
                                                )}
                                            </div>
                                            <div>
                                                <h4 className="font-black text-slate-900 text-sm uppercase italic leading-none">
                                                    România vs {match.opponentName}
                                                </h4>
                                                <p className="text-[10px] font-bold text-slate-400 uppercase mt-1 flex items-center gap-1">
                                                    <MapPin size={10} /> {match.stadiumName} • {new Date(match.matchDate).toLocaleDateString('ro-RO')}
                                                </p>
                                            </div>
                                        </div>
                                        <div className="text-right flex items-center gap-4">
                                            <div className={`px-3 py-1 rounded-full text-[8px] font-black uppercase ${match.published ? 'bg-emerald-50 text-emerald-600 border-b-2 border-emerald-200' : 'bg-amber-50 text-amber-600 border-b-2 border-amber-200'}`}>
                                                {match.published ? 'Publicat' : 'Draft'}
                                            </div>
                                            <div className={`w-2 h-2 rounded-full ${match.status === 'SCHEDULED' ? 'bg-blue-500 animate-pulse' : 'bg-red-500'}`}></div>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <p className="text-center py-10 text-slate-400 font-bold uppercase text-[10px]">Nu există meciuri programate</p>
                            )}
                        </div>
                    </div>

                    {/* Quick Actions Sidebar */}
                    <div className="bg-blue-900 rounded-[2.5rem] p-8 text-white shadow-xl shadow-blue-900/20">
                        <h2 className="text-xl font-black uppercase italic mb-8">Acțiuni Rapide</h2>
                        <div className="space-y-4">
                            <button
                                onClick={() => navigate('/admin/matches')}
                                className="w-full bg-white/10 hover:bg-white/20 p-4 rounded-2xl text-[11px] font-black uppercase tracking-widest transition-all border border-white/10 text-left"
                            >
                                Adaugă Meci Nou
                            </button>

                            {/* ✅ BUTON ACTUALIZAT: Trimite adminul către pagina de rapoarte / analytics */}
                            <button
                                onClick={() => navigate('/admin/analytics')}
                                className="w-full bg-white/10 hover:bg-white/20 p-4 rounded-2xl text-[11px] font-black uppercase tracking-widest transition-all border border-white/10 text-left"
                            >
                                Generează Raport Vânzări
                            </button>

                            <button className="w-full bg-red-600 hover:bg-red-700 p-4 rounded-2xl text-[11px] font-black uppercase tracking-widest transition-all text-left shadow-lg">
                                Trimite Notificare Bulk
                            </button>
                        </div>

                        <div className="mt-12 p-6 bg-blue-950/50 rounded-3xl border border-white/5 text-center">
                            <p className="text-[10px] font-black text-blue-300 uppercase tracking-widest mb-2">Sistem Integrat</p>
                            <div className="flex items-center justify-center gap-2">
                                <div className="w-2 h-2 bg-emerald-500 rounded-full"></div>
                                <span className="font-mono text-[10px] text-emerald-400 uppercase tracking-tighter">Bază date sincronizată</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;