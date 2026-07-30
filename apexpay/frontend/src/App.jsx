// =========================================================
// Importa o Hook useState do React.
// O useState é utilizado para criar variáveis de estado.
// Sempre que um estado é alterado, o React atualiza automaticamente a interface.
// =========================================================
import { useState } from 'react';

// Importa o componente responsável pela tela de Login.
import LoginCard from './components/LoginCard';

// Importa o componente responsável pela tela de transferência bancária.
import TransferCard from './components/TransferCard';

// =========================================================
// Importa o ícone de escudo da biblioteca Lucide React.
// Será utilizado no cabeçalho da aplicação.
// =========================================================
import { ShieldCheck } from 'lucide-react';

// =========================================================
// Componente principal da aplicação.
// Todo o sistema começa por este componente.
// =========================================================
export default function App() {

  // =======================================================
  // Estado que guarda o Token JWT do usuário.
  // Quando a aplicação inicia, tenta recuperar o Token salvo anteriormente no LocalStorage.
  // Caso exista, significa que o usuário já estava logado.
  // =======================================================
  const [token, setToken] = useState(
    localStorage.getItem('apexpay_token')
  );

  // =======================================================
  // Estado que guarda o nome (email) do usuário.
  // Se existir no LocalStorage, ele é carregado.
  // Caso contrário, inicia vazio.
  // =======================================================
  const [username, setUsername] = useState(
    localStorage.getItem('apexpay_user') || ''
  );

  // =======================================================
  // Estado que guarda o número da conta do usuário.
  // Também tenta recuperar o valor salvo anteriormente.
  // =======================================================
  const [accountNumber, setAccountNumber] = useState(
    localStorage.getItem('apexpay_account') || ''
  );

  // =======================================================
  // Função chamada quando o Login é realizado com sucesso.
  // O componente LoginCard envia:
  // token
  // username
  // accountNumber
  //
  // Estes dados atualizam os estados da aplicação.
  // =======================================================
  const handleLoginSuccess = ({
    token,
    username,
    accountNumber
  }) => {
    // Atualiza o Token.
    setToken(token);
    // Atualiza o usuário.
    setUsername(username);
    // Atualiza o número da conta.
    setAccountNumber(accountNumber);
  };

  // =======================================================
  // Função responsável por fazer Logout.
  //
  // Remove todos os dados salvos no navegador e limpa os estados da aplicação.
  // =======================================================
  const handleLogout = () => {
    // Remove o Token salvo.
    localStorage.removeItem('apexpay_token');
    // Remove o usuário salvo.
    localStorage.removeItem('apexpay_user');
    // Remove o número da conta salvo.
    localStorage.removeItem('apexpay_account');
    // Limpa o estado do Token.
    setToken(null);
    // Limpa o usuário.
    setUsername('');
    // Limpa a conta.
    setAccountNumber('');
  };

  // =======================================================
  // Renderização da interface.
  // O React executa este return para montar a tela.
  // =======================================================
  return (
    // =====================================================
    // Container principal da aplicação.
    // Classes Tailwind:
    //
    // min-h-screen -> ocupa toda altura da tela
    // bg-slate-950 -> fundo escuro
    // text-slate-100 -> texto claro
    // flex -> utiliza Flexbox
    // flex-col -> organiza em coluna
    // items-center -> centraliza horizontalmente
    // justify-center -> centraliza verticalmente
    // p-4 -> padding interno
    // =====================================================
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center justify-center p-4">
      {/* ===================================================
          CABEÇALHO DA APLICAÇÃO
         =================================================== */}
      <div className="w-full max-w-md text-center mb-6">

        {/* Badge azul exibindo o nome do projeto */}
        <div className="inline-flex items-center gap-2 bg-blue-500/10 border border-blue-500/20 px-4 py-1.5 rounded-full text-blue-400 text-sm font-semibold mb-3">
          {/* Ícone de escudo */}
          <ShieldCheck className="w-4 h-4" />
          {/* Texto ao lado do ícone */}
          <span>Apexpay Microservices</span>
        </div>

        {/* ===================================================
            Título principal
            Muda dependendo se o usuário está logado.
           =================================================== */}
        <h1 className="text-3xl font-bold tracking-tight text-white">

          {
            token
              // Se existir Token...
              ? 'Transferência Instantânea'
              // Caso contrário...
              : 'Acesse sua Conta'
          }

        </h1>

        {/* ===================================================
            Subtítulo.
            Também muda conforme o login.
           =================================================== */}
        <p className="text-slate-400 text-sm mt-1">

          {
            token
              // Usuário autenticado
              ? 'Módulo de Microtransações P2P com Idempotência'
              // Usuário não autenticado
              : 'Entre com suas credenciais para continuar'
          }
        </p>

      </div>

      {/* ===================================================
          Alternância entre as telas.
          ===================================================

          Se NÃO existir Token:

                LoginCard

          Caso exista:

                TransferCard

          Ou seja:

          Token = Login realizado
          Sem Token = Tela de Login
      ==================================================== */}
      {

        !token
          // ===============================================
          // Usuário ainda NÃO fez Login.
          // Exibe o componente LoginCard.
          // ===============================================
          ? (

            <LoginCard
              onLoginSuccess={handleLoginSuccess}
            />

          )

          // ===============================================
          // Usuário já está autenticado.
          // Exibe a tela de transferência.
          // ===============================================
          : (

            <TransferCard

              // Nome do usuário.
              username={username}

              // Número da conta.
              accountNumber={accountNumber}

              // Função chamada ao clicar em Logout.
              onLogout={handleLogout}

            />

          )

      }

    </div>
  );
}