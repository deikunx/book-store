package com.bookstore.service.impl;

import com.bookstore.dto.order.OrderRequestDto;
import com.bookstore.dto.order.OrderResponseDto;
import com.bookstore.dto.order.OrderUpdateRequestDto;
import com.bookstore.dto.orderitem.OrderItemResponseDto;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.OrderItemMapper;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.ShoppingCart;
import com.bookstore.model.Status;
import com.bookstore.model.User;
import com.bookstore.repository.order.OrderRepository;
import com.bookstore.repository.shoppingcart.ShoppingCartRepository;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public OrderResponseDto create(OrderRequestDto orderRequestDto) {
        ShoppingCart shoppingCart = getShoppingCartForCurrentUser();

        Order order = new Order();
        order.setShippingAddress(orderRequestDto.getShippingAddress());
        order.setUser(shoppingCart.getUser());
        order.setStatus(Status.PENDING);

        Set<OrderItem> orderItems = shoppingCart.getCartItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setBook(cartItem.getBook());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setOrder(order);
                    orderItem.setPrice(cartItem.getBook().getPrice()
                            .multiply(new BigDecimal(cartItem.getQuantity())));
                    return orderItem;
                })
                .collect(Collectors.toSet());

        order.setOrderItems(orderItems);
        order.setOrderDate(LocalDateTime.now());
        BigDecimal total = orderItems.stream()
                .map(orderItem -> orderItem.getBook().getPrice()
                        .multiply(new BigDecimal(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public List<OrderResponseDto> findAllOrders(Pageable pageable) {
        return orderRepository.findAllOrders(pageable).stream()
                .map(orderMapper::toDto).toList();
    }

    @Override
    public OrderUpdateRequestDto updateOrderStatus(Long orderId, OrderUpdateRequestDto orderDto) {
        Order orderFromDb = orderRepository.findById(orderId).orElseThrow(
                () -> new EntityNotFoundException("Can't find order by id " + orderId));
        Order model = orderMapper.toModel(orderDto);
        orderFromDb.setStatus(model.getStatus());
        orderRepository.save(orderFromDb);
        return orderMapper.toUpdateDto(orderFromDb);
    }

    @Override
    public Set<OrderItemResponseDto> findAllOrderItems(Long orderId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find "
                                + "order with id " + orderId));
        return order.getOrderItems().stream()
                .map(orderItemMapper::toDto)
                .collect(Collectors.toSet());
    }

    public OrderItemResponseDto findOrderItemById(Long orderId, Long itemId) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find "
                                + "order with id " + orderId));
        return order.getOrderItems().stream()
                .filter(o -> o.getId().equals(itemId))
                .findFirst()
                .map(orderItemMapper::toDto)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find "
                                + "item with id " + itemId + " in order with id " + orderId));
    }

    private User getCurrentUser() {
        return userService
                .getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("Can't get current user"));
    }

    private ShoppingCart getShoppingCartForCurrentUser() {
        User currentUser = getCurrentUser();
        return shoppingCartRepository
                .findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Can't "
                        + "get shopping cart with id " + currentUser.getId()));
    }
}
