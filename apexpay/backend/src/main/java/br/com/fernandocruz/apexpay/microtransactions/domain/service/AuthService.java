package br.com.fernandocruz.apexpay.microtransactions.domain.service;

import br.com.fernandocruz.apexpay.microtransactions.application.dto.AuthResponse;
import br.com.fernandocruz.apexpay.microtransactions.application.dto.LoginRequest;
import br.com.fernandocruz.apexpay.microtransactions.application.dto.RegisterRequest;
import br.com.fernandocruz.apexpay.microtransactions.domain.model.Account;
import br.com.fernandocruz.apexpay.microtransactions.domain.model.User;
import br.com.fernandocruz.apexpay.microtransactions.domain.repository.AccountRepository;
import br.com.fernandocruz.apexpay.microtransactions.domain.repository.UserRepository;
import br.com.fernandocruz.apexpay.microtransactions.infrastructure.security.JwtTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    //  Efetua o registro do usuário e cria automaticamente uma conta digital vinculada.
    //  Toda a operação é executada dentro de uma única transação ACID no PostgreSQL.
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // 1. Valida se o e-mail já está cadastrado
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema.");
        }

        // 2. Cria e salva o usuário com a senha criptografada via BCrypt
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        // 3. Cria e salva a conta digital vinculada ao novo usuário
        Account account = Account.builder()
                .userId(savedUser.getId())
                .accountNumber(generateUniqueAccountNumber())
                .balance(BigDecimal.ZERO) // Toda nova conta inicia com saldo R$ 0.00
                .version(0L)
                .build();

        Account savedAccount = accountRepository.save(account);

        // 4. Gera o Token JWT para login imediato após o cadastro
        String jwtToken = jwtTokenService.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .accountNumber(savedAccount.getAccountNumber())
                .build();
    }

    //Autentica as credenciais do usuário e gera um novo Token JWT.
    public AuthResponse login(LoginRequest request) {
        // 1. Valida o e-mail e a senha utilizando o AuthenticationManager do Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Busca os dados atualizados do usuário e de sua conta
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha inválidos."));

        Account account = accountRepository.findAll().stream()
                .filter(acc -> acc.getUserId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Conta digital não encontrada para o usuário."));

        // 3. Emite o token JWT
        String jwtToken = jwtTokenService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .accountNumber(account.getAccountNumber())
                .build();
    }

    // Algoritmo simples para geração de número de conta único de 8 dígitos.
    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            int number = 10000000 + random.nextInt(90000000);
            accountNumber = String.valueOf(number);
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        return accountNumber;
    }
}
