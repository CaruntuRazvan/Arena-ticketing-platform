import { Routes, Route, useLocation, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

// Componente Navigație
import Navbar from './components/Navbar';
import AdminNavbar from './components/admin/AdminNavbar.jsx'; // Va trebui creată
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import StadiumPage from "./pages/StadiumPage";
import MatchesPage from './pages/MatchesPage';
import MatchDetailsPage from './pages/MatchDetailsPage';
import CheckoutPage from "./pages/CheckoutPage";
import MyTicketsPage from "./pages/MyTicketsPage";
import ProfilePage from './pages/ProfilePage';

// Pagini Admin
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminUsers from "./pages/admin/AdminUsers.jsx"; // Va trebui creată
import AdminMatches from "./pages/admin/AdminMatches";
import AdminTickets from "./pages/admin/AdminTickets.jsx";
import AdminAnalytics from "./pages/admin/AdminAnalytics.jsx";

function App() {
    const { isAdmin, loading, profile } = useAuth();
    const location = useLocation();

    // 1. Loader în timp ce verificăm sesiunea (foarte important)
    if (loading) {
        return (
            <div className="min-h-screen bg-slate-50 flex items-center justify-center">
                <div className="w-10 h-10 border-4 border-slate-200 border-t-blue-900 rounded-full animate-spin"></div>
            </div>
        );
    }

    const hideNavbarPaths = ['/login', '/register'];
    const showNavbar = !hideNavbarPaths.includes(location.pathname);

    return (
        <>
            {/* Navigație inteligentă: Admin vede AdminNavbar, User vede Navbar */}
            {showNavbar && (
                isAdmin ? <AdminNavbar /> : <Navbar />
            )}

            <Routes>
                {/* --- RUTE PUBLICE (Accesibile oricui) --- */}
                <Route path="/" element={<HomePage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/stadium" element={<StadiumPage />} />

                {/* --- RUTE USER (Doar pentru rolul USER) --- */}
                <Route path="/matches" element={
                    <ProtectedRoute allowedRoles={['USER']}><MatchesPage /></ProtectedRoute>
                } />
                <Route path="/match/:id" element={
                    <ProtectedRoute allowedRoles={['USER']}><MatchDetailsPage /></ProtectedRoute>
                } />
                <Route path="/checkout" element={
                    <ProtectedRoute allowedRoles={['USER']}><CheckoutPage /></ProtectedRoute>
                } />
                <Route path="/my-tickets" element={
                    <ProtectedRoute allowedRoles={['USER']}><MyTicketsPage /></ProtectedRoute>
                } />
                <Route path="/my-tickets/:pageNumber" element={
                    <ProtectedRoute allowedRoles={['USER']}><MyTicketsPage /></ProtectedRoute>
                } />
                <Route path="/profile" element={
                    <ProtectedRoute allowedRoles={['USER']}><ProfilePage /></ProtectedRoute>
                } />

                {/* --- RUTE ADMIN (Doar pentru rolul ADMIN) --- */}
                <Route path="/admin/dashboard" element={
                    <ProtectedRoute allowedRoles={['ADMIN']}><AdminDashboard /></ProtectedRoute>
                } />
                <Route path="/admin/users" element={
                    <ProtectedRoute allowedRoles={['ADMIN']}><AdminUsers /></ProtectedRoute>
                } />
                <Route path="/admin/matches" element={
                    <ProtectedRoute allowedRoles={['ADMIN']}><AdminMatches /></ProtectedRoute>
                } />
                <Route path="/admin/tickets" element={
                    <ProtectedRoute allowedRoles={['ADMIN']}><AdminTickets /></ProtectedRoute>
                } />
                <Route path="/admin/analytics" element={
                    <ProtectedRoute allowedRoles={['ADMIN']}><AdminAnalytics /></ProtectedRoute>
                } />
                {/* Aici vei adăuga rutele de gestiune meciuri, useri, etc. */}
                {/* <Route path="/admin/matches" element={<ProtectedRoute allowedRoles={['ADMIN']}><AdminMatches /></ProtectedRoute>} /> */}

                {/* --- REDIRECT DE SIGURANȚĂ --- */}
                {/* Dacă ești Admin și intri pe o rută greșită, te duce la Dashboard-ul tău */}
                {/* Dacă ești User, te duce la Home */}
                <Route path="*" element={
                    <Navigate to={isAdmin ? "/admin/dashboard" : "/"} replace />
                } />
            </Routes>
        </>
    );
}

export default App;