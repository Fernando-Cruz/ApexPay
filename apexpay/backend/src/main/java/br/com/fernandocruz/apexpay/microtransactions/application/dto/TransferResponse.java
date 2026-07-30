package br.com.fernandocruz.apexpay.microtransactions.application.dto;

import br.com.fernandocruz.apexpay.microtransactions.domain.model.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private UUID transactionId;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private TransactionStatus status;
    private String idempotencyKey;
    private OffsetDateTime timestamp;
}