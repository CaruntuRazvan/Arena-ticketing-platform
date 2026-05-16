import { useState, useEffect } from 'react';
import { ShieldCheck, Zap, Star, Calendar } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import matchService from '../services/matchService';

const HomePage = () => {
    const { user, profile } = useAuth();
    const navigate = useNavigate();
    const [nextMatch, setNextMatch] = useState(null);
    const [loadingMatch, setLoadingMatch] = useState(true);

    useEffect(() => {
        const loadHeroMatch = async () => {
            try {
                const data = await matchService.getUpcomingMatches(0, 1);
                if (data.content && data.content.length > 0) {
                    setNextMatch(data.content[0]);
                }
            } catch (err) {
                console.error("Eroare la încărcarea meciului principal:", err);
            } finally {
                setLoadingMatch(false);
            }
        };
        loadHeroMatch();
    }, []);

    return (
        <main className="max-w-6xl mx-auto mt-12 p-6">
            <div className="grid md:grid-cols-2 gap-12 items-center">
                <div className="space-y-6">
                    <div className="flex items-center gap-2">
                        <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-xs font-bold uppercase">Oficial</span>
                        <span className="bg-yellow-100 text-yellow-700 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider">Echipa Națională</span>
                    </div>

                    <h2 className="text-6xl font-black leading-tight text-blue-900">
                        {user ? (
                            <>Bine ai revenit, <span className="text-red-600">{profile?.firstName || user}</span>!</>
                        ) : (
                            <>Susține <span className="text-yellow-500">România</span> pe Arena Națională</>
                        )}
                    </h2>

                    <p className="text-lg text-slate-600 leading-relaxed border-l-4 border-yellow-400 pl-4 bg-white py-2 rounded-r-lg shadow-sm">
                        {user
                            ? "Ești logat în contul tău de suporter oficial. Verifică meciurile următoare și punctele de loialitate acumulate."
                            : "Fii al 12-lea jucător! Înregistrează-te pentru a avea acces la cele mai bune locuri."
                        }
                    </p>

                    <div className="flex flex-wrap gap-4 pt-4">
                        <div className="flex items-center gap-2 text-sm font-semibold text-slate-700 bg-white p-3 rounded-xl shadow-sm border border-slate-100">
                            <ShieldCheck className="w-5 h-5 text-blue-600" /> Plăți Securizate
                        </div>
                        <div className="flex items-center gap-2 text-sm font-semibold text-slate-700 bg-white p-3 rounded-xl shadow-sm border border-slate-100">
                            <Zap className="w-5 h-5 text-yellow-500" /> Acces Rapid
                        </div>
                    </div>
                </div>

                <div className="relative group">
                    <div className="absolute -inset-1 bg-gradient-to-r from-blue-600 via-yellow-400 to-red-600 rounded-2xl blur opacity-25 group-hover:opacity-40 transition-opacity"></div>
                    <div className="relative bg-white p-8 rounded-2xl shadow-xl border border-slate-100">
                        <div className="flex justify-between items-start mb-6">
                            <h3 className="text-2xl font-black text-blue-900 italic uppercase">Următorul <span className="text-red-600">Meci</span></h3>
                            <Star className="w-6 h-6 text-yellow-400 fill-yellow-400" />
                        </div>

                        {loadingMatch ? (
                            <div className="animate-pulse flex flex-col gap-4">
                                <div className="h-20 bg-slate-100 rounded-xl"></div>
                                <div className="h-12 bg-slate-200 rounded-xl"></div>
                            </div>
                        ) : nextMatch ? (
                            <div className="space-y-4">
                                <div className="bg-slate-50 p-6 rounded-xl border border-dashed border-slate-300 text-center relative overflow-hidden text-blue-900">
                                    <div className="text-[10px] font-black uppercase tracking-[0.2em] mb-2 text-blue-700">{nextMatch.status || 'Eveniment Viitor'}</div>
                                    <div className="text-2xl font-black tracking-tight leading-none mb-3">
                                        ROMÂNIA <span className="text-red-600 italic">vs</span> {nextMatch.opponentName}
                                    </div>
                                    <div className="flex justify-center items-center gap-3 text-sm font-bold text-slate-500">
                                        <Calendar className="w-4 h-4 text-red-600" />
                                        {new Date(nextMatch.matchDate).toLocaleDateString('ro-RO', { day: 'numeric', month: 'long' })}
                                        <span className="text-slate-300">|</span>
                                        {new Date(nextMatch.matchDate).toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' })}
                                    </div>
                                </div>
                                <button
                                    onClick={() => navigate('/matches')}
                                    className="w-full py-4 bg-blue-900 text-white rounded-xl font-black uppercase text-sm tracking-widest hover:bg-red-600 transition-all shadow-lg active:scale-[0.98]"
                                >
                                    {user ? "Cumpără Bilete" : "Vezi Detalii Meci"}
                                </button>
                            </div>
                        ) : (
                            <div className="text-center py-6 text-slate-400 font-bold italic">Momentan nu sunt meciuri programate.</div>
                        )}
                    </div>
                </div>
            </div>
        </main>
    );
};

export default HomePage;