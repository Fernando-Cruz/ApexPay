// =========================================================
// Importa o Hook useState e useEffect do React.
// =========================================================
import { useState, useEffect } from 'react';

// Importa os componentes da aplicação.
import LoginCard from './components/LoginCard';
import TransferCard from './components/TransferCard';
import BalanceCard from './components/BalanceCard';
import TransactionHistory from './components/TransactionHistory';

// Importa os serviços de busca de dados da API.
import { getAccountDetails, getTransactionHistory } from './services/api';

// Importa o ícone de escudo da biblioteca Lucide React.
import { ShieldCheck } from 'lucide-react';

export default function App() {

  // =======================================================
  // Estados de Autenticação e Usuário
  // =======================================================
  const [token, setToken] = useState(
    localStorage.getItem('apexpay_token')
  );

  const [username, setUsername] = useState(
    localStorage.getItem('apexpay_user') || ''
  );

  const [accountNumber, setAccountNumber] = useState(
    localStorage.getItem('apexpay_account') || ''
  );

  // =======================================================
  // Estados da Fase 4: Dados Financeiros & Filtros
  // =======================================================
  const [accountData, setAccountData] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [selectedDays, setSelectedDays] = useState(30); // 🔴 Estado do Filtro de Dias
  const [loadingData, setLoadingData] = useState(false);
  const [loadingTx, setLoadingTx] = useState(false); // 🔴 Loading específico do extrato

  // Contador/Gatilho que força recarregar os dados quando a transferência é concluída
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  // =======================================================
  // Função que busca o Saldo da Conta
  // =======================================================
  const loadAccountData = async () => {
    if (!token) return;
    try {
      setLoadingData(true);
      const currentAccount = accountNumber || localStorage.getItem('apexpay_account');
      const accountRes = await getAccountDetails(currentAccount);
      setAccountData(accountRes);
    } catch (err) {
      console.error("Erro ao carregar dados da conta:", err);
    } finally {
      setLoadingData(false);
    }
  };

  // =======================================================
  // 🔴 Função que busca o Extrato no Backend com Filtro
  // =======================================================
  const fetchTransactions = async (days) => {
    if (!token) return;
    try {
      setLoadingTx(true);
      const data = await getTransactionHistory(days);
      setTransactions(data);
    } catch (err) {
      console.error("Erro ao buscar histórico:", err);
    } finally {
      setLoadingTx(false);
    }
  };

  // Carrega os dados da conta ao logar ou quando o gatilho de recarga mudar
  useEffect(() => {
    if (token) {
      loadAccountData();
    }
  }, [token, refreshTrigger]);

  // 🔴 Carrega o extrato quando o token, o filtro de dias ou o gatilho de recarga mudarem
  useEffect(() => {
    if (token) {
      fetchTransactions(selectedDays);
    }
  }, [token, selectedDays, refreshTrigger]);

  // Função disparada quando a transferência dá certo no TransferCard
  const handleTransferSuccess = () => {
    // Incrementa o gatilho, forçando os useEffects a recarregarem Saldo e Extrato
    setRefreshTrigger((prev) => prev + 1);
  };

  // =======================================================
  // Manipuladores de Login e Logout
  // =======================================================
  const handleLoginSuccess = ({ token, username, accountNumber }) => {
    setToken(token);
    setUsername(username);
    setAccountNumber(accountNumber);
  };

  const handleLogout = () => {
    localStorage.removeItem('apexpay_token');
    localStorage.removeItem('apexpay_user');
    localStorage.removeItem('apexpay_account');
    setToken(null);
    setUsername('');
    setAccountNumber('');
    setAccountData(null);
    setTransactions([]);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center justify-center p-4 md:p-8">
      
      {/* ===================================================
          CABEÇALHO DA APLICAÇÃO
         =================================================== */}
      <div className="w-full max-w-5xl text-center mb-6">
        
        {/* Badge azul */}
        <div className="inline-flex items-center gap-2 bg-blue-500/10 border border-blue-500/20 px-4 py-1.5 rounded-full text-blue-400 text-sm font-semibold mb-3">
          <ShieldCheck className="w-4 h-4" />
          <span>Apexpay Microservices</span>
        </div>

        {/* Título Principal */}
        <h1 className="text-3xl font-bold tracking-tight text-white">
          {token ? 'Painel Financeiro & Transferências' : 'Acesse sua Conta'}
        </h1>

        {/* Subtítulo */}
        <p className="text-slate-400 text-sm mt-1">
          {token
            ? 'Módulo de Microtransações P2P com Idempotência e Extrato Reativo'
            : 'Entre com suas credenciais para continuar'}
        </p>
      </div>

      {/* ===================================================
          CONTEÚDO PRINCIPAL (TELA DE LOGIN OU DASHBOARD)
         =================================================== */}
      {!token ? (
        
        // --- TELA DE LOGIN ---
        <div className="w-full max-w-md">
          <LoginCard onLoginSuccess={handleLoginSuccess} />
        </div>

      ) : (

        // --- DASHBOARD DA FASE 4 (SALDO + TRANSFERÊNCIA + EXTRATO) ---
        <div className="w-full max-w-5xl space-y-6">

          {/* 1. Card de Saldo Atualizado */}
          <BalanceCard
            balance={accountData?.balance}
            accountNumber={accountData?.accountNumber || accountNumber}
            username={username}
            loading={loadingData}
            onRefresh={loadAccountData}
          />

          {/* 2. Grid Lado a Lado: Formulário + Histórico */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            
            <TransferCard
              username={username}
              accountNumber={accountNumber}
              onLogout={handleLogout}
              onTransferSuccess={handleTransferSuccess}
            />

            {/* 🔴 Componente de Extrato passando os estados de dias e carregamento */}
            <TransactionHistory 
              transactions={transactions} 
              loading={loadingTx}
              selectedDays={selectedDays}
              onDaysChange={(newDays) => setSelectedDays(newDays)}
            />

          </div>

        </div>
      )}

    </div>
  );
}