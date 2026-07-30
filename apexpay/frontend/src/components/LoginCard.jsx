// src/components/LoginCard.jsx
import { useState } from 'react';
import { loginUser } from '../services/api';
import { LogIn, Mail, RefreshCw, AlertCircle } from 'lucide-react';

export default function LoginCard({ onLoginSuccess }) {
  const [email, setEmail] = useState(localStorage.getItem('apexpay_user') || '');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const data = await loginUser(email, password);
      
      const token = data?.token || data?.accessToken || localStorage.getItem('apexpay_token');
      const accountFromApi = data?.accountNumber || data?.account || data?.user?.accountNumber || '';

      onLoginSuccess({
        token,
        username: email,
        accountNumber: accountFromApi
      });
    } catch (err) {
      console.error("Erro na autenticação:", err.response?.data);
      
      // Captura mensagem detalhada da validação do Spring (@Email ou @NotBlank)
      const backendMessage = err.response?.data?.message 
                          || err.response?.data?.detail 
                          || err.response?.data?.error 
                          || 'Falha ao autenticar. Verifique e-mail e senha.';

      setError({
        message: backendMessage,
        status: err.response?.status || 'Erro'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-slate-400 uppercase tracking-wider mb-2">
            E-mail cadastrado
          </label>
          <div className="relative">
            <span className="absolute left-4 top-3.5 text-slate-500">
              <Mail className="w-4 h-4" />
            </span>
            <input
              type="email"
              required
              placeholder="seu.email@dominio.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-11 pr-4 py-3 text-sm focus:outline-none focus:border-blue-500 transition-colors"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-400 uppercase tracking-wider mb-2">
            Senha
          </label>
          <input
            type="password"
            required
            autoComplete="current-password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-blue-500 transition-colors"
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-600 hover:bg-blue-500 active:bg-blue-700 text-white font-medium py-3 rounded-xl transition-all flex items-center justify-center gap-2 cursor-pointer shadow-lg shadow-blue-500/20"
        >
          {loading ? (
            <RefreshCw className="w-5 h-5 animate-spin" />
          ) : (
            <div className="flex items-center gap-2">
              <LogIn className="w-4 h-4" />
              <span>Entrar no Sistema</span>
            </div>
          )}
        </button>
      </form>

      {error && (
        <div className="mt-5 p-4 bg-rose-500/10 border border-rose-500/20 rounded-xl text-rose-400 text-xs">
          <div className="flex items-center gap-2 font-semibold text-sm mb-1">
            <AlertCircle className="w-5 h-5 text-rose-400" />
            <span>Erro ({error.status})</span>
          </div>
          <p className="text-slate-300 mt-1">{error.message}</p>
        </div>
      )}
    </div>
  );
}