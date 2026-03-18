package com.cotefacil.orders.service;

import com.cotefacil.orders.dto.OrderRequest;
import com.cotefacil.orders.dto.OrderResponse;
import com.cotefacil.orders.exception.OrderNotFoundException;
import com.cotefacil.orders.mapper.OrderItemMapper;
import com.cotefacil.orders.mapper.OrderMapper;
import com.cotefacil.orders.model.Order;
import com.cotefacil.orders.model.OrderItem;
import com.cotefacil.orders.model.OrderStatus;
import com.cotefacil.orders.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void findAll_deveRetornarPaginaDeOrders() {
        Order order = criarOrder(1L);
        OrderResponse response = criarOrderResponse(1L);
        Page<Order> page = new PageImpl<>(List.of(order));

        when(orderRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(orderMapper.toResponse(order)).thenReturn(response);

        Page<OrderResponse> resultado = orderService.findAll(PageRequest.of(0, 10));

        assertEquals(1, resultado.getTotalElements());
        assertEquals(1L, resultado.getContent().get(0).id());
    }

    @Test
    void findById_pedidoExistente_deveRetornarResponse() {
        Order order = criarOrder(1L);
        OrderResponse response = criarOrderResponse(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse resultado = orderService.findById(1L);

        assertEquals(1L, resultado.id());
    }

    @Test
    void findById_pedidoInexistente_deveLancarOrderNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.findById(99L));
    }

    @Test
    void create_deveCalcularTotalAmountEPersistir() {
        OrderItem item = new OrderItem();
        item.setSubtotal(new BigDecimal("50.00"));

        Order order = criarOrder(null);
        order.setItems(List.of(item));

        OrderResponse response = criarOrderResponse(1L);

        when(orderMapper.toEntity(any(OrderRequest.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderRequest request = new OrderRequest("João", "joao@email.com", OrderStatus.PENDING, null);
        OrderResponse resultado = orderService.create(request);

        assertNotNull(resultado);
        verify(orderRepository).save(order);
        assertEquals(new BigDecimal("50.00"), order.getTotalAmount());
    }

    @Test
    void delete_pedidoExistente_deveChamarDeleteById() {
        when(orderRepository.existsById(1L)).thenReturn(true);

        orderService.delete(1L);

        verify(orderRepository).deleteById(1L);
    }

    @Test
    void delete_pedidoInexistente_deveLancarOrderNotFoundException() {
        when(orderRepository.existsById(99L)).thenReturn(false);

        assertThrows(OrderNotFoundException.class, () -> orderService.delete(99L));
    }

    @Test
    void update_pedidoInexistente_deveLancarOrderNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        OrderRequest request = new OrderRequest("João", "joao@email.com", OrderStatus.PENDING, null);

        assertThrows(OrderNotFoundException.class, () -> orderService.update(99L, request));
    }

    @Test
    void update_pedidoExistente_deveAtualizarCamposERecalcularTotal() {
        Order order = criarOrder(1L);
        order.setItems(new java.util.ArrayList<>());

        OrderResponse response = criarOrderResponse(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderRequest request = new OrderRequest("Novo Nome", "novo@email.com", OrderStatus.CONFIRMED, null);
        orderService.update(1L, request);

        assertEquals("Novo Nome", order.getCustomerName());
        assertEquals("novo@email.com", order.getCustomerEmail());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    private Order criarOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerName("João");
        order.setCustomerEmail("joao@email.com");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setItems(new java.util.ArrayList<>());
        return order;
    }

    private OrderResponse criarOrderResponse(Long id) {
        return new OrderResponse(id, "João", "joao@email.com", LocalDateTime.now(),
                OrderStatus.PENDING, List.of(), BigDecimal.ZERO);
    }
}
