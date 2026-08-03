package br.com.fernandocruz.apexpay.microtransactions.infrastructure.exception;

import br.com.fernandocruz.apexpay.microtransactions.application.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Trata erros de regras de negócio (ex: Saldo insuficiente, conta não encontrada, e-mail duplicado)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .title("Regra de Negócio Violada")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    //Trata erros de estado e concorrência (ex: Idempotência duplicada, falha ao aplicar Lock)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .title("Conflito de Processamento")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    //* Trata falhas de validação de DTOs anotados com @Valid (@NotBlank, @DecimalMin, @Email, etc)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .title("Erro de Validação de Dados")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Um ou mais campos da requisição são inválidos.")
                .fieldErrors(errors)
                .timestamp(OffsetDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    //Trata falhas de autenticação (E-mail/Senha incorretos)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .title("Falha de Autenticação")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail("E-mail ou senha inválidos.")
                .timestamp(OffsetDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    //Trata exceções não mapeadas/inesperadas (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .title("Erro Interno do Servidor")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde.")
                .timestamp(OffsetDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
