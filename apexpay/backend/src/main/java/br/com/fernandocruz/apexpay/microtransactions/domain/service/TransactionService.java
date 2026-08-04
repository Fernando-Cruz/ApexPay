package br.com.fernandocruz.apexpay.microtransactions.domain.service;

import br.com.fernandocruz.apexpay.microtransactions.application.dto.TransactionHistoryDTO;
import br.com.fernandocruz.apexpay.microtransactions.application.dto.TransferRequest;
import br.com.fernandocruz.apexpay.microtransactions.application.dto.TransferResponse;
import br.com.fernandocruz.apexpay.microtransactions.domain.model.*;
import br.com.fernandocruz.apexpay.microtransactions.domain.repository.AccountRepository;
import br.com.fernandocruz.apexpay.microtransactions.domain.repository.IdempotencyKeyRepository;
import br.com.fernandocruz.apexpay.microtransactions.domain.repository.TransactionRepository;
import br.com.fernandocruz.apexpay.microtransactions.domain.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<TransactionHistoryDTO> getHistoryByUser(String userEmail, Integer days, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada."));

        // Se não informar os dias, usa 30 por padrão
        int daysFilter = (days != null && days > 0) ? days : 30;
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(daysFilter);

        return transactionRepository.findByAccountHistoryAndDate(account.getId(), startDate, pageable)
                .map(tx -> TransactionHistoryDTO.builder()
                        .id(tx.getId())
                        .sourceAccount(tx.getSourceAccount().getAccountNumber())
                        .destinationAccount(tx.getDestinationAccount().getAccountNumber())
                        .amount(tx.getAmount())
                        .status(tx.getStatus())
                        .timestamp(tx.getCreatedAt())
                        .build());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransferResponse executeTransfer(String userEmail, TransferRequest request, String idempotencyKeyHeader) {

        // 1. Controle de Idempotência: Verifica se a requisição já foi processada anteriormente
        if (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank()) {
            var existingKey = idempotencyKeyRepository.findByClientKey(idempotencyKeyHeader);
            if (existingKey.isPresent()) {
                IdempotencyKey keyRecord = existingKey.get();
                if (keyRecord.getStatus() == IdempotencyStatus.COMPLETED) {
                    try {
                        return objectMapper.readValue(keyRecord.getResponseBody(), TransferResponse.class);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("Erro ao desserializar resposta de idempotência.", e);
                    }
                } else {
                    throw new IllegalStateException("Esta transação ainda está em processamento por outra requisição.");
                }
            }
        }

        // 2. OTIMIZADO: Busca o usuário e sua conta de origem diretamente no banco pelo Index (sem findAll)
        User sourceUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário pagador não encontrado."));

        Account sourceAccount = accountRepository.findByUserId(sourceUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Conta de origem não encontrada para o usuário."));

        // 3. Busca a conta de destino (suporta Número da Conta ou UUIDs/Identifiers)
        Account destinationAccount = accountRepository.findByIdentifier(request.getDestinationAccount())
                .or(() -> accountRepository.findByAccountNumber(request.getDestinationAccount()))
                .orElseThrow(() -> new IllegalArgumentException("Conta de destino não encontrada."));

        // 4. Regra de Negócio: Impede transferência para a própria conta
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new IllegalArgumentException("Não é permitido realizar transferências para a mesma conta.");
        }

        // 5. Prevenção de Deadlock: Ordenação consistente por UUID para Lock Pessimista no PostgreSQL
        Account lockedSourceAccount;
        Account lockedDestinationAccount;

        if (sourceAccount.getId().compareTo(destinationAccount.getId()) < 0) {
            lockedSourceAccount = accountRepository.findByIdWithPessimisticLock(sourceAccount.getId())
                    .orElseThrow(() -> new IllegalStateException("Erro ao bloquear a conta de origem."));
            lockedDestinationAccount = accountRepository.findByIdWithPessimisticLock(destinationAccount.getId())
                    .orElseThrow(() -> new IllegalStateException("Erro ao bloquear a conta de destino."));
        } else {
            lockedDestinationAccount = accountRepository.findByIdWithPessimisticLock(destinationAccount.getId())
                    .orElseThrow(() -> new IllegalStateException("Erro ao bloquear a conta de destino."));
            lockedSourceAccount = accountRepository.findByIdWithPessimisticLock(sourceAccount.getId())
                    .orElseThrow(() -> new IllegalStateException("Erro ao bloquear a conta de origem."));
        }

        // 6. Regra de Negócio: Valida se a conta de origem possui saldo suficiente
        if (lockedSourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar a transferência.");
        }

        // 7. Atualização Atômica de Saldos
        lockedSourceAccount.setBalance(lockedSourceAccount.getBalance().subtract(request.getAmount()));
        lockedDestinationAccount.setBalance(lockedDestinationAccount.getBalance().add(request.getAmount()));

        accountRepository.save(lockedSourceAccount);
        accountRepository.save(lockedDestinationAccount);

        // 8. Registra no Histórico de Transações
        Transaction transaction = Transaction.builder()
                .sourceAccount(lockedSourceAccount)
                .destinationAccount(lockedDestinationAccount)
                .amount(request.getAmount())
                .status(TransactionStatus.COMPLETED)
                .idempotencyKey(idempotencyKeyHeader)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // 9. Constrói a Resposta
        TransferResponse response = TransferResponse.builder()
                .transactionId(savedTransaction.getId())
                .sourceAccount(lockedSourceAccount.getAccountNumber())
                .destinationAccount(lockedDestinationAccount.getAccountNumber())
                .amount(savedTransaction.getAmount())
                .status(TransactionStatus.COMPLETED)
                .timestamp(OffsetDateTime.now())
                .idempotencyKey(idempotencyKeyHeader)
                .build();

        // 10. Salva o Log de Idempotência
        if (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank()) {
            try {
                String responseJson = objectMapper.writeValueAsString(response);
                IdempotencyKey newKey = IdempotencyKey.builder()
                        .clientKey(idempotencyKeyHeader)
                        .responseBody(responseJson)
                        .status(IdempotencyStatus.COMPLETED)
                        .build();
                idempotencyKeyRepository.save(newKey);
            } catch (DataIntegrityViolationException e) {
                throw new IllegalStateException("Requisição duplicada detectada para esta Chave de Idempotência.");
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Falha ao salvar log de idempotência.", e);
            }
        }

        return response;
    }
}