import { useState, useEffect } from 'react';
import { Sparkles, Quote } from 'lucide-react';
import matchService from '../services/matchService'; // Importăm serviciul nostru

const MatchTrivia = ({ matchId }) => {
    const [trivia, setTrivia] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchTrivia = async () => {
            try {
                // Folosim metoda din matchService - axios va pune automat /api și Token-ul
                const data = await matchService.getMatchTrivia(matchId);
                setTrivia(data);
            } catch (err) {
                setTrivia("Momentan nu am putut prelua curiozități pentru acest meci.");
            } finally {
                setLoading(false);
            }
        };

        if (matchId) fetchTrivia();
    }, [matchId]);

    return (
        <div className="bg-blue-50/50 rounded-2xl p-6 border border-blue-100 relative mt-4">
            <div className="flex items-center gap-2 mb-3">
                <Sparkles className="w-5 h-5 text-yellow-500 fill-yellow-500" />
                <span className="text-[10px] font-black text-blue-900 uppercase tracking-tighter italic">AI Arena Insights</span>
            </div>

            {loading ? (
                <div className="flex flex-col gap-2 animate-pulse">
                    <div className="h-3 bg-blue-200/30 rounded w-full"></div>
                    <div className="h-3 bg-blue-200/30 rounded w-5/6"></div>
                </div>
            ) : (
                <div className="relative">
                    <Quote className="absolute -left-3 -top-3 w-8 h-8 text-blue-200/40 -z-10" />
                    <p className="text-slate-700 text-sm leading-relaxed whitespace-pre-line">
                        {trivia}
                    </p>
                </div>
            )}
        </div>
    );
};

export default MatchTrivia;