import { useState } from 'react';
import { useAuth } from '../context/AuthContext';

export default function Cadastro({ onBackToLogin }) {
  const { cadastrar } = useAuth();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    if (senha.length < 8) { setError('A senha deve ter pelo menos 8 caracteres.'); return; }
    if (senha !== confirmacao) { setError('As senhas precisam ser iguais.'); return; }
    setSubmitting(true);
    try { await cadastrar(email, senha, confirmacao); setSuccess(true); } catch (requestError) { setError(requestError.message); } finally { setSubmitting(false); }
  }

  if (success) return <div className="auth-page"><div className="auth-card"><div className="auth-brand"><span className="brand-mark">J</span><span>Java<span className="brand-accent">Studio</span></span></div><div className="auth-success"><span>✓</span><h1>Cadastro concluído</h1><p>Sua conta foi criada. Agora você já pode entrar.</p><button className="primary-button auth-submit" onClick={onBackToLogin}>Ir para login</button></div></div></div>;
  return <div className="auth-page"><div className="auth-card"><div className="auth-brand"><span className="brand-mark">J</span><span>Java<span className="brand-accent">Studio</span></span></div><p className="eyebrow">Comece sua jornada</p><h1>Crie sua conta</h1><p className="auth-description">Cadastre-se para guardar seu progresso e suas anotações.</p><form onSubmit={handleSubmit}><label>E-mail<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="seu@email.com" autoComplete="email" required /></label><label>Senha<input type="password" value={senha} onChange={(event) => setSenha(event.target.value)} placeholder="Mínimo de 8 caracteres" autoComplete="new-password" required /></label><label>Confirmar senha<input type="password" value={confirmacao} onChange={(event) => setConfirmacao(event.target.value)} placeholder="Repita sua senha" autoComplete="new-password" required /></label>{error && <p className="auth-error" role="alert">{error}</p>}<button className="primary-button auth-submit" disabled={submitting}>{submitting ? 'Criando...' : 'Criar conta'}</button></form><p className="auth-switch">Já possui uma conta? <button onClick={onBackToLogin}>Voltar para login</button></p></div></div>;
}
