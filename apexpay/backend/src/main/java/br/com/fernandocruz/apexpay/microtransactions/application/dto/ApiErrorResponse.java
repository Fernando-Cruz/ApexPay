package br.com.fernandocruz.apexpay.microtransactions.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude
public class ApiErrorResponse {

    private String title;
    private int status;
    private String detail;
    private OffsetDateTime timestamp;
    private Map<String, String> fieldErrors; // Detalhes de validação de campos (@Valid)
}