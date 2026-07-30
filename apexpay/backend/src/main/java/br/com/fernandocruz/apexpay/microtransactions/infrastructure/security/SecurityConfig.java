/* 1 - SessionCreationPolicy.STATELESS: a aplicação não guarda estado no servidor. Se a aplicação escalar para 10
 * contêineres Docker em um cluster Kubernetes, qualquer contêiner processa qualquer requisição sem precisar de "sessão
 * presa" (Sticky Sessions).
 *
 * 2 - BCryptPasswordEncoder: BCrypt é um algoritmo de hashing adaptativo com salt embutido. Ele protege as senhas
 * dos usuários caso o banco de dados seja comprometido, pois é computacionalmente custoso contra ataques de força bruta.
 */

package br.com.fernandocruz.apexpay.microtransactions.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Activa o suporte a CORS usando o bean corsConfigurationSource() definido abaixo
                .cors(Customizer.withDefaults())

                // 2. Desativa CSRF pois APIs REST com JWT não armazenam estado em Cookies/Sessões
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Configura os endpoints públicos e os que exigem autenticação
                .authorizeHttpRequests(auth -> auth
                        // Libera todas as requisições PREFLIGHT (OPTIONS) do navegador sem pedir JWT
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll() // Login e Cadastro liberados
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll() // Documentação Swagger (se usar)
                        .anyRequest().authenticated() // Todas as outras rotas exigem Token JWT válido
                )

                // 4. Define a política de sessão como STATELESS (A API não guarda sessão no servidor)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 5. Injeta o provedor de autenticação e o filtro JWT antes do filtro padrão do Spring
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Configuração explícita de CORS para permitir a comunicação com o Frontend React (Vite)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origem do seu frontend (Vite/React) usando pattern para aceitar credenciais
        configuration.setAllowedOriginPatterns(List.of("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Provedor de Autenticação responsável por ligar o nosso UserDetailsService com o BCrypt
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Gerenciador de Autenticação usado no fluxo de Login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Algoritmo de Criptografia de Senha oficial do mercado financeiro (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}