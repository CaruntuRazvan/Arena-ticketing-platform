import React, { useState, useEffect } from 'react';
import adminMatchesService from '../../services/adminMatchesService.js';
import adminTicketsService from '../../services/adminTicketsService.js';
import { BarChart3, DollarSign, Ticket, Percent, Users, TrendingUp, Filter } from 'lucide-react';

const AdminAnalytics = () => {
    const [matches, setMatches] = useState([]);
    const [selectedMatchId, setSelectedMatchId] = useState("");
    const [loading, setLoading] = useState(false);

    // Stări pentru datele combinate calculate în frontend
    const [processedSectors, setProcessedSectors] = useState([]);
    const [overallStats, setOverallStats] = useState({ totalRevenue: 0, totalSold: 0, totalCapacity: 0, percentage: 0, averagePrice: 0 });

    // 1. Încărcăm lista de meciuri la inițializare pentru a popula dropdown-ul de filtrare
    useEffect(() => {
        const loadMatches = async () => {
            try {
                const data = await adminMatchesService.getAllMatches(0, 100);
                // Afișăm doar meciurile care sunt publicate (la care se pot vinde bilete)
                setMatches(data?.content?.filter(m => m.published) || []);
            } catch (err) {
                console.error("Eroare la încărcarea meciurilor:", err);
            }
        };
        loadMatches();
    }, []);

    // 2. Când adminul selectează un meci, rulăm agregarea de date
    useEffect(() => {
        if (!selectedMatchId) {
            setProcessedSectors([]);
            return;
        }

        const loadCompleteAudit = async () => {
            setLoading(true);
            try {
                // API Composition: Interogăm ambele microservicii în paralel pentru performanță
                const [salesData, sectorsStructure] = await Promise.all([
                    adminTicketsService.getMatchAnalytics(selectedMatchId), // din ticketing-service
                    adminMatchesService.getStadiumSectors(1)               // din catalog-service (Stadion ID: 1)
                ]);

                let calculatedTotalCapacity = 0;
                let calculatedTotalSold = 0;

                // Combinăm structura fizică a stadionului cu vânzările din JSON-ul tău
                const detailedAnalytics = sectorsStructure.map(sector => {
                    // Căutăm în sectorsAnalytics elementul care are același sectorId
                    const salesInfo = salesData.sectorsAnalytics?.find(s => s.sectorId === sector.id);

                    const sold = salesInfo ? salesInfo.ticketsSold : 0;
                    const revenue = salesInfo ? salesInfo.revenue : 0;

                    // Calculăm capacitatea reală a sectorului din configurația stadionului
                    const capacity = sector.totalSeats || (sector.rows * sector.seatsPerRow) || 100;

                    calculatedTotalCapacity += capacity;
                    calculatedTotalSold += sold;

                    // Calculăm procentul de ocupare specific acestui sector
                    const attendancePercentage = capacity > 0 ? (sold / capacity) * 100 : 0;

                    return {
                        id: sector.id,
                        sectorName: sector.name || `Sector ${sector.id}`,
                        ticketsSold: sold,
                        sectorCapacity: capacity,
                        attendancePercentage: Math.round(attendancePercentage * 10) / 10,
                        revenue: revenue
                    };
                });

                setProcessedSectors(detailedAnalytics);

                // Calculăm statisticile globale pentru dashboard-ul de sus
                setOverallStats({
                    totalRevenue: salesData.totalRevenue || 0,
                    totalSold: calculatedTotalSold,
                    totalCapacity: calculatedTotalCapacity,
                    percentage: calculatedTotalCapacity > 0 ? (calculatedTotalSold / calculatedTotalCapacity) * 100 : 0,
                    averagePrice: calculatedTotalSold > 0 ? (salesData.totalRevenue / calculatedTotalSold) : 0
                });

            } catch (err) {
                console.error("Eroare la generarea auditului:", err);
                alert("Nu s-au putut cumula datele din microservicii. Asigură-te că ambele servicii sunt online.");
            } finally {
                setLoading(false);
            }
        };

        loadCompleteAudit();
    }, [selectedMatchId]);

    return (
        <div className="min-h-screen bg-slate-50 p-8 pt-24 font-sans text-slate-900">
            <div className="max-w-7xl mx-auto">

                {/* Header & Controls */}
                <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-6 mb-10">
                    <div>
                        <div className="flex items-center gap-3 mb-2">
                            <div className="bg-blue-600 p-2 rounded-xl text-white shadow-lg">
                                <BarChart3 className="w-6 h-6" />
                            </div>
                            <h1 className="text-3xl font-black text-slate-900 uppercase italic tracking-tighter leading-none">
                                Audit Financiar <span className="text-blue-600">&</span> Ocupare
                            </h1>
                        </div>
                        <p className="text-slate-400 font-bold text-[10px] uppercase tracking-widest ml-1">
                            Analiză în timp real a veniturilor per meci și capacități stadioane
                        </p>
                    </div>

                    {/* Selector Meci */}
                    <div className="relative w-full md:w-96">
                        <div className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none">
                            <Filter className="w-4 h-4" />
                        </div>
                        <select
                            value={selectedMatchId}
                            onChange={(e) => setSelectedMatchId(e.target.value)}
                            className="w-full bg-white border-2 border-slate-100 rounded-2xl py-4 pl-11 pr-10 outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-600/5 shadow-sm text-xs font-black uppercase tracking-wider cursor-pointer appearance-none text-slate-700"
                        >
                            <option value="">Selectează un meci publicat...</option>
                            {matches.map(match => (
                                <option key={match.id} value={match.id}>
                                    România vs {match.opponentName}
                                </option>
                            ))}
                        </select>
                        <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400 font-bold text-[8px]">▼</div>
                    </div>
                </div>

                {loading && (
                    <div className="py-20 flex flex-col items-center gap-4">
                        <div className="w-10 h-10 border-4 border-slate-200 border-t-blue-600 rounded-full animate-spin"></div>
                        <p className="text-[9px] font-black uppercase tracking-widest text-slate-400">Se calculează balanța contabilă...</p>
                    </div>
                )}

                {!loading && processedSectors.length === 0 && (
                    <div className="bg-white border border-slate-100 rounded-[2.5rem] p-20 text-center shadow-xl opacity-60">
                        <TrendingUp size={48} className="mx-auto text-slate-300 mb-4 animate-pulse" />
                        <h3 className="font-black uppercase text-lg italic text-slate-700">Niciun raport generat</h3>
                        <p className="text-xs font-bold text-slate-400 uppercase tracking-wider mt-1">Alege un eveniment din colțul de sus pentru a extrage datele de ocupare și încasări</p>
                    </div>
                )}

                {/* Dashboard-ul cu Date Active */}
                {!loading && processedSectors.length > 0 && (
                    <div className="space-y-10 animate-in fade-in-50 duration-300">

                        {/* KPI Widgets */}
                        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-6">

                            {/* Card Venit */}
                            <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-xl relative overflow-hidden">
                                <div className="absolute right-4 top-4 bg-emerald-50 text-emerald-600 p-2 rounded-xl">
                                    <DollarSign size={20} />
                                </div>
                                <span className="text-[9px] font-black text-slate-400 uppercase tracking-widest">Venituri Totale</span>
                                <h2 className="text-2xl font-black text-slate-900 italic mt-2">
                                    {overallStats.totalRevenue?.toFixed(2)} <span className="text-xs not-italic font-bold text-slate-400">RON</span>
                                </h2>
                                <p className="text-[9px] font-bold text-emerald-600 uppercase mt-2 bg-emerald-50 px-2 py-0.5 rounded-md w-max">Încasat Complet</p>
                            </div>

                            <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-xl relative overflow-hidden">
                                <div className="absolute right-4 top-4 bg-blue-50 text-blue-600 p-2 rounded-xl">
                                    <Ticket size={20} />
                                </div>
                                <span className="text-[9px] font-black text-slate-400 uppercase tracking-widest">Bilete Vândute</span>
                                <h2 className="text-2xl font-black text-slate-900 italic mt-2">
                                    {overallStats.totalSold} / <span className="text-sm font-bold text-slate-400">{overallStats.totalCapacity}</span>
                                </h2>
                                <p className="text-[9px] font-bold text-slate-400 uppercase mt-2">Locuri rezervate pe stadion</p>
                            </div>

                            {/* Card Procent Ocupare */}
                            <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-xl relative overflow-hidden">
                                <div className="absolute right-4 top-4 bg-amber-50 text-amber-600 p-2 rounded-xl">
                                    <Percent size={20} />
                                </div>
                                <span className="text-[9px] font-black text-slate-400 uppercase tracking-widest">Grad General Ocupare</span>
                                <h2 className="text-2xl font-black text-slate-900 italic mt-2">
                                    {overallStats.percentage?.toFixed(1)}%
                                </h2>
                                <p className="text-[9px] font-bold text-slate-400 uppercase mt-2">Proporție suporteri prezenți</p>
                            </div>

                            {/* Card Preț Mediu */}
                            <div className="bg-white p-6 rounded-3xl border border-slate-100 shadow-xl relative overflow-hidden">
                                <div className="absolute right-4 top-4 bg-purple-50 text-purple-600 p-2 rounded-xl">
                                    <Users size={20} />
                                </div>
                                <span className="text-[9px] font-black text-slate-400 uppercase tracking-widest">Preț Mediu / Bilet</span>
                                <h2 className="text-2xl font-black text-slate-900 italic mt-2">
                                    {overallStats.averagePrice?.toFixed(2)} <span className="text-xs not-italic font-bold text-slate-400">RON</span>
                                </h2>
                                <p className="text-[9px] font-bold text-slate-400 uppercase mt-2">Valoare medie de achiziție</p>
                            </div>
                        </div>

                        {/* Tabelul Detaliat pe Sectoare */}
                        <div className="bg-white rounded-[2.5rem] border border-slate-100 shadow-2xl overflow-hidden">
                            <div className="p-6 bg-slate-950 text-white flex justify-between items-center">
                                <h3 className="font-black uppercase italic text-xs tracking-wider">Defalcare Financiară detaliată per sector</h3>
                                <span className="text-[9px] font-black bg-blue-600 px-3 py-1 rounded-full uppercase">Meci ID: #{selectedMatchId}</span>
                            </div>

                            <div className="overflow-x-auto">
                                <table className="w-full text-left border-collapse">
                                    <thead>
                                    <tr className="bg-slate-900 text-slate-400 text-[10px] font-black uppercase tracking-widest border-b border-slate-800">
                                        <th className="p-6">Denumire Sector</th>
                                        <th className="p-6 text-center">Bilete Vândute</th>
                                        <th className="p-6">Grad Ocupare Sector</th>
                                        <th className="p-6 text-right">Venit Sector</th>
                                    </tr>
                                    </thead>
                                    <tbody className="divide-y divide-slate-100">
                                    {processedSectors.map((sector) => (
                                        <tr key={sector.id} className="hover:bg-slate-50/80 transition-colors">
                                            <td className="p-6">
                                                <span className="font-black text-slate-900 text-sm uppercase italic">{sector.sectorName}</span>
                                            </td>
                                            <td className="p-6 text-center">
                                                    <span className="font-bold text-slate-800 text-xs">
                                                        {sector.ticketsSold} <span className="text-slate-400 font-normal">/ {sector.sectorCapacity}</span>
                                                    </span>
                                            </td>
                                            <td className="p-6 w-1/3">
                                                <div className="flex items-center gap-3">
                                                    <div className="w-full bg-slate-100 h-3 rounded-full overflow-hidden p-0.5 border border-slate-200/50">
                                                        <div
                                                            className={`h-full rounded-full transition-all duration-500 ${
                                                                sector.attendancePercentage > 75 ? 'bg-emerald-500' :
                                                                    sector.attendancePercentage > 35 ? 'bg-blue-500' : 'bg-amber-500'
                                                            }`}
                                                            style={{ width: `${Math.min(sector.attendancePercentage, 100)}%` }}
                                                        ></div>
                                                    </div>
                                                    <span className="font-black text-xs italic text-slate-800 min-w-10 text-right">
                                                            {sector.attendancePercentage?.toFixed(1)}%
                                                        </span>
                                                </div>
                                            </td>
                                            <td className="p-6 text-right">
                                                <div className="font-black text-slate-950 text-sm italic">
                                                    {sector.revenue?.toFixed(2)} <span className="text-[10px] not-italic text-slate-400 font-bold">RON</span>
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>

                    </div>
                )}
            </div>
        </div>
    );
};

export default AdminAnalytics;