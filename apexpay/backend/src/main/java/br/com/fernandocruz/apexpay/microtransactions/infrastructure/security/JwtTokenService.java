package br.com.fernandocruz.apexpay.microtransactions.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtTokenService {

    // A assinatura padrão deve ser forte e configurada externamente (ex: no application.yml)
    @Value("${api.security.token.secret:sua-chave-secreta-super-segura-e-longa-com-mais-de-256-bits-para-o-banco-123}")
    private String secret;

    @Value("${api.security.token.expiration-ms:86400000}") // Padrão de 24 horas em milissegundos
    private Long expirationMs;


    //Gera uma chave segura para a assinatura HMAC baseada na string configurada.
    private SecretKey getSigningKey() {
        byte[] keyBytes = this.secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    // Extrai o e-mail (username) de dentro do Token JWT.
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    // Extrai a data de expiração do Token JWT.
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    // Gera um token JWT com base no e-mail do usuário e parâmetros adicionais (claims).
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // Valida se o token pertence ao usuário correspondente e se ainda está no prazo de validade.
    public Boolean validateToken(String token, String userEmail) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userEmail) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            // Se o token for inválido, adulterado ou estiver expirado, a validação falhará de forma segura
            return false;
        }
    }
}