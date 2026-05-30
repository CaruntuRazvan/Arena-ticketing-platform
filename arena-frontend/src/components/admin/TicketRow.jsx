import React from 'react';
import { Ticket, Calendar, Armchair, Tag, QrCode } from 'lucide-react';

const TicketRow = ({ ticket }) => {
    // Formatare dată din "2026-05-21T21:45:00" -> "21.05.2026"
    const formattedDate = ticket.matchDate
        ? new Date(ticket.matchDate).toLocaleDateString('ro-RO', { day: '2-digit', month: '2-digit', year: 'numeric' })
        : 'N/A';

    return (
        <tr className="hover:bg-slate-50/80 transition-colors group">
            {/* ID Bilet + Segment din Codul Unic UUID */}
            <td className="p-6 text-center">
                <div className="flex flex-col items-center gap-1.5">
                    <span className="font-black text-xs text-blue-600 bg-blue-50 px-2.5 py-1.5 rounded-xl border border-blue-100">
                        #{ticket.id}
                    </span>
                    {ticket.ticketCode && (
                        <span className="text-[8px] font-mono font-bold text-slate-400 bg-slate-100 px-1.5 py-0.5 rounded uppercase tracking-wider" title={ticket.ticketCode}>
                            {ticket.ticketCode.substring(0, 8)}...
                        </span>
                    )}
                </div>
            </td>

            {/* Statusul tranzacției (Înlocuiește coloana veche de user care nu era în DTO) */}
            <td className="p-6 text-center">
                <span className={`inline-flex items-center gap-1 px-2.5 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider border-b-4 ${
                    ticket.status === 'CONFIRMED'
                        ? 'bg-emerald-50 text-emerald-600 border-emerald-200'
                        : 'bg-amber-50 text-amber-600 border-amber-200'
                }`}>
                    <QrCode size={10} />
                    {ticket.status || 'CONFIRMED'}
                </span>
            </td>

            {/* Eveniment / Meci */}
            <td className="p-6">
                <div>
                    <p className="font-black text-slate-900 text-sm uppercase italic leading-none flex items-center gap-1.5">
                        <Ticket size={14} className="text-blue-600 shrink-0" /> România vs {ticket.opponentName}
                    </p>
                    <p className="text-[10px] font-black text-slate-400 mt-2 uppercase tracking-tighter flex items-center gap-1">
                        <Calendar size={11} /> {formattedDate} — Arena Națională
                    </p>
                </div>
            </td>

            {/* Configurație Loc (Sector, Rând, Scaun) */}
            <td className="p-6 text-center">
                <div className="inline-flex items-center gap-2 bg-slate-950 text-white px-3 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider border-b-4 border-slate-700 shadow-sm">
                    <Armchair size={11} className="text-blue-400" />
                    <span>{ticket.sectorName}</span>
                    <span className="text-white/20">|</span>
                    <span>RÂND: {ticket.rowNumber}</span>
                    <span className="text-white/20">|</span>
                    <span>LOC: {ticket.seatNumber}</span>
                </div>
            </td>

            {/* Preț achitat mapat pe câmpul tău corect: finalPrice */}
            <td className="p-6 text-right">
                <div className="flex flex-col items-end">
                    <div className="flex items-center gap-1 font-black text-slate-900 text-sm italic">
                        <Tag size={13} className="text-emerald-500" />
                        {/* ✅ Folosește ticket.finalPrice din JSON-ul tău */}
                        <span>{ticket.finalPrice ? ticket.finalPrice.toFixed(2) : '0.00'}</span>
                        <span className="text-[10px] not-italic text-slate-400 font-bold ml-0.5">RON</span>
                    </div>
                    <span className="text-[7px] font-black text-emerald-600 bg-emerald-50 border border-emerald-100 px-1.5 py-0.5 rounded mt-1.5 uppercase tracking-widest">
                        VALIDAT
                    </span>
                </div>
            </td>
        </tr>
    );
};

export default TicketRow;