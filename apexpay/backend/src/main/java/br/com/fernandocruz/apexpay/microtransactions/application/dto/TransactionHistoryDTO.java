package br.com.fernandocruz.apexpay.microtransactions.application.dto;

import br.com.fernandocruz.apexpay.microtransactions.domain.model.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionHistoryDTO {
    private UUID id;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private TransactionStatus status;
    private OffsetDateTime timestamp;
}