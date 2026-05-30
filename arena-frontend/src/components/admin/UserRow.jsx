import React from 'react';
import { User as UserIcon, Mail, Shield, Award, Trash2 } from 'lucide-react';

const UserRow = ({ user, onDelete }) => {
    const handleDeleteClick = () => {
        if (window.confirm(`Sigur vrei să ștergi definitiv suporterul ${user.firstName} ${user.lastName}? Toate datele asociate vor fi șterse.`)) {
            onDelete(user.id);
        }
    };

    return (
        <tr className="hover:bg-slate-50/80 transition-colors group">
            <td className="p-6">
                <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-400 group-hover:bg-blue-600 group-hover:text-white transition-all duration-300 shadow-inner">
                        <UserIcon size={20} />
                    </div>
                    <div>
                        <p className="font-black text-slate-900 text-sm uppercase italic leading-none">
                            {user.firstName} {user.lastName}
                            <span className="text-blue-600 ml-2 text-[10px] lowercase font-bold not-italic opacity-0 group-hover:opacity-100 transition-opacity">
                                @{user.username}
                            </span>
                        </p>
                        <p className="text-slate-400 text-[11px] font-bold mt-1.5 flex items-center gap-1.5 uppercase tracking-tighter">
                            <Mail size={12} className="text-slate-300" /> {user.email}
                        </p>
                    </div>
                </div>
            </td>
            <td className="p-6 text-center">
                <span className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-widest border-b-4 ${
                    user.role === 'ADMIN'
                        ? 'bg-red-50 text-red-600 border-red-200'
                        : 'bg-emerald-50 text-emerald-600 border-emerald-200'
                }`}>
                    <Shield size={10} />
                    {user.role}
                </span>
            </td>
            <td className="p-6 text-center">
                <div className="flex flex-col items-center">
                    <div className="flex items-center gap-1.5 text-amber-500">
                        <Award size={14} />
                        <span className="font-black text-slate-900 text-sm italic">{user.loyaltyPoints || 0}</span>
                    </div>
                    <span className="text-[8px] font-black text-slate-300 uppercase mt-1">Puncte</span>
                </div>
            </td>
            <td className="p-6 text-right">
                {user.role !== 'ADMIN' ? (
                    <button
                        onClick={handleDeleteClick}
                        className="p-3 bg-slate-50 hover:bg-red-50 text-slate-400 hover:text-red-600 border border-slate-100 hover:border-red-200 rounded-2xl transition-all active:scale-95"
                        title="Șterge utilizator"
                    >
                        <Trash2 size={16} />
                    </button>
                ) : (
                    <span className="text-[9px] font-black uppercase text-slate-300 pr-2">Protejat</span>
                )}
            </td>
        </tr>
    );
};

export default UserRow;