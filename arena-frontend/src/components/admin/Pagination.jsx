import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const Pagination = ({ currentPage, totalPages, totalElements, onPageChange }) => {
    return (
        <div className="p-8 bg-slate-50/50 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-4">
                <span className="text-[10px] font-black text-slate-400 uppercase tracking-[0.2em]">
                    Pagina {currentPage + 1} din {totalPages || 1}
                </span>
                <div className="h-4 w-[1px] bg-slate-200"></div>
                <span className="text-[10px] font-black text-blue-600 uppercase tracking-[0.2em]">
                    {totalElements || 0} Total Rezultate
                </span>
            </div>

            <div className="flex gap-3">
                <button
                    onClick={() => onPageChange(Math.max(0, currentPage - 1))}
                    disabled={currentPage === 0}
                    className="flex items-center gap-2 px-5 py-2.5 bg-white border-2 border-slate-100 rounded-2xl font-black text-[10px] uppercase tracking-widest hover:border-blue-600 hover:text-blue-600 disabled:opacity-30 disabled:hover:border-slate-100 disabled:hover:text-slate-400 transition-all shadow-sm"
                >
                    <ChevronLeft size={16} /> Înapoi
                </button>
                <button
                    onClick={() => onPageChange(Math.min((totalPages || 1) - 1, currentPage + 1))}
                    disabled={currentPage === (totalPages || 1) - 1}
                    className="flex items-center gap-2 px-5 py-2.5 bg-white border-2 border-slate-100 rounded-2xl font-black text-[10px] uppercase tracking-widest hover:border-blue-600 hover:text-blue-600 disabled:opacity-30 disabled:hover:border-slate-100 disabled:hover:text-slate-400 transition-all shadow-sm"
                >
                    Înainte <ChevronRight size={16} />
                </button>
            </div>
        </div>
    );
};

export default Pagination;