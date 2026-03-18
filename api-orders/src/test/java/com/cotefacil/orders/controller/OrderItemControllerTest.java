package com.cotefacil.orders.controller;

import com.cotefacil.orders.dto.OrderItemRequest;
import com.cotefacil.orders.dto.OrderItemResponse;
import com.cotefacil.orders.exception.GlobalExceptionHandler;
import com.cotefacil.orders.exception.OrderNotFoundException;
import com.cotefacil.orders.security.JwtAuthFilter;
import com.cotefacil.orders.security.JwtTokenValidator;
import com.cotefacil.orders.service.IOrderItemService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderItemController.class)
@Import(GlobalExceptionHandler.class)
class OrderItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IOrderItemService orderItemService;

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
    @WithMockUser
    void findByOrderId_pedidoExistente_deveRetornar200ComItens() throws Exception {
        OrderItemResponse item = new OrderItemResponse(1L, "Produto X", 2, new BigDecimal("10.00"), new BigDecimal("20.00"));
        when(orderItemService.findByOrderId(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/orders/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Produto X"))
                .andExpect(jsonPath("$[0].subtotal").value(20.00));
    }

    @Test
    @WithMockUser
    void findByOrderId_pedidoInexistente_deveRetornar404() throws Exception {
        when(orderItemService.findByOrderId(99L)).thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/api/orders/99/items"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByOrderId_semAutenticacao_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/orders/1/items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void addItem_requestValido_deveRetornar201() throws Exception {
        OrderItemRequest request = new OrderItemRequest("Produto Y", 3, new BigDecimal("15.00"));
        OrderItemResponse response = new OrderItemResponse(1L, "Produto Y", 3, new BigDecimal("15.00"), new BigDecimal("45.00"));

        when(orderItemService.addItem(eq(1L), any(OrderItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders/1/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Produto Y"))
                .andExpect(jsonPath("$.subtotal").value(45.00));
    }

    @Test
    @WithMockUser
    void addItem_requestInvalido_deveRetornar400() throws Exception {
        OrderItemRequest request = new OrderItemRequest("", 0, new BigDecimal("0.00"));

        mockMvc.perform(post("/api/orders/1/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
    }
}
