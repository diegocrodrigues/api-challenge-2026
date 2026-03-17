package com.cotefacil.orders.service;

import com.cotefacil.orders.dto.OrderRequest;
import com.cotefacil.orders.dto.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderService {

    Page<OrderResponse> findAll(Pageable pageable);

    OrderResponse findById(Long id);

    OrderResponse create(OrderRequest request);

    OrderResponse update(Long id, OrderRequest request);

    void delete(Long id);
}
