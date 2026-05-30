import React from 'react';
import { Calendar, MapPin, CheckCircle2, Trash2, Globe, Edit } from 'lucide-react';

const MatchRow = ({ match, onDelete, onPublish, onStatusChange, onEdit }) => {

    const handleDeleteClick = () => {
        if (window.confirm(`Sigur vrei să ștergi meciul România vs ${match.opponentName}? Toate prețurile sectoarelor asociate vor fi șterse definitiv.`)) {
            onDelete(match.id);
        }
    };

    const handlePublishClick = () => {
        if (window.confirm(`Vrei să publici meciul cu ${match.opponentName}? Această acțiune va trimite notificări către fani.`)) {
            onPublish(match.id);
        }
    };

    return (
        <tr className="hover:bg-slate-50/80 transition-colors group">
            <td className="p-6 text-center">
                <span className="text-[10px] font-black text-slate-300">#{match.id}</span>
            </td>

            <td className="p-6">
                <div className="flex items-center gap-4">
                    <div className="w-16 h-12 rounded-xl bg-slate-100 overflow-hidden border border-slate-200">
                        <img src={match.matchImageUrl || "/images/placeholder.jpg"} className="w-full h-full object-cover" alt="Opponent" />
                    </div>
                    <div>
                        <h4 className="font-black text-slate-900 text-sm uppercase italic">România vs {match.opponentName}</h4>
                        <div className="flex items-center gap-3 mt-1 text-slate-400">
                            <span className="text-[9px] font-bold uppercase flex items-center gap-1">
                                <Calendar size={12}/> {new Date(match.matchDate).toLocaleDateString('ro-RO')}
                            </span>
                            <span className="text-[9px] font-bold uppercase flex items-center gap-1">
                                <MapPin size={12}/> {match.stadiumName}
                            </span>
                        </div>
                    </div>
                </div>
            </td>

            {/* STATUS DROPDOWN INTERACTIV */}
            <td className="p-6 text-center">
                <div className="relative inline-block">
                    <select
                        value={match.status}
                        disabled={match.status === 'CANCELLED'} // Constrângere din backend: nu schimbăm dacă e anulat
                        onChange={(e) => onStatusChange(match.id, e.target.value)}
                        className={`appearance-none px-3 py-1.5 pr-6 rounded-lg text-[9px] font-black uppercase border-b-4 outline-none cursor-pointer transition-all ${
                            match.status === 'SCHEDULED' ? 'bg-blue-50 text-blue-600 border-blue-200 focus:ring-blue-500/10' :
                                match.status === 'FINISHED' ? 'bg-slate-100 text-slate-600 border-slate-300 focus:ring-slate-500/10' :
                                    'bg-red-50 text-red-600 border-red-200 cursor-not-allowed'
                        }`}
                    >
                        <option value="SCHEDULED">Scheduled</option>
                        <option value="FINISHED">Finished</option>
                        <option value="CANCELLED">Cancelled</option>
                    </select>
                    {match.status !== 'CANCELLED' && (
                        <span className="absolute right-2 top-1/2 -translate-y-1/2 text-[7px] pointer-events-none text-slate-400">▼</span>
                    )}
                </div>
            </td>

            {/* REALE ACTION PENTRU PUBLICARE */}
            <td className="p-6 text-center">
                {match.published ? (
                    <div className="flex items-center justify-center gap-1 text-emerald-500 mx-auto bg-emerald-50 border-b-2 border-emerald-200 py-1 px-2.5 rounded-lg w-max">
                        <CheckCircle2 size={14}/>
                        <span className="text-[8px] font-black uppercase tracking-wider">Publicat</span>
                    </div>
                ) : (
                    <button
                        onClick={handlePublishClick}
                        className="mx-auto flex items-center gap-1.5 px-3 py-1 bg-amber-500 text-white rounded-lg text-[8px] font-black uppercase tracking-wider shadow-md hover:bg-slate-900 transition-all active:scale-95"
                        title="Meciul e în draft. Click pentru publicare."
                    >
                        <Globe size={10} /> Publică
                    </button>
                )}
            </td>

            {/* ZONA DE ACȚIUNI GRUPATĂ */}
            <td className="p-6 text-right">
                <div className="flex justify-end gap-2">
                    {/* BUTON EDITARE */}
                    <button
                        onClick={() => onEdit(match)}
                        className="p-3 bg-slate-50 hover:bg-blue-50 text-slate-400 hover:text-blue-600 border border-slate-100 hover:border-blue-200 rounded-2xl transition-all active:scale-95"
                        title="Editează detalii și prețuri sectoare"
                    >
                        <Edit size={16} />
                    </button>

                    {/* BUTON ȘTERGERE */}
                    <button
                        onClick={handleDeleteClick}
                        className="p-3 bg-slate-50 hover:bg-red-50 text-slate-400 hover:text-red-600 border border-slate-100 hover:border-red-200 rounded-2xl transition-all active:scale-95"
                        title="Șterge meci definitiv"
                    >
                        <Trash2 size={16} />
                    </button>
                </div>
            </td>
        </tr>
    );
};

export default MatchRow;