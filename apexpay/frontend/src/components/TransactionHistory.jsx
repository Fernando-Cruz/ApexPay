import React from 'react';
import { ArrowUpRight, ArrowDownLeft, Receipt, Clock, Calendar } from 'lucide-react';

export default function TransactionHistory({ transactions = [], loading, selectedDays, onDaysChange }) {
  return (
    <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-sm flex flex-col h-full">
      
      {/* Cabeçalho do Extrato com Filtro */}
      <div className="flex items-center justify-between mb-5 pb-4 border-b border-slate-800">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-slate-800 border border-slate-700/50 rounded-xl text-blue-400">
            <Receipt className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-base font-semibold text-white">Extrato de Transações</h2>
            <p className="text-xs text-slate-400">Últimas movimentações</p>
          </div>
        </div>

        {/* Componente Select para Filtro de Dias */}
        <div className="flex items-center gap-2 bg-slate-950/60 border border-slate-800 px-3 py-1.5 rounded-xl">
          <Calendar className="w-4 h-4 text-slate-400" />
          <select
            value={selectedDays}
            onChange={(e) => onDaysChange(Number(e.target.value))}
            className="bg-transparent text-xs text-slate-200 font-medium focus:outline-none cursor-pointer"
          >
            <option value={30} className="bg-slate-900 text-white">Últimos 30 dias</option>
            <option value={45} className="bg-slate-900 text-white">Últimos 45 dias</option>
            <option value={60} className="bg-slate-900 text-white">Últimos 60 dias</option>
          </select>
        </div>
      </div>

      {/* Lista de Movimentações */}
      <div className="flex-1 overflow-y-auto max-h-[360px] pr-1 space-y-2.5 custom-scrollbar">
        {loading ? (
          Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-14 bg-slate-800/50 border border-slate-800 rounded-xl animate-pulse" />
          ))
        ) : transactions.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-10 text-center">
            <Clock className="w-8 h-8 text-slate-600 mb-2" />
            <p className="text-slate-400 text-sm font-medium">Nenhuma transação encontrada</p>
            <p className="text-slate-500 text-xs mt-0.5">
              Nenhuma movimentação realizada nos últimos {selectedDays} dias.
            </p>
          </div>
        ) : (
          transactions.map((tx) => {
            const currentAccount = localStorage.getItem('apexpay_account');
            const isSent = tx.sourceAccount === currentAccount;

            return (
              <div
                key={tx.id || tx.transactionId}
                className="flex items-center justify-between p-3 bg-slate-950/40 hover:bg-slate-800/40 border border-slate-800/80 hover:border-slate-700/60 rounded-xl transition-all duration-150"
              >
                <div className="flex items-center gap-3">
                  <div
                    className={`p-2 rounded-lg border ${
                      isSent
                        ? 'bg-rose-500/10 border-rose-500/20 text-rose-400'
                        : 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400'
                    }`}
                  >
                    {isSent ? <ArrowUpRight className="w-4 h-4" /> : <ArrowDownLeft className="w-4 h-4" />}
                  </div>

                  <div>
                    <p className="text-xs font-semibold text-slate-200">
                      {isSent ? `Para: ${tx.destinationAccount}` : `De: ${tx.sourceAccount}`}
                    </p>
                    <p className="text-[11px] text-slate-500 font-mono mt-0.5">
                      {tx.timestamp ? new Date(tx.timestamp).toLocaleString('pt-BR') : 'Agora mesmo'}
                    </p>
                  </div>
                </div>

                <span
                  className={`text-sm font-mono font-bold ${
                    isSent ? 'text-rose-400' : 'text-emerald-400'
                  }`}
                >
                  {isSent ? '-' : '+'} R$ {Number(tx.amount || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
                </span>
              </div>
            );
          })
        )}
      </div>

    </div>
  );
}