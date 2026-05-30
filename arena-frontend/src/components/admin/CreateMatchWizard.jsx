import React from 'react';
import { X, Clock, ImageIcon, Info, Save } from 'lucide-react';

const CreateMatchWizard = ({
                               isOpen, onClose, step, formLoading, newMatch, setNewMatch,
                               createdMatch, sectors, prices, setPrices, onCreateMatch, onSavePrices
                           }) => {
    if (!isOpen) return null;

    // Logăm starea curentă la fiecare randare a modalului pentru urmărirea câmpurilor
    console.log("=== [CreateMatchWizard Render] Câmpuri curente ===");
    console.log("Adversar:", newMatch.opponentName);
    console.log("Data:", newMatch.matchDate);
    console.log("URL Imagine din Stare:", newMatch.matchImageUrl);
    console.log("================================================");

    const handleFormSubmit = (e) => {
        console.log("🚀 [Pasul 1] Se trimite formularul meciului către backend!");
        console.log("Payload trimis:", newMatch);
        onCreateMatch(e);
    };

    return (
        <div className="fixed inset-0 z-[200] flex items-center justify-center p-4">
            <div className="absolute inset-0 bg-slate-950/60 backdrop-blur-sm" onClick={() => !formLoading && onClose()}></div>

            <div className="relative bg-white w-full max-w-2xl rounded-[3rem] shadow-2xl overflow-hidden animate-in zoom-in-95 duration-300">
                {/* Modal Header */}
                <div className="bg-slate-900 p-8 text-white flex justify-between items-center">
                    <div>
                        <h2 className="text-xl font-black uppercase italic leading-none">
                            {step === 1 ? "Pasul 1: Date Meci" : "Pasul 2: Prețuri Sectoare"}
                        </h2>
                        <p className="text-slate-400 text-[10px] font-bold uppercase mt-2 tracking-widest">
                            {step === 1 ? "Configurare informații de bază" : `Setare prețuri pentru ${createdMatch?.opponentName}`}
                        </p>
                    </div>
                    <button onClick={onClose} className="p-2 hover:bg-white/10 rounded-full transition-all"><X/></button>
                </div>

                <div className="p-10">
                    {step === 1 ? (
                        <form onSubmit={handleFormSubmit} className="space-y-6">
                            <div className="grid md:grid-cols-2 gap-6">
                                <div className="space-y-2">
                                    <label className="text-[10px] font-black uppercase text-slate-400 ml-1">Nume Adversar</label>
                                    <input
                                        type="text" required
                                        className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 text-sm font-bold outline-none focus:border-blue-600 transition-all"
                                        placeholder="ex: Germania"
                                        value={newMatch.opponentName}
                                        onChange={e => setNewMatch({...newMatch, opponentName: e.target.value})}
                                    />
                                </div>
                                <div className="space-y-2">
                                    <label className="text-[10px] font-black uppercase text-slate-400 ml-1">Data Meciului</label>
                                    <input
                                        type="datetime-local" required
                                        className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 text-sm font-bold outline-none focus:border-blue-600 transition-all"
                                        value={newMatch.matchDate}
                                        onChange={e => setNewMatch({...newMatch, matchDate: e.target.value})}
                                    />
                                </div>
                            </div>

                            {/* Dropdown Stilizat pentru Imagini cu Log-uri integrate */}
                            <div className="space-y-2">
                                <label className="text-[10px] font-black uppercase text-slate-400 ml-1">Imagine Banner / Adversar</label>
                                <div className="relative">
                                    <ImageIcon className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300 pointer-events-none" size={18}/>
                                    <select
                                        required
                                        className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 pl-12 pr-10 text-sm font-bold outline-none focus:border-blue-600 appearance-none transition-all cursor-pointer text-slate-700"
                                        value={newMatch.matchImageUrl || ""}
                                        onChange={e => {
                                            console.log("📸 [Dropdown Change] Valoare selectată de Admin:", e.target.value);
                                            setNewMatch({...newMatch, matchImageUrl: e.target.value});
                                        }}
                                    >
                                        <option value="" disabled>Alege o imagine din catalog...</option>
                                        <option value="/images/placeholder.jpg">Imagine Implicită (Placeholder)</option>
                                        <option value="/images/matches/ROMANIA-GERMANIA.png">Germania (Meci Acasă)</option>
                                        <option value="/images/matches/ROMANIA-SPANIA.png">Spania (Meci Acasă)</option>
                                        <option value="/images/matches/ROMANIA-FRANTA.png">Franța (Meci Acasă)</option>
                                        <option value="/images/matches/ROMANIA-ITALIA.png">Italia (Meci Acasă)</option>
                                        <option value="/images/matches/ROMANIA-ARMENIA.png">Armenia (Meci Acasă)</option>
                                    </select>
                                    {/* Săgeată custom în locul celei native */}
                                    <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400 font-bold text-[9px]">
                                        ▼
                                    </div>
                                </div>
                            </div>

                            <button
                                type="submit" disabled={formLoading}
                                className="w-full bg-blue-600 text-white py-5 rounded-2xl font-black uppercase tracking-widest hover:bg-red-600 transition-all shadow-lg flex items-center justify-center gap-3"
                            >
                                {formLoading ? <Clock className="animate-spin"/> : "Continuă la Prețuri"}
                            </button>
                        </form>
                    ) : (
                        <div className="space-y-6">
                            <div className="bg-blue-50 p-5 rounded-2xl border border-blue-100 flex items-start gap-4">
                                <Info className="text-blue-600 shrink-0 mt-1" size={20}/>
                                <p className="text-[11px] font-bold text-blue-900 leading-relaxed uppercase">
                                    Am identificat {sectors.length} sectoare pentru stadionul selectat. Introduceți prețul biletului pentru fiecare.
                                </p>
                            </div>

                            <div className="grid sm:grid-cols-2 gap-4 max-h-[350px] overflow-y-auto pr-2 custom-scrollbar">
                                {sectors.map(sector => (
                                    <div key={sector.id} className="bg-slate-50 border-2 border-slate-100 p-5 rounded-3xl flex flex-col gap-3 hover:border-blue-200 transition-all">
                                        <span className="text-[10px] font-black uppercase text-slate-400 italic tracking-widest">{sector.name}</span>
                                        <div className="relative">
                                            <input
                                                type="number" step="0.01" required
                                                className="w-full bg-white border border-slate-200 rounded-xl p-3 font-black text-blue-900 outline-none focus:ring-2 focus:ring-blue-600/10"
                                                placeholder="0.00"
                                                value={prices[sector.id] || ''}
                                                onChange={e => setPrices({...prices, [sector.id]: e.target.value})}
                                            />
                                            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-[10px] font-black text-slate-300">RON</span>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            <button
                                onClick={() => {
                                    console.log("💰 [Pasul 2] Se salvează prețurile pe sectoare. Starea curentă a prețurilor:", prices);
                                    onSavePrices();
                                }}
                                disabled={formLoading}
                                className="w-full bg-emerald-600 text-white py-5 rounded-2xl font-black uppercase tracking-widest hover:bg-slate-900 transition-all shadow-xl flex items-center justify-center gap-3"
                            >
                                {formLoading ? <Clock className="animate-spin"/> : <><Save size={18}/> Salvează și Publică</>}
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default CreateMatchWizard;