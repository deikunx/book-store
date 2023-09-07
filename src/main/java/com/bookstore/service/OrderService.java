package com.bookstore.service;

import com.bookstore.dto.order.OrderRequestDto;
import com.bookstore.dto.order.OrderResponseDto;
import com.bookstore.dto.order.OrderUpdateRequestDto;
import com.bookstore.dto.orderitem.OrderItemResponseDto;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponseDto create(OrderRequestDto orderRequestDto);

    List<OrderResponseDto> findAllOrders(Pageable pageable);

    OrderUpdateRequestDto updateOrderStatus(
            Long orderId,
            OrderUpdateRequestDto
            orderUpdateRequestDto
            );

    Set<OrderItemResponseDto> findAllOrderItems(Long orderId);

    OrderItemResponseDto findOrderItemById(Long orderId, Long itemId);
}
