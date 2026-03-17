package com.cotefacil.orders.service;

import com.cotefacil.orders.dto.OrderRequest;
import com.cotefacil.orders.dto.OrderResponse;
import com.cotefacil.orders.exception.OrderNotFoundException;
import com.cotefacil.orders.mapper.OrderItemMapper;
import com.cotefacil.orders.mapper.OrderMapper;
import com.cotefacil.orders.model.Order;
import com.cotefacil.orders.model.OrderItem;
import com.cotefacil.orders.model.OrderStatus;
import com.cotefacil.orders.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements IOrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
        Order order = orderMapper.toEntity(request);
        order.setTotalAmount(calculateTotal(order.getItems()));
        Order saved = orderRepository.save(order);
        log.debug("Pedido criado com id={}", saved.getId());
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setStatus(Optional.ofNullable(request.status()).orElse(OrderStatus.PENDING));

        order.getItems().clear();
        List<OrderItem> novosItens = Optional.ofNullable(request.items())
                .orElse(Collections.emptyList())
                .stream()
                .map(itemRequest -> {
                    OrderItem item = orderItemMapper.toEntity(itemRequest);
                    item.setOrder(order);
                    return item;
                })
                .toList();
        order.getItems().addAll(novosItens);
        order.setTotalAmount(calculateTotal(order.getItems()));

        Order saved = orderRepository.save(order);
        log.debug("Pedido atualizado id={}", saved.getId());
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }
        orderRepository.deleteById(id);
        log.debug("Pedido deletado id={}", id);
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
