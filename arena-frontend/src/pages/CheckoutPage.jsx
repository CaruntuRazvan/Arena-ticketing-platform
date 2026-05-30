import { useLocation, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import {
    CreditCard, Lock, ArrowLeft, Timer, ShieldCheck,
    Loader2, CheckCircle2, AlertCircle, X, ShieldAlert,
    Smartphone, Check
} from 'lucide-react';
import ticketService from '../services/ticketService';

const CheckoutPage = () => {
    const { state } = useLocation();
    const navigate = useNavigate();
    const { tickets, totalPrice } = state || {};

    // State-uri Formular
    const [cardData, setCardData] = useState({ number: '', name: '', expiry: '', cvv: '' });
    const [cardType, setCardType] = useState('unknown');

    // State-uri Procesare
    const [isProcessing, setIsProcessing] = useState(false);
    const [show3DS, setShow3DS] = useState(false);
    const [smsCode, setSmsCode] = useState('');
    const [generatedCode] = useState(Math.random().toString(36).substring(2, 6).toUpperCase());
    const [paymentSuccess, setPaymentSuccess] = useState(false);
    const [timeLeft, setTimeLeft] = useState(900);

    const handleCardNumberChange = (e) => {
        let value = e.target.value.replace(/\D/g, '');
        if (value.length > 16) value = value.slice(0, 16);

        // Detecție tip card
        if (value.startsWith('4')) setCardType('visa');
        else if (/^5[1-5]/.test(value)) setCardType('mastercard');
        else setCardType('unknown');

        // Auto-format: 0000 0000 0000 0000
        const formatted = value.match(/.{1,4}/g)?.join(' ') || '';
        setCardData({ ...cardData, number: formatted });
    };

    const validateLuhn = (number) => {
        const raw = number.replace(/\s/g, '');
        if (raw.length < 13) return false;
        let sum = 0;
        for (let i = 0; i < raw.length; i++) {
            let digit = parseInt(raw[raw.length - 1 - i]);
            if (i % 2 === 1) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
        }
        return sum % 10 === 0;
    };

    // 2. Timer
    useEffect(() => {
        if (!state || !tickets) navigate('/');
        const timer = setInterval(() => setTimeLeft(prev => prev - 1), 1000);
        if (timeLeft <= 0) navigate('/');
        return () => clearInterval(timer);
    }, [timeLeft, navigate, state, tickets]);

    // 3. Logica de Plată
    const startPaymentProcess = (e) => {
        e.preventDefault();
        if (!validateLuhn(cardData.number)) {
            alert("Numărul cardului nu este valid!");
            return;
        }
        setIsProcessing(true);
        setTimeout(() => {
            setIsProcessing(false);
            setShow3DS(true);
        }, 1500);
    };

    const confirmSmsCode = async () => {
        if (smsCode.toUpperCase() !== generatedCode) {
            alert("Codul introdus este incorect!");
            return;
        }

        setIsProcessing(true);
        setShow3DS(false);

        try {
            const ticketIds = tickets.map(t => t.id);
            await ticketService.confirmPayment(ticketIds);

            // Succes! Redăm sunet (opțional) și afișăm ecranul final
            setPaymentSuccess(true);
            new Audio('https://assets.mixkit.co/active_storage/sfx/2013/2013-preview.mp3').play().catch(() => {});
        } catch (err) {
            alert(err.response?.data?.message || "Eroare la confirmarea plății.");
            navigate('/');
        } finally {
            setIsProcessing(false);
        }
    };

    const formatTime = (s) => `${Math.floor(s / 60)}:${(s % 60).toString().padStart(2, '0')}`;

    if (!tickets) return null;

    // ECRAN SUCCES (Înlocuiește formularul după plată)
    if (paymentSuccess) {
        return (
            <div className="min-h-screen bg-white flex items-center justify-center p-6 animate-in fade-in zoom-in duration-500">
                <div className="text-center max-w-md">
                    <div className="w-24 h-24 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-8 shadow-lg shadow-green-100">
                        <Check size={48} strokeWidth={3} />
                    </div>
                    <h1 className="text-4xl font-black text-blue-950 uppercase italic mb-4 tracking-tighter">Plată Reușită!</h1>
                    <p className="text-slate-500 font-bold uppercase text-xs tracking-widest leading-loose mb-10">
                        Biletele tale au fost confirmate și trimise pe adresa de email. Ne vedem pe stadion! 🇷🇴
                    </p>
                    <button
                        onClick={() => navigate('/')}
                        className="w-full bg-blue-900 text-white py-5 rounded-2xl font-black uppercase tracking-[0.2em] shadow-xl hover:bg-red-600 transition-all"
                    >
                        Înapoi la meciuri
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-slate-50 p-6 md:p-12 font-sans text-slate-900">
            <div className="max-w-5xl mx-auto">
                <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-slate-400 hover:text-blue-900 mb-8 font-bold transition-all uppercase text-[10px] tracking-widest">
                    <ArrowLeft size={16} /> Modifică locurile
                </button>

                <div className="grid lg:grid-cols-3 gap-8">
                    {/* SUMAR COMANDĂ */}
                    <div className="lg:col-span-1 space-y-6">
                        <div className="bg-white p-6 rounded-3xl shadow-sm border border-slate-100">
                            <div className="flex items-center justify-between mb-6">
                                <h2 className="font-black uppercase italic text-blue-900">Rezumat</h2>
                                <div className={`flex items-center gap-1 text-xs font-black px-3 py-1 rounded-full ${timeLeft < 120 ? 'bg-red-100 text-red-600 animate-pulse' : 'bg-blue-50 text-blue-600'}`}>
                                    <Timer size={14} /> {formatTime(timeLeft)}
                                </div>
                            </div>
                            <div className="space-y-4">
                                {tickets.map((t) => (
                                    <div key={t.id} className="flex justify-between items-center text-sm border-b border-slate-50 pb-3">
                                        <div className="flex flex-col">
                                            <span className="font-bold text-slate-400 text-[9px] uppercase tracking-tighter">Bilet #{t.id}</span>
                                            <span className="font-black text-blue-950 uppercase italic">Loc {t.seatId}</span>
                                        </div>
                                        <span className="font-black text-blue-600">{t.finalPrice} RON</span>
                                    </div>
                                ))}
                                <div className="pt-4 flex justify-between items-end">
                                    <span className="font-bold text-slate-400 uppercase text-[10px]">Total de plată</span>
                                    <span className="text-3xl font-black text-blue-950 italic">{totalPrice} RON</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* FORMULAR PLATĂ */}
                    <div className="lg:col-span-2">
                        <div className="bg-white p-8 md:p-12 rounded-[3rem] shadow-xl border border-slate-100">
                            <div className="flex items-center justify-between mb-10">
                                <div className="flex items-center gap-4">
                                    <div className="bg-blue-950 p-4 rounded-2xl text-white">
                                        <CreditCard size={28} />
                                    </div>
                                    <div>
                                        <h1 className="text-2xl font-black text-blue-950 uppercase italic leading-none">Informații Card</h1>
                                        <p className="text-[10px] font-black text-slate-400 mt-2 uppercase tracking-widest flex items-center gap-2">
                                            <CheckCircle2 size={12} className="text-green-500" /> Tranzacție Securizată
                                        </p>
                                    </div>
                                </div>
                                <div className="flex gap-4 opacity-40">
                                    <img
                                        src="https://upload.wikimedia.org/wikipedia/commons/5/5c/Visa_Inc._logo_%282021%E2%80%93present%29.svg?utm_source=commons.wikimedia.org&utm_campaign=index&utm_content=original"
                                        alt="Visa"
                                        className={`h-4 transition-all ${cardType === 'visa' ? 'opacity-100 scale-110 grayscale-0' : ''}`}
                                    />
                                    <img
                                        src="https://upload.wikimedia.org/wikipedia/commons/2/2a/Mastercard-logo.svg"
                                        alt="Mastercard"
                                        className={`h-8 transition-all ${cardType === 'mastercard' ? 'opacity-100 scale-110 grayscale-0' : ''}`}
                                    />
                                </div>
                            </div>

                            <form onSubmit={startPaymentProcess} className="space-y-8">
                                <div className="space-y-6">
                                    <div className="grid md:grid-cols-2 gap-6">
                                        <div className="space-y-2">
                                            <label className="text-[10px] font-black uppercase text-slate-400 ml-2">Nume Deținător</label>
                                            <input required type="text" value={cardData.name} onChange={e => setCardData({...cardData, name: e.target.value.toUpperCase()})} placeholder="EX: ANDREI IONESCU" className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 focus:bg-white focus:border-blue-600 outline-none transition-all font-bold uppercase" />
                                        </div>
                                        <div className="space-y-2">
                                            <label className="text-[10px] font-black uppercase text-slate-400 ml-2">Număr Card</label>
                                            <input required type="text" value={cardData.number} onChange={handleCardNumberChange} placeholder="0000 0000 0000 0000" className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 focus:bg-white focus:border-blue-600 outline-none transition-all font-mono font-bold" />
                                        </div>
                                    </div>
                                    <div className="grid grid-cols-2 gap-6">
                                        <div className="space-y-2">
                                            <label className="text-[10px] font-black uppercase text-slate-400 ml-2">Expirație</label>
                                            <input required type="text" placeholder="MM / YY" maxLength={5} className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 focus:bg-white focus:border-blue-600 outline-none transition-all font-bold text-center" />
                                        </div>
                                        <div className="space-y-2">
                                            <label className="text-[10px] font-black uppercase text-slate-400 ml-2">CVV</label>
                                            <input required type="password" maxLength={3} className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 focus:bg-white focus:border-blue-600 outline-none transition-all font-bold text-center" />
                                        </div>
                                    </div>
                                </div>
                                <button
                                    disabled={isProcessing}
                                    className={`w-full py-6 rounded-[2rem] font-black uppercase text-sm tracking-[0.2em] shadow-2xl transition-all active:scale-[0.98] flex items-center justify-center gap-3
                                        ${isProcessing ? 'bg-slate-200 text-slate-400' : 'bg-blue-600 text-white hover:bg-red-600'}`}
                                >
                                    {isProcessing ? <Loader2 className="animate-spin" /> : <><Lock size={18} /> Plătește {totalPrice} RON</>}
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>

            {/* MODAL 3D SECURE (SIMULARE BANCĂ) */}
            {show3DS && (
                <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-blue-950/80 backdrop-blur-md animate-in fade-in duration-300">
                    <div className="bg-white w-full max-w-md rounded-[2.5rem] shadow-2xl overflow-hidden overflow-y-auto">
                        <div className="bg-slate-50 p-6 border-b border-slate-100 flex items-center justify-between">
                            <div className="flex items-center gap-2">
                                <ShieldAlert className="text-blue-600" size={20} />
                                <span className="font-black uppercase italic text-sm">Banca Ta - Securitate</span>
                            </div>
                            <X className="cursor-pointer text-slate-300" onClick={() => setShow3DS(false)} />
                        </div>
                        <div className="p-8 text-center">
                            <div className="bg-blue-50 w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-6">
                                <Smartphone className="text-blue-600" size={32} />
                            </div>
                            <h3 className="text-xl font-black text-blue-950 uppercase italic mb-2">Verificare 3D Secure</h3>
                            <p className="text-slate-500 text-xs font-bold uppercase tracking-wider mb-8 leading-relaxed">
                                Am trimis un cod de verificare la numărul de telefon asociat cardului tău.
                                <br/><span className="text-blue-600">Codul tău este: {generatedCode}</span>
                            </p>

                            <div className="space-y-4">
                                <input
                                    type="text"
                                    maxLength={4}
                                    value={smsCode}
                                    onChange={(e) => setSmsCode(e.target.value.toUpperCase())}
                                    placeholder="INTRODU CODUL"
                                    className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-5 text-center font-black tracking-[0.5em] text-2xl outline-none focus:border-blue-600 transition-all"
                                />
                                <button
                                    onClick={confirmSmsCode}
                                    disabled={isProcessing}
                                    className="w-full bg-blue-900 text-white py-5 rounded-2xl font-black uppercase tracking-widest hover:bg-blue-800 transition-all shadow-xl flex items-center justify-center gap-3"
                                >
                                    {isProcessing ? <Loader2 className="animate-spin" /> : "Confirmă Plată"}
                                </button>
                                <p className="text-[9px] font-black text-slate-300 uppercase tracking-[0.2em]">Tranzacție securizată de Arena Catalog Service</p>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CheckoutPage;5