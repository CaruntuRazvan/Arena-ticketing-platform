import { useState, useEffect } from 'react';
import { Trophy, Users, Calendar, MapPin, Construction, Info, Loader2, Camera } from 'lucide-react';
import matchService from '../services/matchService';

const StadiumPage = () => {
    const [stadium, setStadium] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStadiumData = async () => {
            try {
                const data = await matchService.getStadiums();
                if (data && data.length > 0) setStadium(data[0]);
            } catch (err) {
                console.error("Nu s-au putut încărca datele stadionului");
            } finally {
                setLoading(false);
            }
        };
        fetchStadiumData();
    }, []);

    // Statistici simplificate
    const stats = [
        { icon: <Calendar className="text-red-600" />, label: "Inaugurare", value: "2011", sub: "Arena Națională" },
        { icon: <Construction className="text-yellow-600" />, label: "Categorie", value: "UEFA Elite", sub: "Standard FIFA Class 4" },
        { icon: <Trophy className="text-purple-600" />, label: "Evenimente", value: "Top Level", sub: "Sport & Concerte" },
    ];

    if (loading) return (
        <div className="h-screen flex items-center justify-center bg-slate-50">
            <Loader2 className="animate-spin text-blue-900 w-12 h-12" />
        </div>
    );

    return (
        <div className="min-h-screen bg-slate-50 font-sans text-slate-900 pb-20">
            {/* HERO SECTION - IMAGINE PRINCIPALĂ */}
            <div className="relative h-[65vh] overflow-hidden">
                <img
                    src="/images/arena-exterior.jpeg"
                    alt="Arena Nationala Exterior"
                    className="w-full h-full object-cover scale-105 hover:scale-100 transition-transform duration-[2000ms]"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-slate-900 via-slate-900/20 to-transparent" />
                <div className="absolute bottom-0 left-0 p-8 md:p-16">
                    <div className="flex items-center gap-3 text-red-500 mb-4 font-black uppercase tracking-widest text-xs">
                        <MapPin size={16} /> București, România
                    </div>
                    <h1 className="text-6xl md:text-8xl font-black text-white uppercase italic tracking-tighter leading-none">
                        {stadium?.name || "Arena Națională"}
                    </h1>
                </div>
            </div>

            <div className="max-w-7xl mx-auto px-6 relative z-10">
                {/* STATS GRID - COMPACT */}
                <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 -mt-16">
                    {stats.map((stat, idx) => (
                        <div key={idx} className="bg-white p-6 md:p-8 rounded-[2rem] shadow-xl border border-slate-100 transition-all duration-300 hover:shadow-2xl">
                            <div className="mb-4 bg-slate-50 w-10 h-10 rounded-xl flex items-center justify-center">
                                {stat.icon}
                            </div>
                            <p className="text-[9px] font-black text-slate-400 uppercase tracking-widest">{stat.label}</p>
                            <h3 className="text-xl md:text-2xl font-black text-blue-950 mt-1">{stat.value}</h3>
                            <p className="text-[10px] font-bold text-slate-500 uppercase">{stat.sub}</p>
                        </div>
                    ))}
                </div>

                {/* CONTENT SECTION */}
                <div className="grid lg:grid-cols-5 gap-8 md:gap-12 mt-16">
                    {/* TEXT DESCRIPTIV */}
                    <div className="lg:col-span-3 space-y-8">
                        <section className="bg-white p-8 md:p-12 rounded-[3rem] shadow-sm border border-slate-100">
                            <h2 className="text-3xl font-black text-blue-950 uppercase italic mb-8 flex items-center gap-3">
                                <Info className="text-blue-600" size={28} /> Identitatea Arenei
                            </h2>
                            <div className="space-y-6 text-slate-600 leading-relaxed font-medium text-lg">
                                <p>
                                    Arena Națională reprezintă inima fotbalului românesc, fiind o bijuterie arhitecturală dotată cu cele mai moderne facilități europene. Designul său iconic și acoperișul retractabil o fac una dintre cele mai versatile locații sportive din regiune.
                                </p>
                                <p>
                                    Cu o atmosferă electrizantă la fiecare meci, stadionul a fost conceput pentru a oferi vizibilitate maximă din orice punct al tribunei. Este locul unde pasiunea întâlnește performanța, oferind condiții de joc la standarde UEFA Elite.
                                </p>
                            </div>
                        </section>
                    </div>

                    {/* IMAGINE SECUNDARĂ ȘI DETALII */}
                    <div className="lg:col-span-2 space-y-6">
                        <div className="rounded-[3rem] overflow-hidden shadow-2xl h-64 md:h-80 group relative">
                            <img
                                src="/images/arena-interior.jpeg"
                                alt="Stadium Interior"
                                className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700"
                            />
                            <div className="absolute inset-0 bg-blue-900/20 group-hover:bg-transparent transition-colors" />
                            <div className="absolute top-4 right-4 bg-white/90 p-3 rounded-full shadow-lg">
                                <Camera size={20} className="text-blue-950" />
                            </div>
                        </div>

                        <div className="bg-blue-950 p-10 rounded-[3rem] text-white shadow-2xl relative overflow-hidden">
                            <div className="relative z-10">
                                <h4 className="text-xl font-black uppercase italic mb-6">Facilități Gazon</h4>
                                <ul className="space-y-5 text-xs font-bold tracking-wider opacity-90">
                                    <li className="flex items-center gap-4 italic uppercase">
                                        <div className="w-2 h-2 bg-red-500 rounded-full" /> Suprafață hibridă premium
                                    </li>
                                    <li className="flex items-center gap-4 italic uppercase">
                                        <div className="w-2 h-2 bg-red-500 rounded-full" /> Sistem de drenaj ultra-rapid
                                    </li>
                                    <li className="flex items-center gap-4 italic uppercase">
                                        <div className="w-2 h-2 bg-red-500 rounded-full" /> Instalație nocturnă de ultimă generație
                                    </li>
                                    <li className="flex items-center gap-4 italic uppercase">
                                        <div className="w-2 h-2 bg-red-500 rounded-full" /> Acoperiș retractabil complet
                                    </li>
                                </ul>
                            </div>
                            <div className="absolute -right-6 -bottom-6 text-white/5 font-black text-9xl italic select-none">
                                ARENA
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default StadiumPage;