package com.cotefacil.orders.service;

import com.cotefacil.orders.dto.OrderItemRequest;
import com.cotefacil.orders.dto.OrderItemResponse;

import java.util.List;

public interface IOrderItemService {

    List<OrderItemResponse> findByOrderId(Long orderId);

    OrderItemResponse addItem(Long orderId, OrderItemRequest request);
}
