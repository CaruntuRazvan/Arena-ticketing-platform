import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';
import { Ticket, Lock, User, Mail, Phone, ArrowLeft, Loader2, AlertCircle, CheckCircle2 } from 'lucide-react';

const RegisterPage = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [isSubmittingCode, setIsSubmittingCode] = useState(false);

    // Starea pentru a ști dacă afișăm formularul sau verificarea codului
    const [step, setStep] = useState(1); // 1 = Register, 2 = Verify OTP

    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        phoneNumber: ''
    });

    const [verificationCode, setVerificationCode] = useState('');

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await authService.register(formData);
            setStep(2); // Trecem la pasul de verificare
        } catch (err) {
            setError(err.response?.data?.message || "Eroare la înregistrare. Verifică datele.");
        } finally {
            setLoading(false);
        }
    };

    const handleVerify = async (e) => {
        e.preventDefault();
        setIsSubmittingCode(true);
        setError('');
        try {
            await authService.verifyAccount(formData.email, verificationCode);
            // Dacă verificarea a reușit, trimitem user-ul la login
            alert("Cont activat! Acum te poți loga.");
            navigate('/login');
        } catch (err) {
            setError(err.response?.data?.message || "Cod invalid sau expirat.");
        } finally {
            setIsSubmittingCode(false);
        }
    };

    return (
        <div className="min-h-screen bg-blue-900 flex items-center justify-center p-4 relative overflow-hidden">
            <div className="absolute top-0 left-0 w-full h-2 bg-yellow-400"></div>
            <div className="absolute top-2 left-0 w-full h-2 bg-red-600"></div>

            <div className="max-w-xl w-full bg-white rounded-3xl shadow-2xl overflow-hidden relative z-10">
                <div className="p-8">
                    <Link to="/login" className="inline-flex items-center text-sm font-bold text-blue-900 hover:text-blue-700 mb-6 transition-colors group">
                        <ArrowLeft className="w-4 h-4 mr-2 group-hover:-translate-x-1 transition-transform" />
                        Înapoi la login
                    </Link>

                    <div className="text-center mb-8">
                        <h2 className="text-3xl font-black text-blue-900 uppercase tracking-tight">
                            {step === 1 ? "Cont Nou Suporter" : "Verificare Email"}
                        </h2>
                        <p className="text-slate-500 font-medium">
                            {step === 1 ? "Alătură-te echipei naționale" : `Am trimis un cod la ${formData.email}`}
                        </p>
                    </div>

                    {error && (
                        <div className="mb-6 p-4 bg-red-50 border-l-4 border-red-600 rounded-r-xl flex items-center gap-3">
                            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0" />
                            <p className="text-red-800 text-sm font-bold">{error}</p>
                        </div>
                    )}

                    {step === 1 ? (
                        /* FORMULAR INREGISTRARE */
                        <form onSubmit={handleRegister} className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="md:col-span-1">
                                <label className="block text-xs font-bold text-slate-700 mb-1 ml-1 uppercase">Prenume</label>
                                <input name="firstName" onChange={handleChange} required className="w-full bg-slate-50 border-2 border-slate-100 rounded-xl py-3 px-4 outline-none focus:border-yellow-400" />
                            </div>
                            <div className="md:col-span-1">
                                <label className="block text-xs font-bold text-slate-700 mb-1 ml-1 uppercase">Nume</label>
                                <input name="lastName" onChange={handleChange} required className="w-full bg-slate-50 border-2 border-slate-100 rounded-xl py-3 px-4 outline-none focus:border-yellow-400" />
                            </div>
                            <div className="md:col-span-2">
                                <label className="block text-xs font-bold text-slate-700 mb-1 ml-1 uppercase">Username</label>
                                <div className="relative">
                                    <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                                    <input name="username" onChange={handleChange} required className="w-full bg-slate-50 border-2 border-slate-100 rounded-xl py-3 px-10 outline-none focus:border-yellow-400" />
                                </div>
                            </div>
                            <div className="md:col-span-2">
                                <label className="block text-xs font-bold text-slate-700 mb-1 ml-1 uppercase">Email</label>
                                <div className="relative">
                                    <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                                    <input type="email" name="email" onChange={handleChange} required className="w-full bg-slate-50 border-2 border-slate-100 rounded-xl py-3 px-10 outline-none focus:border-yellow-400" />
                                </div>
                            </div>
                            <div className="md:col-span-2">
                                <label className="block text-xs font-bold text-slate-700 mb-1 ml-1 uppercase">Telefon</label>
                                <div className="relative">
                                    <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                                    <input name="phoneNumber" onChange={handleChange} required className="w-full bg-slate-50 border-2 border-slate-100 rounded-xl py-3 px-10 outline-none focus:border-yellow-400" />
                                </div>
                            </div>
                            <div className="md:col-span-2">
                                <label className="block text-xs font-bold text-slate-700 mb-1 ml-1 uppercase">Parolă</label>
                                <div className="relative">
                                    <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                                    <input type="password" name="password" onChange={handleChange} required className="w-full bg-slate-50 border-2 border-slate-100 rounded-xl py-3 px-10 outline-none focus:border-yellow-400" />
                                </div>
                            </div>

                            <button type="submit" disabled={loading} className="md:col-span-2 w-full bg-red-600 text-white py-4 rounded-xl font-black uppercase tracking-widest hover:bg-red-700 transition-all shadow-lg flex items-center justify-center gap-2 mt-2">
                                {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : "Creează Contul"}
                            </button>
                        </form>
                    ) : (
                        /* FORMULAR VERIFICARE COD */
                        <form onSubmit={handleVerify} className="space-y-6 animate-in fade-in zoom-in-95">
                            <div className="bg-blue-50 p-6 rounded-2xl border border-blue-100 text-center">
                                <p className="text-sm text-blue-900 font-bold mb-4 uppercase tracking-wide">Introdu codul de 6 cifre</p>
                                <input
                                    type="text"
                                    maxLength="6"
                                    value={verificationCode}
                                    onChange={(e) => setVerificationCode(e.target.value)}
                                    className="w-full text-center text-4xl font-black tracking-[1rem] bg-white border-2 border-blue-200 rounded-xl py-4 outline-none focus:border-yellow-400 text-blue-900"
                                    placeholder="000000"
                                    required
                                />
                            </div>
                            <button type="submit" disabled={isSubmittingCode} className="w-full bg-green-600 text-white py-4 rounded-xl font-black uppercase tracking-widest hover:bg-green-700 transition-all shadow-lg flex items-center justify-center gap-2">
                                {isSubmittingCode ? <Loader2 className="w-5 h-5 animate-spin" /> : "Activează Contul"}
                            </button>
                            <button
                                type="button"
                                onClick={() => authService.resendCode(formData.email)}
                                className="w-full text-sm font-bold text-slate-500 hover:text-blue-900 transition-colors"
                            >
                                Nu ai primit codul? Trimite din nou
                            </button>
                        </form>
                    )}
                </div>
            </div>
        </div>
    );
};

export default RegisterPage;