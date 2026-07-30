package br.com.fernandocruz.apexpay.microtransactions.application.dto;

import java.util.UUID;
public record AccountValidationResponseDTO(
        boolean exists,
        UUID accountId,
        String accountNumber,
        UUID userId
) {

}
