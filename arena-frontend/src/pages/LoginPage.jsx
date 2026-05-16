import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; // Importăm hook-ul de Auth
import { Ticket, Lock, User, ArrowLeft, Loader2, AlertCircle } from 'lucide-react';

const LoginPage = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [rememberMe, setRememberMe] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const navigate = useNavigate();
    const { login } = useAuth(); // Extragem funcția de login din Context

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            // Apelăm funcția din Context, nu direct serviciul
            // Aceasta va rula authService.login și va seta user-ul global
            const p = await login(username, password, rememberMe);
            if(p.role === 'ADMIN') navigate('/admin/dashboard');
            else navigate('/');
        } catch (err) {
            console.log("Structura eroare primită:", err.response?.data);
            // Verificăm ierarhic unde se află mesajul tău
            const errorMessage =
                err.response?.data?.message || // Aici ar trebui să fie conform handleAuthException
                (err.response?.data?.errors ? err.response.data.errors[0] : null) || // Pentru validări
                "Eroare de autentificare!";
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-blue-900 flex items-center justify-center p-4 relative overflow-hidden font-sans">
            {/* Decoratiuni Tricolore */}
            <div className="absolute top-0 left-0 w-full h-2 bg-yellow-400"></div>
            <div className="absolute top-2 left-0 w-full h-2 bg-red-600"></div>

            <div className="max-w-md w-full bg-white rounded-3xl shadow-2xl overflow-hidden relative z-10 border border-white/20">
                <div className="p-8">
                    <Link to="/" className="inline-flex items-center text-sm font-bold text-blue-900 hover:text-blue-700 mb-8 transition-colors group">
                        <ArrowLeft className="w-4 h-4 mr-2 group-hover:-translate-x-1 transition-transform" />
                        Înapoi la site
                    </Link>

                    <div className="text-center mb-10">
                        <div className="inline-flex bg-red-50 p-4 rounded-2xl mb-4">
                            <Ticket className="w-10 h-10 text-red-600" />
                        </div>
                        <h2 className="text-3xl font-black text-blue-900 uppercase tracking-tight">Autentificare</h2>
                        <p className="text-slate-500 font-medium">Cont Suporter România</p>
                    </div>

                    {error && (
                        <div className="mb-6 p-4 bg-red-50 border-l-4 border-red-600 rounded-r-xl flex items-center gap-3 animate-in fade-in slide-in-from-top-1">
                            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0" />
                            <p className="text-red-800 text-sm font-bold">{error}</p>
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div>
                            <label className="block text-sm font-bold text-slate-700 mb-2 ml-1">Utilizator / Email</label>
                            <div className="relative">
                                <User className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                                <input
                                    type="text"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    disabled={loading}
                                    className="w-full bg-slate-50 border-2 border-slate-100 rounded-xl py-3.5 px-12 outline-none focus:border-yellow-400 focus:bg-white transition-all font-medium disabled:opacity-50"
                                    placeholder="Introdu username"
                                    required
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-bold text-slate-700 mb-2 ml-1">Parolă</label>
                            <div className="relative">
                                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                                <input
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    disabled={loading}
                                    className="w-full bg-slate-50 border-2 border-slate-100 rounded-xl py-3.5 px-12 outline-none focus:border-yellow-400 focus:bg-white transition-all font-medium disabled:opacity-50"
                                    placeholder="••••••••"
                                    required
                                />
                            </div>
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full bg-red-600 text-white py-4 rounded-xl font-black uppercase tracking-widest hover:bg-red-700 transition-all shadow-lg active:scale-[0.98] disabled:bg-slate-400 flex items-center justify-center gap-2"
                        >
                            {loading ? (
                                <>
                                    <Loader2 className="w-5 h-5 animate-spin" />
                                    Se verifică...
                                </>
                            ) : (
                                "Intră în cont"
                            )}
                        </button>
                    </form>
                </div>
                <div className="flex items-center justify-between mb-6 ml-1">
                    <label className="flex items-center gap-2 cursor-pointer group">
                        <input
                            type="checkbox"
                            checked={rememberMe}
                            onChange={(e) => setRememberMe(e.target.checked)}
                            className="w-4 h-4 rounded border-slate-300 text-red-600 focus:ring-red-500 cursor-pointer"
                        />
                        <span className="text-sm font-bold text-slate-600 group-hover:text-blue-900 transition-colors">
                            Ține-mă minte
                        </span>
                    </label>

                    <Link to="/forgot-password" disabled={loading} className="text-sm font-bold text-blue-700 hover:underline">
                        Ai uitat parola?
                    </Link>
                </div>
                <div className="bg-slate-50 p-6 text-center border-t border-slate-100">
                    <p className="text-sm text-slate-600 font-medium">
                        Nu ai cont? <Link to="/register" className="text-blue-700 font-bold hover:underline">Înregistrează-te</Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;