import { useState } from 'react';
import { useAuth } from '../context/AuthContext';

export default function Login({ onCreateAccount }) {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try { await login(email, senha); } catch (requestError) { setError(requestError.message); } finally { setSubmitting(false); }
  }

  return <div className="auth-page"><div className="auth-card"><div className="auth-brand"><span className="brand-mark">J</span><span>Java<span className="brand-accent">Studio</span></span></div><p className="eyebrow">Bem-vindo de volta</p><h1>Entre para estudar</h1><p className="auth-description">Acesse suas anotações e continue sua jornada em Java.</p><form onSubmit={handleSubmit}><label>E-mail<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="seu@email.com" autoComplete="email" required /></label><label>Senha<input type="password" value={senha} onChange={(event) => setSenha(event.target.value)} placeholder="Sua senha" autoComplete="current-password" required /></label>{error && <p className="auth-error" role="alert">{error}</p>}<button className="primary-button auth-submit" disabled={submitting}>{submitting ? 'Entrando...' : 'Entrar'}</button></form><p className="auth-switch">Ainda não tem uma conta? <button onClick={onCreateAccount}>Criar cadastro</button></p></div></div>;
}
