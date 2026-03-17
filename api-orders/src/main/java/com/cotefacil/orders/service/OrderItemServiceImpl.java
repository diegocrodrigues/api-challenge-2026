package com.cotefacil.orders.service;

import com.cotefacil.orders.dto.OrderItemRequest;
import com.cotefacil.orders.dto.OrderItemResponse;
import com.cotefacil.orders.exception.OrderNotFoundException;
import com.cotefacil.orders.mapper.OrderItemMapper;
import com.cotefacil.orders.model.Order;
import com.cotefacil.orders.model.OrderItem;
import com.cotefacil.orders.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderItemServiceImpl implements IOrderItemService {

    private static final Logger log = LoggerFactory.getLogger(OrderItemServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemMapper orderItemMapper;

    public OrderItemServiceImpl(OrderRepository orderRepository,
                                OrderItemMapper orderItemMapper) {
        this.orderRepository = orderRepository;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemResponse> findByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return order.getItems().stream()
                .map(orderItemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderItemResponse addItem(Long orderId, OrderItemRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderItem item = orderItemMapper.toEntity(request);
        item.setOrder(order);
        order.getItems().add(item);
        order.setTotalAmount(recalcularTotal(order.getItems()));

        orderRepository.save(order);
        log.debug("Item adicionado ao pedido id={}", orderId);
        return orderItemMapper.toResponse(item);
    }

    private BigDecimal recalcularTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
