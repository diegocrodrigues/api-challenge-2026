package com.cotefacil.gateway.service;

import com.cotefacil.gateway.dto.LoginRequest;
import com.cotefacil.gateway.dto.LoginResponse;
import com.cotefacil.gateway.model.User;
import com.cotefacil.gateway.repository.UserRepository;
import com.cotefacil.gateway.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_credenciaisValidas_deveRetornarLoginResponse() {
        User user = criarUser("usuario", "hash");
        when(userRepository.findByUsername("usuario")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);
        when(jwtTokenProvider.generateToken("usuario", List.of("USER"))).thenReturn("token.jwt.gerado");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3_600_000L);

        LoginResponse response = authService.login(new LoginRequest("usuario", "senha123"));

        assertNotNull(response);
        assertEquals("token.jwt.gerado", response.token());
        assertEquals("Bearer", response.type());
        assertEquals(3_600_000L, response.expiresIn());
    }

    @Test
    void login_usuarioInexistente_deveLancarBadCredentialsException() {
        when(userRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("inexistente", "senha")));
    }

    @Test
    void login_senhaErrada_deveLancarBadCredentialsException() {
        User user = criarUser("usuario", "hash");
        when(userRepository.findByUsername("usuario")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaErrada", "hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("usuario", "senhaErrada")));
    }

    private User criarUser(String username, String passwordHash) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(passwordHash);
        user.setRoles(List.of("USER"));
        return user;
    }
}
