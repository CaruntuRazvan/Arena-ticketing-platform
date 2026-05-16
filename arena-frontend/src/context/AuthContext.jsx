import { createContext, useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const fetchProfile = async () => {
        try {
            const data = await authService.getProfile();
            setProfile(data);
            return data; // Returnăm datele pentru a le folosi imediat la login
        } catch (err) {
            console.error("Nu s-au putut încărca datele profilului");
            throw err;
        }
    };

    useEffect(() => {
        const initAuth = async () => {
            const storedUser = authService.getCurrentUser();
            const token = localStorage.getItem('token');

            if (storedUser && token) {
                setUser(storedUser);
                try {
                    await fetchProfile();
                } catch (e) {
                    localStorage.clear();
                    setUser(null);
                    setProfile(null);
                }
            }
            setLoading(false);
        };
        initAuth();
    }, []);

    const login = async (username, password, rememberMe) => {
        const data = await authService.login(username, password, rememberMe);
        setUser(data.username);
        // Așteptăm profilul pentru a avea acces la ROLE imediat după login
        const userProfile = await fetchProfile();
        return userProfile;
    };

    const logout = async () => {
        authService.logout();
        setUser(null);
        setProfile(null);
        navigate('/login');
    };

    // Verificare sigură bazată pe profilul venit de la backend
    const isAdmin = profile?.role === 'ADMIN';

    return (
        <AuthContext.Provider value={{ user, profile, login, logout, loading, isAdmin }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);