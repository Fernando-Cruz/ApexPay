import React from 'react';
import { Wallet, RefreshCw, UserCheck } from 'lucide-react';

export default function BalanceCard({ balance, accountNumber, username, loading, onRefresh }) {
  return (
    <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-sm">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        
        {/* Identificação da Conta e Usuário */}
        <div className="flex items-center gap-4">
          <div className="p-3 bg-blue-500/10 border border-blue-500/20 rounded-xl text-blue-400">
            <Wallet className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
                Conta Principal
              </span>
              {username && (
                <span className="inline-flex items-center gap-1 text-[10px] bg-slate-800 text-slate-300 px-2 py-0.5 rounded-full border border-slate-700">
                  <UserCheck className="w-3 h-3 text-emerald-400" />
                  {username}
                </span>
              )}
            </div>
            <p className="text-xs text-slate-500 mt-0.5 font-mono">
              Nº {accountNumber || '---'}
            </p>
          </div>
        </div>

        {/* Exibição do Saldo e Botão de Refresh */}
        <div className="flex items-center justify-between sm:justify-end gap-4 border-t border-slate-800 sm:border-t-0 pt-3 sm:pt-0">
          <div className="text-left sm:text-right">
            <span className="text-xs text-slate-400 font-medium block mb-0.5">
              Saldo Disponível
            </span>
            <span className="text-2xl sm:text-3xl font-bold text-emerald-400 font-mono tracking-tight">
              {loading ? (
                <span className="text-slate-600 animate-pulse">R$ ---,--</span>
              ) : (
                `R$ ${Number(balance || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
              )}
            </span>
          </div>

          <button
            onClick={onRefresh}
            disabled={loading}
            className="p-2.5 text-slate-400 hover:text-white hover:bg-slate-800/80 border border-transparent hover:border-slate-700 rounded-xl transition-all duration-200 active:scale-95 disabled:opacity-50"
            title="Atualizar saldo"
          >
            <RefreshCw className={`w-5 h-5 ${loading ? 'animate-spin text-blue-400' : ''}`} />
          </button>
        </div>

      </div>
    </div>
  );
}