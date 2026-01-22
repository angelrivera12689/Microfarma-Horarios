import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import DashboardLayout from './components/DashboardLayout';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ForgotPassword from './pages/auth/ForgotPassword';
import UserLanding from './pages/UserLanding';
import MySchedule from './pages/MySchedule';
import ProfileEdit from './pages/ProfileEdit';
import Notifications from './pages/Notifications';
import DashboardHome from './pages/DashboardHome';
import Users from './pages/security/Users';
import Roles from './pages/security/Roles';
import Permissions from './pages/security/Permissions';
import Employees from './pages/hr/Employees';
import Positions from './pages/hr/Positions';
import Contracts from './pages/hr/Contracts';
import Companies from './pages/org/Companies';
import Locations from './pages/org/Locations';
import ShiftTypes from './pages/schedules/ShiftTypes';
import Shifts from './pages/schedules/Shifts';
import Reports from './pages/schedules/Reports';
import News from './pages/news/News';
import NewsTypes from './pages/news/NewsTypes';
import Email from './pages/notifications/Email';
import authService from './services/authService';

// Protected Route Component
const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = () => {
      const authenticated = authService.isAuthenticated();
      setIsAuthenticated(authenticated);
      setLoading(false);
    };

    checkAuth();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requireAdmin) {
    const user = authService.getCurrentUser();
    if (!user || user.role !== 'ADMIN') {
      return <Navigate to="/user-landing" replace />;
    }
  }

  return children;
};

// Public Route Component (redirects to appropriate dashboard if already authenticated)
const PublicRoute = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = () => {
      const authenticated = authService.isAuthenticated();
      setIsAuthenticated(authenticated);
      setLoading(false);
    };

    checkAuth();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500"></div>
      </div>
    );
  }

  if (isAuthenticated) {
    const user = authService.getCurrentUser();
    return user && user.role === 'ADMIN' ? <Navigate to="/dashboard" replace /> : <Navigate to="/user-landing" replace />;
  }

  return children;
};

// Auth Redirect Component (for root path)
const AuthRedirect = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = () => {
      const authenticated = authService.isAuthenticated();
      setIsAuthenticated(authenticated);
      setLoading(false);
    };

    checkAuth();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500"></div>
      </div>
    );
  }

  if (isAuthenticated) {
    const user = authService.getCurrentUser();
    return user && user.role === 'ADMIN' ? <Navigate to="/dashboard" replace /> : <Navigate to="/user-landing" replace />;
  }

  return <Navigate to="/login" replace />;
};

function App() {
  return (
    <Router>
      <Routes>
        {/* Root redirect based on authentication */}
        <Route path="/" element={<AuthRedirect />} />

        {/* Public Routes */}
        <Route path="/login" element={<Login />} />
        <Route
          path="/register"
          element={
            <PublicRoute>
              <Register />
            </PublicRoute>
          }
        />
        <Route
          path="/forgot-password"
          element={
            <PublicRoute>
              <ForgotPassword />
            </PublicRoute>
          }
        />

        {/* User Routes */}
        <Route
          path="/user-landing"
          element={
            <ProtectedRoute>
              <UserLanding />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <ProfileEdit />
            </ProtectedRoute>
          }
        />
        <Route
          path="/notifications"
          element={
            <ProtectedRoute>
              <Notifications />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-schedule"
          element={
            <ProtectedRoute>
              <MySchedule />
            </ProtectedRoute>
          }
        />

        {/* Admin Dashboard Routes */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute requireAdmin={true}>
              <DashboardLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<DashboardHome />} />
          <Route path="security/users" element={<Users />} />
          <Route path="security/roles" element={<Roles />} />
          <Route path="security/permissions" element={<Permissions />} />
          <Route path="hr/employees" element={<Employees />} />
          <Route path="hr/positions" element={<Positions />} />
          <Route path="hr/contracts" element={<Contracts />} />
          <Route path="org/companies" element={<Companies />} />
          <Route path="org/locations" element={<Locations />} />
          <Route path="schedules/shifttypes" element={<ShiftTypes />} />
          <Route path="schedules/shifts" element={<Shifts />} />
          <Route path="schedules/reports" element={<Reports />} />
          <Route path="news/news" element={<News />} />
          <Route path="news/types" element={<NewsTypes />} />
          <Route path="notifications/email" element={<Email />} />
        </Route>

        {/* Catch all route - redirect to dashboard if authenticated, login if not */}
        <Route
          path="*"
          element={
            <ProtectedRoute>
              <Navigate to="/dashboard" replace />
            </ProtectedRoute>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
