package com.cotefacil.orders.controller;

import com.cotefacil.orders.dto.OrderItemRequest;
import com.cotefacil.orders.dto.OrderItemResponse;
import com.cotefacil.orders.service.IOrderItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/items")
public class OrderItemController {

    private final IOrderItemService orderItemService;

    public OrderItemController(IOrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @GetMapping
    public ResponseEntity<List<OrderItemResponse>> findByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderItemService.findByOrderId(orderId));
    }

    @PostMapping
    public ResponseEntity<OrderItemResponse> addItem(@PathVariable Long orderId,
                                                     @Valid @RequestBody OrderItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderItemService.addItem(orderId, request));
    }
}
