package br.com.fernandocruz.apexpay.microtransactions.application.controller;

import br.com.fernandocruz.apexpay.microtransactions.application.dto.TransferRequest;
import br.com.fernandocruz.apexpay.microtransactions.application.dto.TransferResponse;
import br.com.fernandocruz.apexpay.microtransactions.domain.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Transferências P2P", description = "Operações de débito, crédito e liquidação de microtransações entre contas com garantia de idempotência")
public class TransactionController {

    private final TransactionService transactionService;

    // Endpoint para realização de transferências instantâneas P2P.
    // Suporta garantia de Idempotência via cabeçalho HTTP "Idempotency-Key".
    @Operation(summary = "Realizar transferência P2P instantânea",
            description = """
                       Executa o débito na conta do usuário autenticado e o crédito na conta de destino. 
                       
                       * **Segurança**: Bloqueio exclusivo no PostgreSQL via `Pessimistic Write` ordenado.
                       * **Idempotência**: Envie o parâmetro `Idempotency-Key` no cabeçalho HTTP com um UUID para evitar reprocessamentos acidentais.
                       """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transferência realizada com sucesso ou resposta idempotente retornada."),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente, dados inválidos ou tentativa de transferência para a própria conta."),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou expirado."),
            @ApiResponse(responseCode = "409", description = "Conflito de concorrência ou chave de idempotência ainda em processamento.")
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransferRequest request,
            @Parameter(description = "Identificador único da requisição (UUID) gerado pelo cliente para garantir a idempotência do débito.", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        TransferResponse response = transactionService.executeTransfer(
                userDetails.getUsername(),
                request,
                idempotencyKey
        );
        return ResponseEntity.ok(response);
    }
}
