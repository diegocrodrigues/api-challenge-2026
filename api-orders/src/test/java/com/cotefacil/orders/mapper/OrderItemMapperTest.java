package com.cotefacil.orders.mapper;

import com.cotefacil.orders.dto.OrderItemRequest;
import com.cotefacil.orders.dto.OrderItemResponse;
import com.cotefacil.orders.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderItemMapperTest {

    private OrderItemMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OrderItemMapper();
    }

    @Test
    void toEntity_deveMappearTodosOsCampos() {
        OrderItemRequest request = new OrderItemRequest("Produto X", 3, new BigDecimal("15.00"));

        OrderItem item = mapper.toEntity(request);

        assertEquals("Produto X", item.getProductName());
        assertEquals(3, item.getQuantity());
        assertEquals(new BigDecimal("15.00"), item.getUnitPrice());
        assertEquals(new BigDecimal("45.00"), item.getSubtotal());
    }

    @Test
    void toEntity_deveCalcularSubtotalCorretamente() {
        OrderItemRequest request = new OrderItemRequest("Produto Y", 5, new BigDecimal("10.50"));

        OrderItem item = mapper.toEntity(request);

        assertEquals(new BigDecimal("52.50"), item.getSubtotal());
    }

    @Test
    void toResponse_deveMappearTodosOsCampos() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductName("Produto Z");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("20.00"));
        item.setSubtotal(new BigDecimal("40.00"));

        OrderItemResponse response = mapper.toResponse(item);

        assertEquals(1L, response.id());
        assertEquals("Produto Z", response.productName());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("20.00"), response.unitPrice());
        assertEquals(new BigDecimal("40.00"), response.subtotal());
    }

    @Test
    void toEntity_semOrdemAssociada_deveRetornarItemSemOrder() {
        OrderItemRequest request = new OrderItemRequest("Produto", 1, new BigDecimal("1.00"));

        OrderItem item = mapper.toEntity(request);

        assertNotNull(item);
        assertEquals(null, item.getOrder());
    }
}
