import { useState } from 'react';
import { executeTransfer, checkAccountExists } from '../services/api';
import {
  Send,
  CheckCircle2,
  AlertCircle,
  RefreshCw,
  KeyRound,
  LogOut,
  User,
  CreditCard,
  XCircle
} from 'lucide-react';

export default function TransferCard({ username, accountNumber, onLogout }) {
  const [destinationAccount, setDestinationAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [idempotencyKey, setIdempotencyKey] = useState(crypto.randomUUID());
  const [loading, setLoading] = useState(false);
  const [response, setResponse] = useState(null);
  const [error, setError] = useState(null);

  // Estados de validação da conta
  const [accountValidationStatus, setAccountValidationStatus] = useState('idle'); // 'idle' | 'validating' | 'valid' | 'invalid'
  const [accountValidationError, setAccountValidationError] = useState('');

  // Regex para aceitar formato UUID ou números de conta simples
  const uuidRegex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;

  // Função chamada no onBlur do campo de conta
  const handleAccountBlur = async () => {
    const cleanAccount = destinationAccount.trim();

    if (!cleanAccount) {
      setAccountValidationStatus('idle');
      setAccountValidationError('');
      return;
    }

    setAccountValidationStatus('validating');
    setAccountValidationError('');

    try {
      const result = await checkAccountExists(cleanAccount);

      // Suporta se o backend retornar { exists: true } ou o objeto da conta diretamente
      if (result && (result.exists === true || result.id || result.accountNumber)) {
        setAccountValidationStatus('valid');
      } else {
        setAccountValidationStatus('invalid');
        setAccountValidationError('Conta de destino não encontrada.');
      }
    } catch (err) {
      setAccountValidationStatus('invalid');

      if (err.response?.status === 404) {
        setAccountValidationError('Conta de destino não encontrada no sistema.');
      } else {
        const errorMsg = err.response?.data?.message || 'Erro de comunicação ao validar conta.';
        setAccountValidationError(errorMsg);
      }
    }
  };

  const handleTransfer = async (e) => {
    e.preventDefault();

    const cleanDestination = destinationAccount.trim();
    if (accountValidationStatus === 'invalid' || !cleanDestination) {
      setAccountValidationStatus('invalid');
      setAccountValidationError('Informe uma conta válida antes de transferir.');
      return;
    }

    setLoading(true);
    setError(null);
    setResponse(null);

    try {
      const result = await executeTransfer(cleanDestination, amount, idempotencyKey);

      setResponse({
        status: 'Sucesso',
        key: result.keyUsed,
        data: result.data
      });

      setAmount('');
      setDestinationAccount('');
      setAccountValidationStatus('idle');
      setIdempotencyKey(crypto.randomUUID());
    } catch (err) {
      //console.log("Detalhes do erro:", error?.details || "Erro não informado");
      //console.log("Detalhes do erro:", err.response?.data || err.message);
      console.error("Detalhes do erro:", err.response?.data || err.message);

      //console.error("Detalhes do erro:", err.response?.data);
      const backendError = err.response?.data;

      setError({
        message:
          backendError?.detail ||
          backendError?.message ||
          backendError?.error ||
          err.message ||
          'Erro ao realizar transferência.',
        status: err.response?.status || '500'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl">
      {/* Informações do Usuário */}
      <div className="bg-slate-950 border border-slate-800/80 rounded-xl p-4 mb-5">
        <div className="flex items-center justify-between mb-3 border-b border-slate-800/60 pb-2.5">
          <span className="text-xs text-emerald-400 font-medium flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            <span>Sessão Autenticada</span>
          </span>

          <button
            type="button"
            onClick={onLogout}
            className="text-xs text-rose-400 hover:text-rose-300 flex items-center gap-1 transition-colors cursor-pointer font-medium"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span>Sair</span>
          </button>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-blue-500/10 border border-blue-500/20 rounded-lg text-blue-400">
              <User className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-wider text-slate-500 font-medium">Usuário Logado</p>
              <p className="text-xs font-semibold text-white truncate max-w-[120px]">{username || 'Usuário'}</p>
            </div>
          </div>

          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-emerald-500/10 border border-emerald-500/20 rounded-lg text-emerald-400">
              <CreditCard className="w-4 h-4" />
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-wider text-slate-500 font-medium">Conta Origem</p>
              <p className="text-xs font-mono font-bold text-emerald-400">{accountNumber || '---'}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Formulário de Transferência */}
      <form onSubmit={handleTransfer} className="space-y-4">
        <div>
          <label className="block text-xs font-medium text-slate-400 uppercase tracking-wider mb-2">
            Conta de Destino
          </label>

          <div className="relative">
            <input
              type="text"
              required
              placeholder="Digite a conta de destino"
              value={destinationAccount}
              onChange={(e) => {
                setDestinationAccount(e.target.value);
                if (accountValidationStatus !== 'idle') {
                  setAccountValidationStatus('idle');
                  setAccountValidationError('');
                }
              }}
              onBlur={handleAccountBlur}
              className={`w-full bg-slate-950 border rounded-xl px-4 py-3 pr-10 text-sm focus:outline-none font-mono text-slate-200 transition-colors ${
                accountValidationStatus === 'invalid'
                  ? 'border-rose-500 focus:border-rose-500'
                  : accountValidationStatus === 'valid'
                  ? 'border-emerald-500 focus:border-emerald-500'
                  : 'border-slate-800 focus:border-blue-500'
              }`}
            />

            <div className="absolute right-3 top-3.5 flex items-center">
              {accountValidationStatus === 'validating' && (
                <RefreshCw className="w-4 h-4 text-blue-400 animate-spin" />
              )}
              {accountValidationStatus === 'valid' && (
                <CheckCircle2 className="w-4 h-4 text-emerald-400" />
              )}
              {accountValidationStatus === 'invalid' && (
                <XCircle className="w-4 h-4 text-rose-400" />
              )}
            </div>
          </div>

          {accountValidationError && (
            <p className="text-xs text-rose-400 mt-1.5 flex items-center gap-1">
              <AlertCircle className="w-3.5 h-3.5" />
              <span>{accountValidationError}</span>
            </p>
          )}
        </div>

        {/* Campo Valor */}
        <div>
          <label className="block text-xs font-medium text-slate-400 uppercase tracking-wider mb-2">
            Valor (R$)
          </label>
          <div className="relative">
            <span className="absolute left-4 top-3 text-slate-500 font-medium">R$</span>
            <input
              type="number"
              step="0.01"
              min="0.01"
              required
              placeholder="0,00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl pl-12 pr-4 py-3 text-sm font-semibold focus:outline-none focus:border-blue-500 text-slate-200"
            />
          </div>
        </div>

        {/* Chave de Idempotência */}
        <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3">
          <div className="flex items-center justify-between mb-1">
            <div className="flex items-center gap-1.5 text-xs text-slate-400 font-medium">
              <KeyRound className="w-3.5 h-3.5 text-emerald-400" />
              <span>Idempotency-Key</span>
            </div>
            <button
              type="button"
              onClick={() => setIdempotencyKey(crypto.randomUUID())}
              className="text-xs text-blue-400 hover:text-blue-300 flex items-center gap-1 font-medium cursor-pointer"
            >
              <RefreshCw className="w-3 h-3" />
              <span>Nova Chave</span>
            </button>
          </div>
          <input
            type="text"
            readOnly
            value={idempotencyKey}
            className="w-full bg-transparent text-[11px] font-mono text-emerald-400 focus:outline-none select-all"
          />
        </div>

        {/* Botão Enviar */}
        <button
          type="submit"
          disabled={loading || accountValidationStatus === 'invalid'}
          className="w-full bg-blue-600 hover:bg-blue-500 active:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-white font-medium py-3 rounded-xl transition-all flex items-center justify-center gap-2 cursor-pointer shadow-lg shadow-blue-500/20"
        >
          {loading ? (
            <RefreshCw className="w-5 h-5 animate-spin" />
          ) : (
            <div className="flex items-center gap-2">
              <Send className="w-4 h-4" />
              <span>Transferir Agora</span>
            </div>
          )}
        </button>
      </form>

      {/* Alertas de Resultado */}
      {response && (
        <div className="mt-5 p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-xl text-emerald-400 text-xs">
          <div className="flex items-center gap-2 font-semibold text-sm mb-2">
            <CheckCircle2 className="w-5 h-5 text-emerald-400" />
            <span>Transferência Processada</span>
          </div>
          <p className="font-mono text-[11px] break-all mb-2">
            <strong>Key Registrada:</strong> {response.key}
          </p>
          <pre className="p-2 bg-slate-950 rounded border border-slate-800 overflow-x-auto text-slate-300">
            {JSON.stringify(response.data, null, 2)}
          </pre>
        </div>
      )}

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