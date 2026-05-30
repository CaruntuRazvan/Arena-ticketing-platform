import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    MapPin,
    Armchair,
    Loader2,
    X,
    ShoppingCart,
    Info,
    Banknote,
    Trash2,
    AlertCircle,
    CheckCircle2
} from 'lucide-react';

// Servicii
import matchService from '../services/matchService';
import ticketService from '../services/ticketService';

// Context Autentificare
import { useAuth } from '../context/AuthContext';

const MatchDetailsPage = () => {
    const { id: matchId } = useParams();
    const navigate = useNavigate();
    const { profile } = useAuth();

    const [selectedSector, setSelectedSector] = useState(null);
    const [selectedSeats, setSelectedSeats] = useState([]);
    const [useLoyaltyPoints, setUseLoyaltyPoints] = useState(false); // State pentru bifa de discount
    const [seats, setSeats] = useState([]);
    const [loadingSector, setLoadingSector] = useState(false);
    const [loadingSeats, setLoadingSeats] = useState(false);
    const [error, setError] = useState(null);

    const sectorsUI = [
        { name: 'Peluza Sud', color: 'bg-red-600', info: 'Acces prin Poarta B' },
        { name: 'Tribuna 2', color: 'bg-yellow-500', info: 'Acces prin Poarta C' },
        { name: 'Peluza Nord', color: 'bg-blue-600', info: 'Acces prin Poarta D' },
        { name: 'Tribuna 1', color: 'bg-green-600', info: 'Acces prin Poarta A' },
        { name: 'VIP', color: 'bg-purple-600', info: 'Acces VIP - Lounge Inclus' },
    ];

    // Gestiune selecție locuri cu limită de 5
    const handleSeatClick = (seat) => {
        if (seat.occupied) return;

        setSelectedSeats(prev => {
            const isAlreadySelected = prev.find(s => s.id === seat.id);
            if (isAlreadySelected) return prev.filter(s => s.id !== seat.id);

            if (prev.length >= 5) {
                setError("Puteți selecta maxim 5 bilete per comandă!");
                return prev;
            }

            setError(null);
            return [...prev, {
                ...seat,
                sectorName: selectedSector.name,
                price: selectedSector.basePrice
            }];
        });
    };

    const clearCart = () => {
        if (window.confirm("Sigur vrei să ștergi toate locurile selectate?")) {
            setSelectedSeats([]);
            setUseLoyaltyPoints(false);
            setError(null);
        }
    };

    const handleSectorClick = async (uiInfo) => {
        setLoadingSector(true);
        setError(null);
        setSelectedSector(null);
        setSeats([]);
        try {
            const dataPromise = matchService.getSectorDetails(matchId, uiInfo.name);
            const delayPromise = new Promise(resolve => setTimeout(resolve, 700));
            const [data] = await Promise.all([dataPromise, delayPromise]);

            setSelectedSector({ ...data, color: uiInfo.color, info: uiInfo.info });
        } catch (err) {
            setError(err.response?.data?.message || "Sectorul nu este disponibil.");
        } finally {
            setLoadingSector(false);
        }
    };

    useEffect(() => {
        if (selectedSector?.id) {
            const fetchSeats = async () => {
                setLoadingSeats(true);
                try {
                    const seatsData = await matchService.getSeatsBySector(matchId, selectedSector.id);
                    setSeats(seatsData);
                } catch (err) {
                    console.error("Eroare la încărcarea scaunelor");
                } finally {
                    setLoadingSeats(false);
                }
            };
            fetchSeats();
        }
    }, [selectedSector, matchId]);

    // Logica de calcul preț
    const baseTotalPrice = selectedSeats.reduce((acc, curr) => acc + curr.price, 0);
    const canUseLoyalty = profile?.loyaltyPoints >= 10;
    const finalTotalPrice = (useLoyaltyPoints && canUseLoyalty) ? baseTotalPrice * 0.9 : baseTotalPrice;

    const handleConfirmOrder = async () => {
        if (!profile) {
            setError("Trebuie să fii logat pentru a finaliza comanda!");
            return;
        }
        if (selectedSeats.length === 0) return;
        if (selectedSeats.length > 5) {
            setError("Limita maximă este de 5 bilete!");
            return;
        }

        setLoadingSector(true);
        setError(null);

        const requestData = {
            matchId: parseInt(matchId),
            userId: profile.id,
            seatIds: selectedSeats.map(s => s.id),
            useLoyaltyPoints: useLoyaltyPoints && canUseLoyalty // Validăm bifa cu numărul de puncte
        };

        try {
            const pendingTickets = await ticketService.bookTickets(requestData);
            navigate('/checkout', {
                state: {
                    tickets: pendingTickets,
                    totalPrice: finalTotalPrice
                }
            });
        } catch (err) {
            const msg = err.response?.data?.message || "Eroare la procesarea rezervării.";
            setError(msg);
        } finally {
            setLoadingSector(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-50 p-4 md:p-10 font-sans text-slate-900">
            <div className="max-w-[1600px] mx-auto">
                <div className="grid lg:grid-cols-12 gap-10 items-start">

                    {/* COLOANA STÂNGA: HARTA ȘI GRID LOCURI */}
                    <div className="lg:col-span-8 space-y-10">
                        <div className="bg-white p-8 rounded-[2.5rem] shadow-xl relative border border-slate-100">
                            <h1 className="text-xl font-black uppercase italic mb-6 flex items-center gap-2 text-blue-900">
                                <MapPin className="text-red-600 w-5 h-5" /> Selectează Sectorul
                            </h1>
                            <div className="relative overflow-hidden rounded-2xl bg-slate-50 border border-slate-100 shadow-inner">
                                <img src="/images/stadium-structure.png" className="w-full h-auto opacity-90" alt="Stadium" />
                                {sectorsUI.map((s, idx) => (
                                    <div
                                        key={idx}
                                        onClick={() => handleSectorClick(s)}
                                        className={`absolute cursor-pointer opacity-0 hover:opacity-30 transition-all duration-300 ${
                                            s.name === 'Peluza Sud' ? 'top-[25%] left-[6%] w-[13%] h-[50%] rounded-[30%]' :
                                                s.name === 'Tribuna 2' ? 'top-[8%] left-[25%] w-[50%] h-[15%] rounded-b-xl' :
                                                    s.name === 'Peluza Nord' ? 'top-[25%] right-[6%] w-[13%] h-[50%] rounded-[30%]' :
                                                        s.name === 'Tribuna 1' ? 'bottom-[11%] left-[25%] w-[50%] h-[15%] rounded-t-xl' :
                                                            'bottom-[26%] left-[37%] w-[26%] h-[6%] skew-x-[-15deg]'
                                        } ${s.color}`}
                                    />
                                ))}
                            </div>
                        </div>

                        {selectedSector && (
                            <div className="bg-white p-8 md:p-12 rounded-[3.5rem] shadow-2xl border border-slate-100 animate-in zoom-in-95 duration-500">
                                <div className="flex flex-col md:flex-row justify-between mb-12 items-end gap-6">
                                    <div>
                                        <div className={`inline-block px-4 py-1 rounded-full text-[10px] font-black text-white uppercase mb-4 ${selectedSector.color}`}>
                                            {selectedSector.name}
                                        </div>
                                        <h3 className="text-4xl font-black text-blue-950 uppercase italic leading-none">Alege Locul</h3>
                                    </div>
                                    <div className="bg-blue-50 text-blue-600 px-4 py-2 rounded-xl border border-blue-100 text-[10px] font-black uppercase italic">
                                        Limită: {selectedSeats.length}/5 Bilete
                                    </div>
                                </div>

                                {loadingSeats ? (
                                    <div className="py-24 flex flex-col items-center gap-4">
                                        <Loader2 className="animate-spin text-blue-600 w-12 h-12" />
                                        <span className="text-[10px] font-black text-slate-300 uppercase tracking-widest">Sincronizăm locurile...</span>
                                    </div>
                                ) : (
                                    <div className="w-full overflow-x-auto pb-10 custom-scrollbar">
                                        <div className="grid gap-3 mx-auto" style={{ gridTemplateColumns: `40px repeat(${selectedSector.seatsPerRow}, minmax(45px, 1fr))`, width: 'fit-content' }}>
                                            {seats.map((seat) => {
                                                const isFirstInRow = seat.seatNumber === 1;
                                                const isSelected = selectedSeats.some(s => s.id === seat.id);
                                                return (
                                                    <div key={`container-${seat.id}`} style={{ display: 'contents' }}>
                                                        {isFirstInRow && <div className="flex items-center justify-center text-blue-900 font-black text-xs italic border-r-2 border-slate-100 pr-2">R{seat.rowNumber}</div>}
                                                        <button
                                                            disabled={seat.occupied}
                                                            onClick={() => handleSeatClick(seat)}
                                                            className={`aspect-square rounded-xl flex items-center justify-center text-[11px] font-black transition-all border-b-4
                                                                ${seat.occupied ? 'bg-slate-200 text-slate-400 cursor-not-allowed border-slate-300 opacity-50' :
                                                                isSelected ? 'bg-yellow-400 text-blue-900 scale-110 border-yellow-600 z-10 rotate-3 shadow-xl' :
                                                                    'bg-green-500 text-white hover:scale-110 active:scale-95 shadow-md border-green-700 hover:rotate-2'}`}
                                                        >
                                                            {seat.seatNumber}
                                                        </button>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>

                    {/* COLOANA DREAPTĂ: COȘ ȘI PREȚ */}
                    <div className="lg:col-span-4 sticky top-10 space-y-6">
                        {selectedSector && (
                            <div className="bg-white rounded-[2.5rem] p-8 shadow-xl border border-slate-100 animate-in slide-in-from-top-4">
                                <p className="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em] mb-2 text-center">Preț bilet</p>
                                <div className="flex items-baseline justify-center gap-2">
                                    <span className="text-7xl font-black text-blue-950 italic tracking-tighter">{selectedSector.basePrice}</span>
                                    <span className="text-xl font-bold text-blue-600 uppercase">RON</span>
                                </div>
                            </div>
                        )}

                        <div className="bg-white rounded-[2.5rem] shadow-2xl border border-slate-100 flex flex-col h-[calc(100vh-350px)] min-h-[400px] overflow-hidden">
                            <div className="bg-blue-950 p-8 text-white shrink-0">
                                <div className="flex justify-between items-center">
                                    <h2 className="text-2xl font-black uppercase italic flex items-center gap-3 tracking-tight">
                                        <ShoppingCart className="w-6 h-6 text-blue-400" /> Coș
                                    </h2>
                                    <div className="flex items-center gap-2">
                                        <span className={`px-3 py-1 rounded-full text-[10px] font-black ${selectedSeats.length >= 5 ? 'bg-red-600' : 'bg-blue-800'}`}>
                                            {selectedSeats.length}/5 BILETE
                                        </span>
                                        {selectedSeats.length > 0 && (
                                            <button onClick={clearCart} className="p-2 bg-red-600/20 hover:bg-red-600 text-red-400 hover:text-white rounded-lg transition-all">
                                                <Trash2 className="w-4 h-4" />
                                            </button>
                                        )}
                                    </div>
                                </div>
                            </div>

                            <div className="p-6 flex-grow overflow-y-auto custom-scrollbar space-y-3">
                                {loadingSector ? (
                                    <div className="h-full flex flex-col items-center justify-center p-10 gap-4">
                                        <Loader2 className="w-10 h-10 text-blue-600 animate-spin" />
                                        <p className="text-slate-400 font-bold text-xs uppercase text-center tracking-widest">Sincronizare...</p>
                                    </div>
                                ) : selectedSeats.length === 0 ? (
                                    <div className="h-full flex flex-col items-center justify-center text-center space-y-4 opacity-20 py-10 text-slate-400">
                                        <Armchair className="w-16 h-16" />
                                        <p className="text-[10px] font-black uppercase tracking-[0.2em]">Coș gol</p>
                                    </div>
                                ) : (
                                    selectedSeats.map((s) => (
                                        <div key={s.id} className="bg-slate-50 border border-slate-100 rounded-2xl p-4 transition-all hover:bg-white hover:shadow-md animate-in slide-in-from-right-4">
                                            <div className="flex justify-between items-start">
                                                <div>
                                                    <span className="text-[9px] font-black bg-blue-100 text-blue-700 px-2 py-0.5 rounded uppercase tracking-tighter">{s.sectorName}</span>
                                                    <p className="text-blue-950 font-black text-sm uppercase italic mt-1">Rând {s.rowNumber}, Loc {s.seatNumber}</p>
                                                </div>
                                                <button onClick={() => handleSeatClick(s)} className="p-2 bg-white text-slate-300 hover:text-red-500 rounded-xl shadow-sm transition-all border border-slate-100">
                                                    <X className="w-4 h-4" />
                                                </button>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>

                            {selectedSeats.length > 0 && (
                                <div className="p-8 bg-slate-50 border-t border-slate-100 shrink-0">

                                    {/* --- SECTIUNE PUNCTE LOIALITATE --- */}
                                    {profile && (
                                        <div className={`mb-6 p-4 rounded-2xl border-2 transition-all flex flex-col gap-3 ${
                                            canUseLoyalty
                                                ? (useLoyaltyPoints ? 'bg-blue-50 border-blue-200' : 'bg-white border-slate-100')
                                                : 'bg-slate-50 border-slate-100 opacity-60'
                                        }`}>
                                            <div className="flex items-center justify-between">
                                                <div className="flex items-center gap-3">
                                                    <div className={`p-2 rounded-xl ${useLoyaltyPoints ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-400'}`}>
                                                        <Banknote className="w-4 h-4" />
                                                    </div>
                                                    <div>
                                                        <p className="text-[10px] font-black text-blue-900 uppercase tracking-tighter leading-none">Puncte Loialitate</p>
                                                        <p className="text-[9px] font-bold text-slate-400 uppercase mt-1">Sold: {profile.loyaltyPoints || 0}</p>
                                                    </div>
                                                </div>
                                                <input
                                                    type="checkbox"
                                                    disabled={!canUseLoyalty}
                                                    checked={useLoyaltyPoints}
                                                    onChange={(e) => setUseLoyaltyPoints(e.target.checked)}
                                                    className="w-5 h-5 rounded-lg border-slate-200 text-blue-600 focus:ring-blue-500 cursor-pointer disabled:cursor-not-allowed"
                                                />
                                            </div>

                                            {canUseLoyalty ? (
                                                useLoyaltyPoints && (
                                                    <div className="flex items-center gap-2 text-green-600 animate-in slide-in-from-left-2">
                                                        <CheckCircle2 size={12} />
                                                        <span className="text-[9px] font-black uppercase italic tracking-tighter">10 puncte folosite (Reducere 10%)</span>
                                                    </div>
                                                )
                                            ) : (
                                                <div className="flex items-center gap-2 text-red-400 italic">
                                                    <Info size={12} />
                                                    <span className="text-[9px] font-bold uppercase tracking-tighter">Minim 10 puncte necesare</span>
                                                </div>
                                            )}
                                        </div>
                                    )}

                                    <div className="flex justify-between items-end mb-6">
                                        <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Total Plată</span>
                                        <div className="text-right">
                                            {useLoyaltyPoints && canUseLoyalty && (
                                                <p className="text-xs text-slate-300 line-through font-bold mb-[-4px] italic">{baseTotalPrice} RON</p>
                                            )}
                                            <p className="text-4xl font-black text-blue-950 italic">
                                                {finalTotalPrice.toFixed(2)} <span className="text-sm font-normal">RON</span>
                                            </p>
                                        </div>
                                    </div>

                                    <button
                                        onClick={handleConfirmOrder}
                                        disabled={loadingSector}
                                        className={`w-full py-5 rounded-2xl font-black uppercase text-xs tracking-widest transition-all shadow-xl flex items-center justify-center gap-3 active:scale-95
                                            ${loadingSector ? 'bg-slate-400' : 'bg-blue-600 text-white hover:bg-red-600'}`}
                                    >
                                        {loadingSector ? <Loader2 className="w-5 h-5 animate-spin" /> : <><Banknote className="w-5 h-5" /> Confirmă Comanda</>}
                                    </button>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* NOTIFICARE EROARE */}
            {error && (
                <div className="fixed bottom-10 left-1/2 -translate-x-1/2 md:left-auto md:right-10 md:translate-x-0 w-[90%] md:w-96 bg-red-600 text-white p-5 rounded-2xl shadow-2xl font-black uppercase text-[10px] flex items-center gap-4 animate-in slide-in-from-bottom-10">
                    <AlertCircle className="shrink-0 w-6 h-6" />
                    <div className="flex-grow">{error}</div>
                    <button onClick={() => setError(null)}><X className="w-4 h-4" /></button>
                </div>
            )}
        </div>
    );
};

export default MatchDetailsPage;