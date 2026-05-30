import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.jsx';
import { LayoutDashboard, Users, LogOut, ShieldCheck, Mail, Calendar } from 'lucide-react'; // Am adăugat Calendar

const AdminNavbar = () => {
    const { logout, profile } = useAuth();
    const location = useLocation();

    const menuItems = [
        { label: 'Dashboard', icon: LayoutDashboard, path: '/admin/dashboard' },
        { label: 'Evenimente', icon: Calendar, path: '/admin/matches' }, // Secțiunea nouă
        { label: 'Utilizatori', icon: Users, path: '/admin/users' },
        { label: 'Bilete', icon: Users, path: '/admin/tickets' },
        { label: 'Statistici', icon: Users, path: '/admin/analytics' }
    ];

    return (
        <nav className="fixed top-0 left-0 right-0 bg-slate-950 text-white z-[100] border-b border-slate-800">
            <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <div className="bg-red-600 p-1.5 rounded-lg shadow-lg shadow-red-600/20">
                        <ShieldCheck className="w-5 h-5 text-white" />
                    </div>
                    <span className="text-sm font-black uppercase italic tracking-tighter">
                        Arena <span className="text-red-600">Admin</span>
                    </span>
                </div>

                <div className="flex items-center gap-2">
                    {menuItems.map((item) => (
                        <Link
                            key={item.label}
                            to={item.path}
                            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all ${
                                location.pathname === item.path
                                    ? 'bg-white/10 text-white shadow-inner'
                                    : 'text-slate-400 hover:text-white hover:bg-white/5'
                            }`}
                        >
                            <item.icon size={14} />
                            {item.label}
                        </Link>
                    ))}
                </div>

                <div className="flex items-center gap-4 pl-6 border-l border-slate-800">
                    <div className="hidden sm:block text-right mr-2">
                        <p className="text-[10px] font-black uppercase leading-none">{profile?.username}</p>
                        <p className="text-[8px] font-bold text-slate-500 uppercase mt-1">Super Admin</p>
                    </div>
                    <button
                        onClick={logout}
                        className="p-2 hover:bg-red-600/20 text-slate-400 hover:text-red-500 rounded-xl transition-all border border-transparent hover:border-red-600/20"
                    >
                        <LogOut size={16} />
                    </button>
                </div>
            </div>
        </nav>
    );
};

export default AdminNavbar;