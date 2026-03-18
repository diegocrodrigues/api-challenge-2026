package com.cotefacil.orders.service;

import com.cotefacil.orders.dto.OrderItemRequest;
import com.cotefacil.orders.dto.OrderItemResponse;
import com.cotefacil.orders.exception.OrderNotFoundException;
import com.cotefacil.orders.mapper.OrderItemMapper;
import com.cotefacil.orders.model.Order;
import com.cotefacil.orders.model.OrderItem;
import com.cotefacil.orders.model.OrderStatus;
import com.cotefacil.orders.repository.OrderItemRepository;
import com.cotefacil.orders.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @Test
    void findByOrderId_pedidoExistente_deveRetornarItens() {
        OrderItem item = criarItem(new BigDecimal("30.00"));
        Order order = criarOrder(1L, List.of(item));

        OrderItemResponse itemResponse = new OrderItemResponse(1L, "Produto", 3, new BigDecimal("10.00"), new BigDecimal("30.00"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemMapper.toResponse(item)).thenReturn(itemResponse);

        List<OrderItemResponse> resultado = orderItemService.findByOrderId(1L);

        assertEquals(1, resultado.size());
        assertEquals("Produto", resultado.get(0).productName());
    }

    @Test
    void findByOrderId_pedidoInexistente_deveLancarOrderNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderItemService.findByOrderId(99L));
    }

    @Test
    void addItem_pedidoInexistente_deveLancarOrderNotFoundException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        OrderItemRequest request = new OrderItemRequest("Produto", 1, new BigDecimal("10.00"));

        assertThrows(OrderNotFoundException.class, () -> orderItemService.addItem(99L, request));
    }

    @Test
    void addItem_deveAdicionarItemERecalcularTotalAmount() {
        Order order = criarOrder(1L, new ArrayList<>());
        order.setTotalAmount(BigDecimal.ZERO);

        OrderItem novoItem = criarItem(new BigDecimal("25.00"));
        OrderItemResponse itemResponse = new OrderItemResponse(1L, "Novo Produto", 1, new BigDecimal("25.00"), new BigDecimal("25.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemMapper.toEntity(any(OrderItemRequest.class))).thenReturn(novoItem);
        when(orderItemRepository.save(novoItem)).thenReturn(novoItem);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderItemMapper.toResponse(novoItem)).thenReturn(itemResponse);

        OrderItemRequest request = new OrderItemRequest("Novo Produto", 1, new BigDecimal("25.00"));
        OrderItemResponse resultado = orderItemService.addItem(1L, request);

        assertEquals("Novo Produto", resultado.productName());
        assertEquals(new BigDecimal("25.00"), order.getTotalAmount());
        assertEquals(1, order.getItems().size());
        verify(orderItemRepository).save(novoItem);
        verify(orderRepository).save(order);
    }

    @Test
    void addItem_pedidoComItensExistentes_deveAcumularTotalAmount() {
        OrderItem itemExistente = criarItem(new BigDecimal("50.00"));
        Order order = criarOrder(1L, new ArrayList<>(List.of(itemExistente)));
        order.setTotalAmount(new BigDecimal("50.00"));

        OrderItem novoItem = criarItem(new BigDecimal("30.00"));
        OrderItemResponse itemResponse = new OrderItemResponse(2L, "Produto 2", 1, new BigDecimal("30.00"), new BigDecimal("30.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemMapper.toEntity(any(OrderItemRequest.class))).thenReturn(novoItem);
        when(orderItemRepository.save(novoItem)).thenReturn(novoItem);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderItemMapper.toResponse(novoItem)).thenReturn(itemResponse);

        orderItemService.addItem(1L, new OrderItemRequest("Produto 2", 1, new BigDecimal("30.00")));

        assertEquals(new BigDecimal("80.00"), order.getTotalAmount());
    }

    private Order criarOrder(Long id, List<OrderItem> items) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerName("João");
        order.setCustomerEmail("joao@email.com");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setItems(items);
        return order;
    }

    private OrderItem criarItem(BigDecimal subtotal) {
        OrderItem item = new OrderItem();
        item.setProductName("Produto");
        item.setQuantity(1);
        item.setUnitPrice(subtotal);
        item.setSubtotal(subtotal);
        return item;
    }
}
