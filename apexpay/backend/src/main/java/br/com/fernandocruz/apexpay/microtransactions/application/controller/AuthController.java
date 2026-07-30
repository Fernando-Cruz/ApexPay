package br.com.fernandocruz.apexpay.microtransactions.application.controller;

import br.com.fernandocruz.apexpay.microtransactions.application.dto.AuthResponse;
import br.com.fernandocruz.apexpay.microtransactions.application.dto.LoginRequest;
import br.com.fernandocruz.apexpay.microtransactions.application.dto.RegisterRequest;
import br.com.fernandocruz.apexpay.microtransactions.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Autenticação & Contas", description = "Endpoints públicos para cadastro de clientes, abertura de contas " +
        "e geração de tokens JWT")
public class AuthController {

    private final AuthService authService;

     // Endpoint para cadastro de novos usuários e abertura automática de conta.
     // Retorna HTTP 201 Created em caso de sucesso.
     @Operation(summary = "Registrar novo cliente e abrir conta",
             description = "Cria um novo usuário com senha criptografada via BCrypt e gera automaticamente uma conta " +
                     "digital de 8 dígitos com saldo inicial zerado dentro da mesma transação.")
     @ApiResponses(value = {
             @ApiResponse(responseCode = "201", description = "Cliente cadastrado e conta digital criada com sucesso."),
             @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou e-mail já cadastrado."),
             @ApiResponse(responseCode = "500", description = "Erro interno no servidor.")
     })
     @PostMapping("/register")
     public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
         AuthResponse response = authService.register(request);
         return ResponseEntity.status(HttpStatus.CREATED).body(response);
     }

     // Endpoint para autenticação e geração de token JWT
     // Retorna HTTP 200 OK em caso de sucesso
     @Operation(summary = "Autenticar usuário",
             description = "Valida as credenciais do usuário (e-mail e senha) e retorna o Token JWT assinado para " +
                     "acesso às rotas protegidas.")
     @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso."),
             @ApiResponse(responseCode = "401", description = "Credenciais inválidas (e-mail ou senha incorretos)."),
             @ApiResponse(responseCode = "400", description = "Requisição malformada.")
     })
     @PostMapping("/login")
     public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
         AuthResponse response = authService.login(request);
         return ResponseEntity.ok(response);
     }
}
