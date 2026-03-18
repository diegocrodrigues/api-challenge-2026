package com.cotefacil.orders.controller;

import com.cotefacil.orders.dto.OrderRequest;
import com.cotefacil.orders.dto.OrderResponse;
import com.cotefacil.orders.exception.GlobalExceptionHandler;
import com.cotefacil.orders.exception.OrderNotFoundException;
import com.cotefacil.orders.model.OrderStatus;
import com.cotefacil.orders.security.JwtAuthFilter;
import com.cotefacil.orders.security.JwtTokenValidator;
import com.cotefacil.orders.service.IOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IOrderService orderService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtTokenValidator jwtTokenValidator;

    @BeforeEach
    void configurarFiltroPassThrough() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void findAll_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void findAll_autenticado_deveRetornar200ComPagina() throws Exception {
        OrderResponse response = criarResponse(1L);
        when(orderService.findAll(any())).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].customerName").value("João Silva"));
    }

    @Test
    @WithMockUser
    void findById_pedidoExistente_deveRetornar200() throws Exception {
        when(orderService.findById(1L)).thenReturn(criarResponse(1L));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void findById_pedidoInexistente_deveRetornar404() throws Exception {
        when(orderService.findById(99L)).thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pedido não encontrado com id: 99"));
    }

    @Test
    @WithMockUser
    void create_requestValido_deveRetornar201() throws Exception {
        OrderRequest request = new OrderRequest("João Silva", "joao@email.com", OrderStatus.PENDING, null);
        when(orderService.create(any(OrderRequest.class))).thenReturn(criarResponse(1L));

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void create_requestInvalido_deveRetornar400() throws Exception {
        OrderRequest request = new OrderRequest("", "email-invalido", null, null);

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @WithMockUser
    void update_pedidoExistente_deveRetornar200() throws Exception {
        OrderRequest request = new OrderRequest("João Silva", "joao@email.com", OrderStatus.CONFIRMED, null);
        when(orderService.update(eq(1L), any(OrderRequest.class))).thenReturn(criarResponse(1L));

        mockMvc.perform(put("/api/orders/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void delete_pedidoExistente_deveRetornar204() throws Exception {
        mockMvc.perform(delete("/api/orders/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void delete_pedidoInexistente_deveRetornar404() throws Exception {
        doThrow(new OrderNotFoundException(99L)).when(orderService).delete(99L);

        mockMvc.perform(delete("/api/orders/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    private OrderResponse criarResponse(Long id) {
        return new OrderResponse(id, "João Silva", "joao@email.com",
                LocalDateTime.now(), OrderStatus.PENDING, List.of(), new BigDecimal("100.00"));
    }
}
