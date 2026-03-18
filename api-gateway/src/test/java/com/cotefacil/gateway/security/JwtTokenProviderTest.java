package com.cotefacil.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET);
    }

    @Test
    void generateToken_deveGerarTokenNaoNulo() {
        String token = jwtTokenProvider.generateToken("usuario", List.of("USER"));

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void validateToken_tokenValido_deveRetornarTrue() {
        String token = jwtTokenProvider.generateToken("usuario", List.of("USER"));

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_tokenInvalido_deveRetornarFalse() {
        assertFalse(jwtTokenProvider.validateToken("token.invalido.aqui"));
    }

    @Test
    void validateToken_tokenVazio_deveRetornarFalse() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void validateToken_tokenNulo_deveRetornarFalse() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void getUsernameFromToken_deveRetornarUsernameCorreto() {
        String token = jwtTokenProvider.generateToken("usuario_teste", List.of("USER"));

        assertEquals("usuario_teste", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void getRolesFromToken_deveRetornarRolesCorretas() {
        String token = jwtTokenProvider.generateToken("usuario", List.of("USER", "ADMIN"));

        List<String> roles = jwtTokenProvider.getRolesFromToken(token);

        assertEquals(2, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));
    }

    @Test
    void getExpirationMs_deveRetornarUmaHoraEmMs() {
        assertEquals(3_600_000L, jwtTokenProvider.getExpirationMs());
    }
}
