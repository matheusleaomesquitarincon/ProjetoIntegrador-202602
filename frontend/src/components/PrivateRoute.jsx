import { useAuth } from '../context/AuthContext';

export default function PrivateRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="auth-loading">Verificando sua sessão...</div>;
  return user ? children : null;
}
