package br.com.fernandocruz.apexpay.microtransactions.application.controller;

import br.com.fernandocruz.apexpay.microtransactions.domain.model.Account;
import br.com.fernandocruz.apexpay.microtransactions.domain.repository.AccountRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Contas", description = "Consulta de informações de contas digitais")
public class AccountController {

    //@Operation(summary = "Buscar conta por ID (UUID)")
    //@GetMapping("/validate/{accountNumber}")
    //public ResponseEntity<AccountValidationResponseDTO> validateAccount(@PathVariable String accountNumber) {
    //    AccountValidationResponseDTO response = accountValidationService.validateByAccountNumber(accountNumber);

    //    if (!response.exists()) {
    //        return ResponseEntity.notFound().build(); // Retorna HTTP 404 se não existir
    //    }

    //    return ResponseEntity.ok(response); // Retorna HTTP 200 + DTO se existir
    //}

//    private final AccountValidationService accountValidationService;
//
//    @Operation(summary = "Buscar conta por ID (UUID) ou Número")
//    // 1. Ajustado o mapeamento para aceitar direto o ID na URL sem o "/validate"
//    @GetMapping("/{accountNumber}")
//    public ResponseEntity<AccountValidationResponseDTO> validateAccount(@PathVariable String accountNumber) {
//        try {
//            AccountValidationResponseDTO response = accountValidationService.validateByAccountNumber(accountNumber);
//
//            // 2. Proteção caso o serviço retorne null em vez de um DTO preenchido
//            if (response == null || !response.exists()) {
//                return ResponseEntity.notFound().build(); // Retorna 404
//            }
//
//            return ResponseEntity.ok(response); // Retorna 200 + DTO
//
//        } catch (IllegalArgumentException e) {
//            // Se o UUID/número fornecido for num formato inválido
//            return ResponseEntity.badRequest().build(); // Retorna 400
//        } catch (Exception e) {
//            // 3. Captura erros do Service (ex: erro de banco/integração) evitando o crash 500
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    private final AccountRepository accountRepository;

    @GetMapping("/{identifier}")
    public ResponseEntity<?> findAccount(@PathVariable String identifier) {
        Optional<Account> accountOptional;

        // Verifica se a string informada é um UUID válido
        if (isUuid(identifier)) {
            UUID uuid = UUID.fromString(identifier);

            // Tenta buscar primeiro pelo ID da Conta; se não achar, busca pelo ID do Usuário
            accountOptional = accountRepository.findById(uuid)
                    .or(() -> accountRepository.findByUserId(uuid));
        } else {
            // Se for uma string comum, busca diretamente pelo Número da Conta
            accountOptional = accountRepository.findByAccountNumber(identifier);
        }

        return accountOptional
                .map(account -> ResponseEntity.ok(Map.of(
                        "id", account.getId(),
                        "accountNumber", account.getAccountNumber(),
                        "userId", account.getUserId(),
                        "exists", true
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    private boolean isUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}