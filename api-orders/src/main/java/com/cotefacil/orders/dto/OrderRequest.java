package com.cotefacil.orders.dto;

import com.cotefacil.orders.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record OrderRequest(

        @NotBlank(message = "Nome do cliente é obrigatório")
        String customerName,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String customerEmail,

        OrderStatus status,

        @Valid
        List<OrderItemRequest> items
) {}
