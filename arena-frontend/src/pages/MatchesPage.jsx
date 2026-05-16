import { useState, useEffect } from 'react';
import { Calendar, MapPin, Ticket } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import matchService from '../services/matchService';
import MatchTrivia from '../components/MatchTrivia'; // Importăm componenta nouă

const MatchesPage = () => {
    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchMatches = async () => {
            try {
                const data = await matchService.getUpcomingMatches(0, 10);
                if (data && data.content) {
                    console.log("Primul meci:", data.content[0]);
                    setMatches(data.content);
                }
            } catch (err) {
                console.error("Nu s-au putut încărca meciurile:", err);
            } finally {
                setLoading(false);
            }
        };
        fetchMatches();
    }, []);

    if (loading) return (
        <div className="min-h-screen flex items-center justify-center">
            <div className="text-center font-black text-blue-900 uppercase italic animate-pulse">
                Se încarcă atmosfera de pe stadion...
            </div>
        </div>
    );

    return (
        <div className="min-h-screen bg-slate-50 p-6 md:p-12">
            <header className="max-w-5xl mx-auto mb-16 text-center">
                <h1 className="text-5xl md:text-6xl font-black text-blue-900 uppercase tracking-tighter italic">
                    Bătălii pe <span className="text-red-600">Arenă</span>
                </h1>
                <p className="text-slate-500 mt-4 text-lg font-medium">
                    Istorie, pasiune și statistici generate de AI pentru meciurile tricolorilor.
                </p>
            </header>

            <div className="max-w-5xl mx-auto space-y-12">
                {matches.length > 0 ? (
                    matches.map((match) => (
                        <div
                            key={match.id}
                            className="bg-white rounded-[2.5rem] overflow-hidden shadow-xl border border-slate-100 flex flex-col md:flex-row hover:shadow-2xl transition-all duration-500 group"
                        >
                            {/* Partea Stângă: Imagine Hero */}
                            <div className="md:w-1/3 relative h-64 md:h-auto overflow-hidden">
                                <img
                                    src={match.matchImageUrl || "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?q=80&w=800"}
                                    alt="Stadium"
                                    className="w-full h-full object-contain group-hover:scale-110 transition-transform duration-700"
                                />
                                <div className="absolute inset-0 bg-gradient-to-t from-blue-900/90 via-blue-900/20 to-transparent"></div>

                                <div className="absolute bottom-8 left-8 text-white">
                                    <div className="text-[10px] font-black uppercase tracking-[0.2em] bg-red-600 px-3 py-1 rounded-full w-fit mb-3 shadow-lg">
                                        {match.status}
                                    </div>
                                    <h3 className="text-3xl font-black uppercase leading-none tracking-tighter">
                                        ROMÂNIA <br/>
                                        <span className="text-yellow-400 italic text-2xl">vs</span> {match.opponentName}
                                    </h3>
                                </div>
                            </div>

                            {/* Partea Dreaptă: Detalii și AI Insights */}
                            <div className="md:w-2/3 p-8 md:p-10 flex flex-col justify-between">

                                {/* Info Row */}
                                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 mb-8">
                                    <div className="space-y-2">
                                        <div className="flex items-center gap-2 text-red-600 font-black text-sm uppercase tracking-wider">
                                            <Calendar className="w-5 h-5" />
                                            {new Date(match.matchDate).toLocaleDateString('ro-RO', {
                                                day: 'numeric',
                                                month: 'long',
                                                year: 'numeric'
                                            })}
                                            {' • '}
                                            {new Date(match.matchDate).toLocaleTimeString('ro-RO', {
                                                hour: '2-digit',
                                                minute: '2-digit'
                                            })}
                                        </div>
                                        <div className="flex items-center gap-2 text-slate-500 font-bold">
                                            <MapPin className="w-5 h-5 text-blue-500" />
                                            {match.stadiumName || 'Arena Națională, București'}
                                        </div>
                                    </div>

                                    <button
                                        onClick={() => navigate(`/match/${match.id}`)} // 3. Adaugă navigarea la click
                                        className="w-full bg-blue-900 text-white py-4 rounded-xl font-black uppercase text-xs tracking-widest flex items-center justify-center gap-2 hover:bg-red-600 transition-all shadow-lg active:scale-95"
                                    >
                                        <Ticket className="w-4 h-4" />
                                        Cumpără Bilete
                                    </button>
                                </div>

                                {/* Componenta AI - Se încarcă independent pentru fiecare meci */}
                                <MatchTrivia matchId={match.id} />

                            </div>
                        </div>
                    ))
                ) : (
                    <div className="text-center py-24 bg-white rounded-[3rem] border-2 border-dashed border-slate-200">
                        <p className="text-slate-400 font-black italic uppercase tracking-widest text-xl">
                            Momentan nu sunt bătălii programate.
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default MatchesPage;