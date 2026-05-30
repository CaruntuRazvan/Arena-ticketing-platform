// components/ProtectedRoute.jsx
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children, allowedRoles }) => {
    const { profile, loading } = useAuth();

    if (loading) return null;

    if (!profile) return <Navigate to="/login" />;

    if (allowedRoles && !allowedRoles.includes(profile.role)) {
        // Dacă e admin și incearcă sa intre pe pagini de user (analog)
        return <Navigate to="/" />;
    }

    return children;
};

export default ProtectedRoute;