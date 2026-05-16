// 1. IMPORTĂ useEffect aici:
import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { User, Mail, Phone, Award, Check, Settings2, X } from 'lucide-react';

const ProfilePage = () => {
    const { profile, loading } = useAuth();
    const [avatar, setAvatar] = useState(null);
    const [isSelecting, setIsSelecting] = useState(false);

    // Acest useEffect va funcționa acum pentru că l-am importat sus
    useEffect(() => {
        if (profile?.id) {
            const userSpecificKey = `user_avatar_${profile.id}`;
            const savedAvatar = localStorage.getItem(userSpecificKey);
            setAvatar(savedAvatar);
        }
    }, [profile?.id]);

    const presetAvatars = [
        'https://api.dicebear.com/7.x/personas/svg?seed=Fan1&backgroundColor=008000',
        'https://api.dicebear.com/7.x/adventurer/svg?seed=UltraFan&backgroundColor=ff0000',
        'https://api.dicebear.com/7.x/bottts/svg?seed=TechFan&backgroundColor=0000ff',
        'https://api.dicebear.com/7.x/personas/svg?seed=GoalMachine&backgroundColor=facc15',
        'https://api.dicebear.com/7.x/adventurer/svg?seed=Captain&backgroundColor=1e3a8a',
        'https://api.dicebear.com/7.x/bottts/svg?seed=UltraDefense&backgroundColor=111827'
    ];

    const handleSelectAvatar = (url) => {
        if (!profile?.id) return;

        const userSpecificKey = `user_avatar_${profile.id}`;
        setAvatar(url);
        localStorage.setItem(userSpecificKey, url);

        window.dispatchEvent(new Event('avatarUpdated'));
        setIsSelecting(false);
    };

    // Verifică consola browserului (F12) dacă tot nu se încarcă.
    // Dacă profile este null, va râmâne în loading.
    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center font-bold text-blue-900">
                Se încarcă profilul...
            </div>
        );
    }

    return (
        <main className="max-w-4xl mx-auto mt-10 p-6 relative">
            {/* Restul codului tău rămâne neschimbat, e corect scris */}
            <div className="grid md:grid-cols-3 gap-8">
                <div className="md:col-span-1 space-y-6">
                    <div className="bg-white rounded-3xl p-6 shadow-sm border border-slate-200 flex flex-col items-center">
                        <button
                            onClick={() => setIsSelecting(true)}
                            className="relative group transition-transform hover:scale-105 active:scale-95"
                        >
                            <div className="w-32 h-32 rounded-full border-4 border-blue-50 overflow-hidden bg-slate-100 flex items-center justify-center shadow-inner mb-4 transition-all group-hover:border-blue-400">
                                {avatar ? (
                                    <img src={avatar} alt="Profile" className="w-full h-full object-cover" />
                                ) : (
                                    <User className="w-16 h-16 text-slate-300" />
                                )}
                            </div>
                            <div className="absolute bottom-4 right-2 bg-blue-600 p-2 rounded-full border-2 border-white shadow-lg text-white">
                                <Settings2 className="w-4 h-4" />
                            </div>
                        </button>

                        <h2 className="font-black text-blue-900 text-xl mb-1 text-center">
                            {profile?.firstName || 'Suporter'}
                        </h2>
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Apasă pe foto pentru editare</p>
                    </div>

                    <div className="bg-blue-900 rounded-3xl p-8 text-center text-white shadow-xl relative overflow-hidden">
                        <Award className="w-16 h-16 text-yellow-400 mx-auto mb-4" />
                        <h3 className="text-lg font-bold opacity-80 uppercase tracking-widest text-xs">Puncte Loialitate</h3>
                        <div className="text-6xl font-black my-2">{profile?.loyaltyPoints || 0}</div>
                        <p className="text-xs font-medium text-blue-200 uppercase">Suporter Rang: <span className="text-yellow-400">Veteran</span></p>
                    </div>
                </div>

                <div className="md:col-span-2 bg-white rounded-3xl shadow-sm border border-slate-200 p-8 self-start">
                    <h2 className="text-2xl font-black text-blue-900 mb-8 flex items-center gap-3">
                        <User className="w-6 h-6 text-red-600" />
                        Informații Profil
                    </h2>
                    <div className="grid md:grid-cols-2 gap-8 text-slate-800 font-bold">
                        <div>
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest block mb-1">Nume Complet</label>
                            {profile?.firstName} {profile?.lastName}
                        </div>
                        <div>
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest block mb-1">Username</label>
                            @{profile?.username}
                        </div>
                        <div>
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest block mb-1">Email Oficial</label>
                            <div className="flex items-center gap-2 font-bold"><Mail className="w-4 h-4 text-blue-600" />{profile?.email}</div>
                        </div>
                        <div>
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest block mb-1">Telefon</label>
                            <div className="flex items-center gap-2 font-bold"><Phone className="w-4 h-4 text-green-600" />{profile?.phoneNumber || 'Nespecificat'}</div>
                        </div>
                    </div>
                </div>
            </div>

            {isSelecting && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                    <div
                        className="absolute inset-0 bg-blue-950/40 backdrop-blur-sm animate-in fade-in duration-300"
                        onClick={() => setIsSelecting(false)}
                    ></div>

                    <div className="relative bg-white rounded-[2.5rem] p-8 w-full max-w-md shadow-2xl animate-in zoom-in-95 duration-200 border border-slate-100">
                        <button
                            onClick={() => setIsSelecting(false)}
                            className="absolute top-6 right-6 p-2 hover:bg-slate-100 rounded-full transition-colors"
                        >
                            <X className="w-6 h-6 text-slate-400" />
                        </button>

                        <h3 className="text-2xl font-black text-blue-900 mb-2">Alege-ți stilul</h3>
                        <p className="text-slate-500 font-medium mb-8">Personalizează-ți apariția pe platformă.</p>

                        <div className="grid grid-cols-3 gap-4">
                            {presetAvatars.map((url, index) => (
                                <button
                                    key={index}
                                    onClick={() => handleSelectAvatar(url)}
                                    className={`relative aspect-square rounded-3xl overflow-hidden border-4 transition-all hover:scale-105 active:scale-95 ${
                                        avatar === url ? 'border-blue-600 shadow-lg shadow-blue-200' : 'border-slate-50 hover:border-slate-200'
                                    }`}
                                >
                                    <img src={url} alt={`Avatar ${index}`} className="w-full h-full object-cover" />
                                    {avatar === url && (
                                        <div className="absolute inset-0 bg-blue-600/10 flex items-center justify-center">
                                            <div className="bg-white rounded-full p-1 shadow-md">
                                                <Check className="w-4 h-4 text-blue-600 stroke-[3px]" />
                                            </div>
                                        </div>
                                    )}
                                </button>
                            ))}
                        </div>

                        <button
                            onClick={() => setIsSelecting(false)}
                            className="w-full mt-10 py-4 bg-slate-900 text-white rounded-2xl font-bold hover:bg-blue-900 transition-colors uppercase tracking-widest text-xs"
                        >
                            Închide
                        </button>
                    </div>
                </div>
            )}
        </main>
    );
};

export default ProfilePage;