# ApexPay - Sistema Distribuído de Microtransações Financeiras

O **ApexPay** é uma plataforma de microtransações financeiras de alta performance, projetada sob a arquitetura de microsserviços. O objetivo principal do projeto é simular o ecossistema de um banco digital moderno, garantindo consistência transacional (ACID), resiliência, segurança avançada e processamento assíncrono de eventos.

O ecossistema é composto por microsserviços especializados utilizando **Java** e **Python**, demonstrando interoperabilidade, desacoplamento e o uso das melhores práticas de engenharia de software voltadas para o setor bancário.

---

## 🛠️ Arquitetura e Tecnologias

A solução foi desenhada utilizando apenas tecnologias open-source e amplamente adotadas no mercado financeiro de grande escala:

*   **Core Transacional (Back-End):** Java 17+ & Spring Boot 3
    *   *Spring Security & JWT:* Autenticação e autorização robustas de usuários.
    *   *Spring Data JPA:* Persistência de dados eficiente.
    *   *Controle de Concorrência:* Implementação de travas (Locks) para evitar problemas de *race conditions* em transferências simultâneas.
*   **Módulo de Auditoria e Conciliação (Back-End):** Python 3.11+ & FastAPI
    *   *Processamento de Dados:* Pandas para análise de grandes volumes transacionais e relatórios.
    *   *Desempenho:* FastAPI para barramento de leitura assíncrona de relatórios executivos.
*   **Mensageria e Eventos:** RabbitMQ / Apache Kafka
    *   Garante o desacoplamento entre os serviços. O Core (Java) publica eventos de transações efetuadas que são consumidos de forma assíncrona pelo serviço de Auditoria (Python).
*   **Banco de Dados:** PostgreSQL
    *   Banco de dados relacional robusto, configurado com foco em integridade referencial e conformidade ACID.
*   **Ambiente e Infraestrutura:** Docker & Docker Compose
    *   Toda a infraestrutura do projeto (Bancos de dados, Brokers e Aplicações) é orquestrada em containers para garantir consistência e facilidade de execução em qualquer ambiente.

---

## 🚀 Principais Funcionalidades

### 1. Core Transacional (Módulo Java)
*   **Abertura e Gestão de Contas:** Cadastro de usuários e criação automática de contas digitais com geração de chaves únicas.
*   **Microtransações Seguras (P2P):** Transferências instantâneas entre contas com validação estrita de saldo e dupla checagem de integridade.
*   **Garantia de Não-Duplicidade:** Mecanismos para evitar o reprocessamento de transações idênticas enviadas em um curto intervalo de tempo.

### 2. Auditoria e Antifraude (Módulo Python)
*   **Conciliação Bancária Automatizada:** Rotinas assíncronas que cruzam os saldos históricos para garantir que não houve desvio ou inconsistência nos registros.
*   **Análise de Padrões Suspeitos:** Filtro rápido para identificar transações repetitivas ou fora do perfil do usuário (simulação de motor de antifraude).
*   **Painel Executivo de Dados:** Geração de relatórios consolidados de volume transacional e liquidez.

---

## 📐 O Desenho da Solução (Data Flow)

1. **Request:** O cliente solicita uma transferência.
2. **Processamento:** O serviço em **Java** intercepta, valida a segurança (JWT), abre uma transação isolada no **PostgreSQL**, valida o saldo, debita do remetente, credita no destinatário e comita.
3. **Evento:** Assim que comitada, a transação gera um evento publicado no **Broker de Mensageria**.
4. **Consumo:** O serviço em **Python** consome o evento em background, atualizando o motor de auditoria sem gerar latência para o usuário final.

---

## 🏗️ Como Executar o Projeto (Em Desenvolvimento)

> ⚠️ **Nota:** Este projeto está em desenvolvimento ativo. Os componentes estão sendo integrados gradativamente.

Para rodar o ambiente completo de infraestrutura em sua máquina, certifique-se de ter o **Docker** instalado e execute:

```bash
docker-compose up -d
