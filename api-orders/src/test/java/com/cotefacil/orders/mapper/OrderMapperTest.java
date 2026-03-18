package com.cotefacil.orders.mapper;

import com.cotefacil.orders.dto.OrderItemResponse;
import com.cotefacil.orders.dto.OrderRequest;
import com.cotefacil.orders.dto.OrderResponse;
import com.cotefacil.orders.model.Order;
import com.cotefacil.orders.model.OrderItem;
import com.cotefacil.orders.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

    @Mock
    private OrderItemMapper itemMapper;

    @InjectMocks
    private OrderMapper mapper;

    @Test
    void toEntity_deveMappearCamposBasicos() {
        OrderRequest request = new OrderRequest("João Silva", "joao@email.com", OrderStatus.CONFIRMED, null);

        Order order = mapper.toEntity(request);

        assertEquals("João Silva", order.getCustomerName());
        assertEquals("joao@email.com", order.getCustomerEmail());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(BigDecimal.ZERO, order.getTotalAmount());
        assertNotNull(order.getOrderDate());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void toEntity_statusNulo_deveDefaultParaPending() {
        OrderRequest request = new OrderRequest("João Silva", "joao@email.com", null, null);

        Order order = mapper.toEntity(request);

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void toEntity_comItens_deveMappearItens() {
        OrderItem itemMock = new OrderItem();
        when(itemMapper.toEntity(any())).thenReturn(itemMock);

        var itemRequest = new com.cotefacil.orders.dto.OrderItemRequest("Produto", 1, new BigDecimal("10.00"));
        OrderRequest request = new OrderRequest("Maria", "maria@email.com", null, List.of(itemRequest));

        Order order = mapper.toEntity(request);

        assertEquals(1, order.getItems().size());
        assertEquals(order, order.getItems().get(0).getOrder());
    }

    @Test
    void toResponse_deveMappearTodosOsCampos() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerName("João Silva");
        order.setCustomerEmail("joao@email.com");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setItems(List.of());

        OrderResponse response = mapper.toResponse(order);

        assertEquals(1L, response.id());
        assertEquals("João Silva", response.customerName());
        assertEquals("joao@email.com", response.customerEmail());
        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(new BigDecimal("100.00"), response.totalAmount());
        assertTrue(response.items().isEmpty());
    }

    @Test
    void toResponse_comItens_deveMappearItensViaItemMapper() {
        OrderItem item = new OrderItem();
        OrderItemResponse itemResponse = new OrderItemResponse(1L, "Produto", 1, new BigDecimal("10.00"), new BigDecimal("10.00"));
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        Order order = new Order();
        order.setId(1L);
        order.setCustomerName("Maria");
        order.setCustomerEmail("maria@email.com");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("10.00"));
        order.setItems(List.of(item));

        OrderResponse response = mapper.toResponse(order);

        assertEquals(1, response.items().size());
        assertEquals("Produto", response.items().get(0).productName());
    }
}
