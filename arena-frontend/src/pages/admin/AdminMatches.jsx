import React, { useState, useEffect } from 'react';
import adminMatchesService from "../../services/adminMatchesService.js";
import MatchRow from "../../components/admin/MatchRow.jsx";
import CreateMatchWizard from "../../components/admin/CreateMatchWizard.jsx";
import { Plus } from 'lucide-react';

const AdminMatches = () => {
    const [matchesPage, setMatchesPage] = useState(null);
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(0);

    // State pentru Modal Wizard
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [step, setStep] = useState(1);
    const [formLoading, setFormLoading] = useState(false);

    // State pentru management meciuri (nou sau editat)
    const [newMatch, setNewMatch] = useState({ opponentName: '', matchDate: '', stadiumId: 1, matchImageUrl: '' });
    const [editingMatchId, setEditingMatchId] = useState(null); // null = mod creare, ID numeric = mod editare
    const [createdMatch, setCreatedMatch] = useState(null);
    const [sectors, setSectors] = useState([]);
    const [prices, setPrices] = useState({});

    useEffect(() => {
        fetchMatches();
    }, [currentPage]);

    const fetchMatches = async () => {
        setLoading(true);
        try {
            const data = await adminMatchesService.getAllMatches(currentPage, 10);
            setMatchesPage(data);
        } catch (err) {
            console.error("Eroare la încărcarea meciurilor:", err);
        } finally {
            setLoading(false);
        }
    };

    // ✅ METODA DE EDITARE: Pre-populează datele meciului ȘI prețurile anterioare salvate
    const handleStartEdit = async (match) => {
        setEditingMatchId(match.id);

        // Convertim formatul datei din backend în format acceptat de input-ul datetime-local (YYYY-MM-DDTHH:MM)
        const formattedDate = match.matchDate ? match.matchDate.substring(0, 16) : '';

        setNewMatch({
            opponentName: match.opponentName,
            matchDate: formattedDate,
            stadiumId: 1, // ID-ul implicit al stadionului
            matchImageUrl: match.matchImageUrl || ''
        });

        setCreatedMatch(match);

        try {
            // 1. Încărcăm structura sectoarelor stadionului
            const sectorsData = await adminMatchesService.getStadiumSectors(1);
            setSectors(sectorsData);

            // 2. Extragem prețurile deja existente pentru fiecare sector în parte
            const existingPrices = {};

            // Executăm apelurile în paralel pentru a menține aplicația rapidă
            await Promise.all(
                sectorsData.map(async (sector) => {
                    try {
                        const price = await adminMatchesService.getSectorPrice(match.id, sector.id);
                        if (price !== undefined && price !== null && price !== '') {
                            existingPrices[sector.id] = price;
                        }
                    } catch (err) {
                        // Dacă un sector nu are preț configurat (404), lăsăm câmpul gol în formular
                        existingPrices[sector.id] = '';
                    }
                })
            );

            // 3. Salvăm prețurile populate în starea locală
            setPrices(existingPrices);

            // Deschidem modalul la primul pas (datele de bază)
            setIsModalOpen(true);
            setStep(1);
        } catch (err) {
            console.error("Eroare la inițializarea modului de editare:", err);
            alert("Nu s-au putut pre-popula complet datele meciului.");
        }
    };

    // ✅ METODA UNIFICATĂ (CREARE SAU UPDATE): Știe automat ce metodă de backend să apeleze
    const handleCreateOrUpdateMatch = async (e) => {
        e.preventDefault();
        setFormLoading(true);
        try {
            if (editingMatchId) {
                // Mod Editare: Apelăm PUT
                const updatedData = await adminMatchesService.updateMatch(editingMatchId, newMatch);
                setCreatedMatch(updatedData);
            } else {
                // Mod Creare: Apelăm POST
                const matchData = await adminMatchesService.createMatch(newMatch);
                setCreatedMatch(matchData);
            }

            // Dacă primul pas a fost salvat cu succes, trecem la configurarea/actualizarea prețurilor
            setStep(2);
        } catch (err) {
            console.error("Eroare la salvarea datelor meciului:", err);
            alert("Eroare la procesarea meciului. Verifică datele introduse sau configurarea rutelor.");
        } finally {
            setFormLoading(false);
        }
    };

    const handleSavePrices = async () => {
        setFormLoading(true);
        try {
            const pricesPayload = sectors.map(sector => ({
                matchId: createdMatch.id,
                sectorId: sector.id,
                price: parseFloat(prices[sector.id] || 0)
            }));
            await adminMatchesService.setMatchPrices(pricesPayload);
            setIsModalOpen(false);
            resetForm();
            fetchMatches();
        } catch (err) {
            console.error("Eroare la salvarea prețurilor:", err);
            alert("Eroare la salvarea prețurilor.");
        } finally {
            setFormLoading(false);
        }
    };

    const handleDeleteMatch = async (matchId) => {
        try {
            await adminMatchesService.deleteMatch(matchId);
            alert("Meciul și prețurile sectoarelor au fost șterse cu succes!");
            fetchMatches();
        } catch (err) {
            console.error("Eroare la ștergerea meciului:", err);
            alert("Nu s-a putut efectua ștergerea.");
        }
    };

    const handlePublishMatch = async (matchId) => {
        try {
            await adminMatchesService.publishMatch(matchId);
            alert("Meciul a fost publicat cu succes! Fanii au fost notificați prin e-mail.");
            fetchMatches();
        } catch (err) {
            console.error("Eroare la publicarea meciului:", err);
            const errorMsg = err.response?.data?.message || "Nu s-a putut publica meciul. Verifică prețurile sectoarelor.";
            alert(errorMsg);
        }
    };

    const handleStatusChange = async (matchId, newStatus) => {
        try {
            await adminMatchesService.updateMatchStatus(matchId, newStatus);
            alert(`Statusul meciului a fost actualizat cu succes în [${newStatus}]!`);
            fetchMatches();
        } catch (err) {
            console.error("Eroare la modificarea statusului:", err);
            const errorMsg = err.response?.data?.message || "Nu s-a putut actualiza statusul meciului.";
            alert(errorMsg);
        }
    };

    const resetForm = () => {
        setStep(1);
        setNewMatch({ opponentName: '', matchDate: '', stadiumId: 1, matchImageUrl: '' });
        setPrices({});
        setCreatedMatch(null);
        setEditingMatchId(null); // Resetăm starea modului de editare înapoi la default
    };

    if (loading) return (
        <div className="min-h-[60vh] flex items-center justify-center font-sans">
            <div className="flex flex-col items-center gap-4">
                <div className="w-12 h-12 border-4 border-slate-200 border-t-blue-600 rounded-full animate-spin"></div>
                <p className="text-[10px] font-black uppercase text-slate-400 tracking-[0.3em]">Sincronizare Catalog...</p>
            </div>
        </div>
    );

    return (
        <div className="min-h-screen bg-slate-50 p-8 pt-24 font-sans text-slate-900">
            <div className="max-w-7xl mx-auto">

                {/* Header */}
                <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6 mb-10">
                    <div>
                        <h1 className="text-3xl font-black text-slate-900 uppercase italic tracking-tighter leading-none">
                            Gestiune <span className="text-blue-600">Evenimente</span>
                        </h1>
                        <p className="text-slate-500 font-bold text-[10px] uppercase tracking-widest mt-2">Programare meciuri și configurare ticketing</p>
                    </div>

                    <button
                        onClick={() => setIsModalOpen(true)}
                        className="bg-blue-600 text-white px-8 py-4 rounded-2xl font-black uppercase text-[10px] tracking-[0.2em] flex items-center gap-3 hover:bg-red-600 transition-all shadow-xl active:scale-95"
                    >
                        <Plus size={18} /> Adaugă Meci Nou
                    </button>
                </div>

                {/* Tabel Meciuri */}
                <div className="bg-white rounded-[2.5rem] border border-slate-100 shadow-2xl overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="bg-slate-950 text-white">
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest text-center border-r border-white/5">Info</th>
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest border-r border-white/5">Meci</th>
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest text-center border-r border-white/5">Status</th>
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest text-center border-r border-white/5">Publicat</th>
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest text-right">Acțiuni</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-50">
                            {matchesPage?.content?.map((match) => (
                                <MatchRow
                                    key={match.id}
                                    match={match}
                                    onDelete={handleDeleteMatch}
                                    onPublish={handlePublishMatch}
                                    onStatusChange={handleStatusChange}
                                    onEdit={handleStartEdit}
                                />
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Wizard Component */}
                <CreateMatchWizard
                    isOpen={isModalOpen}
                    onClose={() => { setIsModalOpen(false); resetForm(); }}
                    step={step}
                    formLoading={formLoading}
                    newMatch={newMatch}
                    setNewMatch={setNewMatch}
                    createdMatch={createdMatch}
                    sectors={sectors}
                    prices={prices}
                    setPrices={setPrices}
                    onCreateMatch={handleCreateOrUpdateMatch}
                    onSavePrices={handleSavePrices}
                />
            </div>
        </div>
    );
};

export default AdminMatches;