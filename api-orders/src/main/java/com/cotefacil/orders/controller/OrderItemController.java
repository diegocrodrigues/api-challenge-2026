package com.cotefacil.orders.controller;

import com.cotefacil.orders.dto.OrderItemRequest;
import com.cotefacil.orders.dto.OrderItemResponse;
import com.cotefacil.orders.service.IOrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Itens de Pedido", description = "Gerenciamento de itens vinculados a um pedido")
@RestController
@RequestMapping("/api/orders/{orderId}/items")
public class OrderItemController {

    private final IOrderItemService orderItemService;

    public OrderItemController(IOrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @Operation(summary = "Lista itens de um pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itens retornados"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @GetMapping
    public ResponseEntity<List<OrderItemResponse>> findByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderItemService.findByOrderId(orderId));
    }

    @Operation(summary = "Adiciona item ao pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item adicionado — totalAmount recalculado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @PostMapping
    public ResponseEntity<OrderItemResponse> addItem(@PathVariable Long orderId,
                                                     @Valid @RequestBody OrderItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderItemService.addItem(orderId, request));
    }
}
