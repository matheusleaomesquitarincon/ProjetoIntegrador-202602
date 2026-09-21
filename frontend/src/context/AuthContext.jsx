import { createContext, useContext, useEffect, useState } from 'react';

const API = 'http://localhost:8080/api/auth';
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`${API}/me`, { credentials: 'include' })
      .then((response) => response.ok ? response.json() : null)
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  async function login(email, senha) {
    const response = await fetch(`${API}/login`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email, senha }) });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.erro || 'Não foi possível entrar.');
    setUser(data);
  }

  async function cadastrar(email, senha, confirmacao) {
    const response = await fetch(`${API}/cadastro`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email, senha, confirmacao }) });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.erro || 'Não foi possível criar a conta.');
  }

  async function logout() {
    await fetch(`${API}/logout`, { method: 'POST', credentials: 'include' });
    setUser(null);
  }

  return <AuthContext.Provider value={{ user, loading, login, cadastrar, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
