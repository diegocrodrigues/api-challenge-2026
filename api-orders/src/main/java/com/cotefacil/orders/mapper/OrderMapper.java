package com.cotefacil.orders.mapper;

import com.cotefacil.orders.dto.OrderRequest;
import com.cotefacil.orders.dto.OrderResponse;
import com.cotefacil.orders.model.Order;
import com.cotefacil.orders.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class OrderMapper {

    private final OrderItemMapper itemMapper;

    public OrderMapper(OrderItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public Order toEntity(OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Optional.ofNullable(request.status()).orElse(OrderStatus.PENDING));
        order.setTotalAmount(BigDecimal.ZERO);

        List<com.cotefacil.orders.model.OrderItem> items = Optional.ofNullable(request.items())
                .orElse(Collections.emptyList())
                .stream()
                .map(itemRequest -> {
                    var item = itemMapper.toEntity(itemRequest);
                    item.setOrder(order);
                    return item;
                })
                .toList();

        order.setItems(items);
        return order;
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getOrderDate(),
                order.getStatus(),
                order.getItems().stream().map(itemMapper::toResponse).toList(),
                order.getTotalAmount()
        );
    }
}
