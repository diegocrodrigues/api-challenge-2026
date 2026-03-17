package com.cotefacil.orders.dto;

import com.cotefacil.orders.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerName,
        String customerEmail,
        LocalDateTime orderDate,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal totalAmount
) {}
