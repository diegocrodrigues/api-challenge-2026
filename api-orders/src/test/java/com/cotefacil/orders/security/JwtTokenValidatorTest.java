package com.cotefacil.orders.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenValidatorTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtTokenValidator validator;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        validator = new JwtTokenValidator(SECRET);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void validateToken_tokenValido_deveRetornarTrue() {
        String token = gerarToken("usuario", 3600000);

        assertTrue(validator.validateToken(token));
    }

    @Test
    void validateToken_tokenExpirado_deveRetornarFalse() {
        String token = gerarToken("usuario", -1000);

        assertFalse(validator.validateToken(token));
    }

    @Test
    void validateToken_tokenMalformado_deveRetornarFalse() {
        assertFalse(validator.validateToken("token.invalido.aqui"));
    }

    @Test
    void validateToken_tokenVazio_deveRetornarFalse() {
        assertFalse(validator.validateToken(""));
    }

    @Test
    void validateToken_tokenComSecretDiferente_deveRetornarFalse() {
        SecretKey outraKey = Keys.hmacShaKeyFor("OutraSecretCompletamenteDiferentaAqui12345".getBytes(StandardCharsets.UTF_8));
        String tokenComOutraSecret = Jwts.builder()
                .subject("usuario")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(outraKey)
                .compact();

        assertFalse(validator.validateToken(tokenComOutraSecret));
    }

    @Test
    void getUsernameFromToken_deveRetornarUsernameCorreto() {
        String token = gerarToken("usuario_teste", 3600000);

        assertEquals("usuario_teste", validator.getUsernameFromToken(token));
    }

    private String gerarToken(String username, long expirationMillis) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(secretKey)
                .compact();
    }
}
