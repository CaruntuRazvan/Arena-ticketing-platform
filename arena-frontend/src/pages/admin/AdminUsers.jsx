import React, { useState, useEffect } from 'react';
import adminUsersService from '../../services/adminUsersService.js';
import UserRow from '../../components/admin/UserRow.jsx';
import Pagination from '../../components/admin/Pagination.jsx';
import { Users, Search, UserSearch, X } from 'lucide-react';

const AdminUsers = () => {
    const [usersPage, setUsersPage] = useState(null);
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(0);
    const [searchTerm, setSearchTerm] = useState("");

    useEffect(() => {
        fetchUsers();
    }, [currentPage]);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const data = await adminUsersService.getAllUsers(currentPage, 20);
            setUsersPage(data);
        } catch (err) {
            console.error("Eroare la încărcarea utilizatorilor:", err);
        } finally {
            setLoading(false);
        }
    };

    // Apelul către backend pentru ștergerea în cascadă
    const handleDeleteUser = async (userId) => {
        try {
            await adminUsersService.deleteUser(userId); // Asigură-te că metoda există în adminUsersService.js
            alert("Suporter șters cu succes!");
            fetchUsers(); // Refresh la pagină după ștergere
        } catch (err) {
            console.error("Eroare la ștergerea utilizatorului:", err);
            alert("Nu s-a putut șterge utilizatorul.");
        }
    };

    // Filtrare client-side permisivă
    const users = usersPage?.content || [];
    const filteredUsers = users.filter(user => {
        const fullSearchString = `${user.firstName} ${user.lastName} ${user.username} ${user.email}`.toLowerCase();
        return fullSearchString.includes(searchTerm.toLowerCase());
    });

    if (loading) return (
        <div className="min-h-screen flex items-center justify-center bg-slate-50">
            <div className="flex flex-col items-center gap-4">
                <div className="w-12 h-12 border-4 border-slate-200 border-t-blue-600 rounded-full animate-spin"></div>
                <p className="text-[10px] font-black uppercase text-slate-400 tracking-[0.3em]">Accesare Bază Date...</p>
            </div>
        </div>
    );

    return (
        <div className="min-h-screen bg-slate-50 p-8 pt-24 font-sans text-slate-900">
            <div className="max-w-7xl mx-auto">

                {/* Header & Search Bar */}
                <div className="flex flex-col md:flex-row justify-between items-start md:items-end gap-6 mb-10">
                    <div>
                        <div className="flex items-center gap-3 mb-2">
                            <div className="bg-slate-900 p-2 rounded-xl">
                                <Users className="w-6 h-6 text-white" />
                            </div>
                            <h1 className="text-3xl font-black text-slate-900 uppercase italic tracking-tighter leading-none">
                                Management <span className="text-blue-600">Suporteri</span>
                            </h1>
                        </div>
                        <p className="text-slate-400 font-bold text-[10px] uppercase tracking-widest ml-1">
                            Administrare conturi și puncte de loialitate
                        </p>
                    </div>

                    {/* Search Input */}
                    <div className="relative w-full md:w-96 group">
                        <div className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-blue-600 transition-colors">
                            <Search className="w-5 h-5" />
                        </div>
                        <input
                            type="text"
                            placeholder="Caută după nume, email sau username..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full bg-white border-2 border-slate-100 rounded-2xl py-4 pl-12 pr-4 outline-none focus:border-blue-600 focus:ring-4 focus:ring-blue-600/5 shadow-sm text-sm font-bold transition-all"
                        />
                        {searchTerm && (
                            <button
                                onClick={() => setSearchTerm("")}
                                className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-300 hover:text-slate-500"
                            >
                                <X size={16} />
                            </button>
                        )}
                    </div>
                </div>

                {/* Tabelul și Paginarea */}
                <div className="bg-white rounded-[2.5rem] border border-slate-100 shadow-2xl overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="bg-slate-950 text-white">
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest border-r border-white/5">Suporter</th>
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest text-center border-r border-white/5">Rol</th>
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest text-center border-r border-white/5">Loialitate</th>
                                <th className="p-6 text-[10px] font-black uppercase tracking-widest text-right">Acțiuni</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-50">
                            {filteredUsers.length > 0 ? (
                                filteredUsers.map((user) => (
                                    <UserRow
                                        key={user.id}
                                        user={user}
                                        onDelete={handleDeleteUser}
                                    />
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="4" className="p-20 text-center">
                                        <div className="flex flex-col items-center opacity-20">
                                            <UserSearch size={48} className="mb-4" />
                                            <p className="font-black uppercase italic text-xl">Niciun suporter găsit</p>
                                            <p className="text-xs font-bold uppercase tracking-widest mt-2">Încearcă alt termen de căutare</p>
                                        </div>
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>

                    {/* Sub-componenta de paginare */}
                    <Pagination
                        currentPage={currentPage}
                        totalPages={usersPage?.totalPages}
                        totalElements={usersPage?.totalElements}
                        onPageChange={setCurrentPage}
                    />
                </div>
            </div>
        </div>
    );
};

export default AdminUsers;