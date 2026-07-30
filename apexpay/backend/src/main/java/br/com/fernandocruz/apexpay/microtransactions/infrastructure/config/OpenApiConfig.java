package br.com.fernandocruz.apexpay.microtransactions.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(){
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // 1. Detalhamento Geral e Comercial da API
                .info(new Info()
                        .title("ApexPay - Microtransactions API")
                        .version("v1.0.0")
                        .description("""
                                ### Core Transacional de Alta Disponibilidade e Consistência
                                
                                Esta API é o motor central da plataforma de pagamento instantâneo ApexPay. 
                                Projetada sob os princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**.
                                
                                #### Recursos e Garantias Técnicas:
                                * **Autenticação Stateless**: Segurança robusta baseada em tokens JWT assinados via algoritmo HMAC-SHA256.
                                * **Garantia de Idempotência**: Suporte a cabeçalhos customizados `Idempotency-Key` para evitar duplo processamento de débitos sob oscilações de rede.
                                * **Integridade Transacional (ACID)**: Controle rigoroso de concorrência usando **Lock Pessimista (`SELECT ... FOR UPDATE`)** e ordenação determinística de recursos para eliminação total de Deadlocks.
                                * **Resiliência e Erros Padronizados**: Tratamento de exceções aderente à especificação internacional **RFC 7807 (Problem Details)**.
                                """)
                        .contact( new Contact()
                                .name("Fernando Cruz - Engenharia de Software")
                                .email("suporte@apexpay.com.br")
                                .url("https://github.com/fernandocruz"))
                        .license(new License()
                                .name("Proprietary License - ApexPay")
                                .url("https://apexpay.com.br/terms")))
                // 2. Ambientes de Execução Registrados
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento Local (Docker / Spring Boot)")
                ))

                // 3. Exigência Global de Segurança para os Endpoints no Swagger
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                // 4. Configuração do Botão "Authorize" para Testes via JWT Bearer Token
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                Insira o Token JWT gerado no endpoint de `/api/v1/auth/login` ou `/api/v1/auth/register`. 
                                                
                                                Exemplo: `eyJhbGciOiJIUzI1NiJ9...`
                                                """)
                        ));

    }
}
