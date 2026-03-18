package com.cotefacil.orders;

import com.cotefacil.orders.dto.OrderItemRequest;
import com.cotefacil.orders.dto.OrderItemResponse;
import com.cotefacil.orders.dto.OrderRequest;
import com.cotefacil.orders.dto.OrderResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
        headers.setBearerAuth(generateToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void fluxoCompleto_criarPedido_adicionarItem_verificarTotal_deletar() {
        // 1. Criar pedido
        OrderRequest orderRequest = new OrderRequest("João Silva", "joao@email.com", null, List.of());

        ResponseEntity<OrderResponse> createResponse = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(orderRequest, headers),
                OrderResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        Long orderId = createResponse.getBody().id();
        assertThat(createResponse.getBody().totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);

        // 2. Adicionar item (3 unidades x R$ 15,00 = R$ 45,00)
        OrderItemRequest itemRequest = new OrderItemRequest("Produto A", 3, new BigDecimal("15.00"));

        ResponseEntity<OrderItemResponse> itemResponse = restTemplate.exchange(
                "/api/orders/" + orderId + "/items", HttpMethod.POST,
                new HttpEntity<>(itemRequest, headers),
                OrderItemResponse.class
        );

        assertThat(itemResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(itemResponse.getBody()).isNotNull();
        assertThat(itemResponse.getBody().subtotal()).isEqualByComparingTo(new BigDecimal("45.00"));

        // 3. Verificar totalAmount atualizado no pedido
        ResponseEntity<OrderResponse> getResponse = restTemplate.exchange(
                "/api/orders/" + orderId, HttpMethod.GET,
                new HttpEntity<>(headers),
                OrderResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().totalAmount()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(getResponse.getBody().items()).hasSize(1);

        // 4. Deletar pedido
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/orders/" + orderId, HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 5. Confirmar que foi removido
        ResponseEntity<String> notFoundResponse = restTemplate.exchange(
                "/api/orders/" + orderId, HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(notFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void semAutorizacao_deveRetornar401() {
        HttpHeaders noAuth = new HttpHeaders();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders", HttpMethod.GET,
                new HttpEntity<>(noAuth),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void tokenInvalido_deveRetornar401() {
        HttpHeaders badAuth = new HttpHeaders();
        badAuth.set(HttpHeaders.AUTHORIZATION, "Bearer token.invalido.aqui");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders", HttpMethod.GET,
                new HttpEntity<>(badAuth),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void criarPedido_comDadosInvalidos_deveRetornar400() {
        OrderRequest invalido = new OrderRequest("", "email-invalido", null, List.of());

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(invalido, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void buscarPedidoInexistente_deveRetornar404() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders/999999", HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String generateToken() {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("usuario-teste")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
    }
}
