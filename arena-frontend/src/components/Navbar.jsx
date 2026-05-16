import { useState, useEffect } from 'react'; // Adăugăm hook-urile necesare
import { Ticket, User, LogOut, LayoutDashboard } from 'lucide-react';
import { Link, useLocation } from 'react-router-dom'; // Folosim useLocation pentru a face update când navigăm
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
    const { user, profile, logout } = useAuth();
    const location = useLocation();
    //const [navbarAvatar, setNavbarAvatar] = useState(localStorage.getItem('user_avatar') || null);
    const [navbarAvatar, setNavbarAvatar] = useState(null);

    useEffect(() => {
        const updateAvatar = () => {
            if (profile?.id) {
                const userSpecificKey = `user_avatar_${profile.id}`;
                setNavbarAvatar(localStorage.getItem(userSpecificKey));
            } else {
                setNavbarAvatar(null); // Reset la logout
            }
        };

        updateAvatar();
        window.addEventListener('avatarUpdated', updateAvatar);
        return () => window.removeEventListener('avatarUpdated', updateAvatar);
    }, [profile?.id]);

    return (
        <nav className="bg-white shadow-sm p-4 border-b-4 border-blue-700">
            <div className="max-w-6xl mx-auto flex justify-between items-center">
                <Link to="/" className="text-xl font-extrabold text-blue-900 tracking-tight flex items-center gap-2">
                    <Ticket className="w-6 h-6 text-red-600" />
                    ARENA <span className="text-yellow-500">TICKETING</span>
                </Link>

                <div className="flex items-center gap-6">
                    <div className="hidden md:flex space-x-6 text-sm font-bold text-slate-600">
                        <Link to="/matches" className="hover:text-blue-700 transition-colors">Meciuri</Link>
                        <Link to="/stadium" className="hover:text-blue-700 transition-colors">Stadion</Link>
                    </div>

                    {user ? (
                        <div className="flex items-center gap-3 md:gap-5">
                            {/* LINK NOU: BILETELE MELE (Buton rapid) */}
                            <Link
                                to="/my-tickets"
                                className={`flex items-center gap-2 px-3 py-2 rounded-xl transition-all ${
                                    location.pathname === '/my-tickets'
                                        ? 'bg-blue-50 text-blue-700 ring-1 ring-blue-200'
                                        : 'text-slate-500 hover:bg-slate-50 hover:text-blue-900'
                                }`}
                            >
                                <Ticket className="w-5 h-5 opacity-80" />
                                <span className="hidden sm:block text-xs font-black uppercase tracking-tight">Bilete</span>
                            </Link>

                            <div className="h-8 w-px bg-slate-200 hidden sm:block"></div>

                            {/* CONTUL MEU */}
                            <Link
                                to="/profile"
                                className="flex items-center gap-2 bg-slate-100 px-4 py-2 rounded-full border border-slate-200 shadow-sm hover:bg-blue-50 transition-colors group"
                            >
                                <div className="w-8 h-8 rounded-full border border-blue-200 overflow-hidden bg-blue-900 flex items-center justify-center shadow-inner transition-colors group-hover:bg-blue-700">
                                    {navbarAvatar ? (
                                        <img src={navbarAvatar} alt="Avatar" className="w-full h-full object-cover" />
                                    ) : (
                                        <User className="w-4 h-4 text-white" />
                                    )}
                                </div>
                                <div className="hidden lg:flex flex-col items-start">
                                    <span className="text-[10px] font-bold text-slate-500 uppercase leading-none">Contul meu</span>
                                    <span className="text-sm font-black text-blue-900 uppercase">
                                        {profile?.firstName || user}
                                    </span>
                                </div>
                            </Link>

                            <button
                                onClick={logout}
                                className="text-slate-400 hover:text-red-600 transition-colors p-2"
                                title="Logout"
                            >
                                <LogOut className="w-5 h-5" />
                            </button>
                        </div>
                    ) : (
                        <Link to="/login" className="bg-red-600 text-white px-6 py-2 rounded-full hover:bg-red-700 transition-all shadow-md font-bold uppercase text-xs tracking-widest active:scale-95">
                            Login
                        </Link>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;