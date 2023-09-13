package com.bookstore.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.dto.order.OrderRequestDto;
import com.bookstore.dto.order.OrderResponseDto;
import com.bookstore.dto.order.OrderUpdateRequestDto;
import com.bookstore.dto.orderitem.OrderItemResponseDto;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.OrderItemMapper;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.ShoppingCart;
import com.bookstore.model.Status;
import com.bookstore.model.User;
import com.bookstore.repository.order.OrderRepository;
import com.bookstore.repository.shoppingcart.ShoppingCartRepository;
import com.bookstore.repository.user.UserRepository;
import com.bookstore.service.impl.OrderServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private UserService userService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Test
    @DisplayName("Verify create() method works")
    void create_SuccessfulCreate() {
        User user = new User();
        user.setId(1L);

        Book book = new Book();
        book.setId(1L);
        book.setPrice(BigDecimal.valueOf(10));

        when(userService.getCurrentUser()).thenReturn(Optional.of(user));

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUser(user);

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setBook(book);
        cartItem.setQuantity(2);
        cartItem.setShoppingCart(shoppingCart);
        shoppingCart.setCartItems(Set.of(cartItem));

        when(shoppingCartRepository.findById(1L)).thenReturn(Optional.of(shoppingCart));

        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(Status.PENDING);

        Set<OrderItem> orderItems = shoppingCart.getCartItems().stream()
                .map(c -> {
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

        OrderResponseDto orderResponseDto = new OrderResponseDto();
        orderResponseDto.setStatus(String.valueOf(Status.PENDING));

        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderRequestDto orderRequestDto = new OrderRequestDto();

        OrderResponseDto createdOrder = orderService.create(orderRequestDto);

        assertNotNull(createdOrder);
        assertEquals(String.valueOf(Status.PENDING), createdOrder.getStatus());
        verify(shoppingCartRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    public void updateOrderStatus_WithInvalidId_ThrowsException() {
        Long orderId = 1L;
        OrderUpdateRequestDto orderDto = new OrderUpdateRequestDto();
        orderDto.setStatus("Updated Status");

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            orderService.updateOrderStatus(orderId, orderDto);
        });
    }

    @Test
    @DisplayName("Verify findAllOrders() method works")
    void findAllOrders_ShouldReturnListOfOrders() {
        Pageable pageable = Pageable.unpaged();
        Order order1 = new Order();
        Order order2 = new Order();
        List<Order> orders = List.of(order1, order2);
        when(orderRepository.findAllOrders(pageable)).thenReturn(orders);

        List<OrderResponseDto> result = orderService.findAllOrders(pageable);

        verify(orderRepository).findAllOrders(pageable);

        assertEquals(orders.size(), result.size());
    }

    @Test
    @DisplayName("Verify updateOrderStatus() method works")
    void updateOrderStatus_SuccessfulUpdate() {
        Long orderId = 1L;
        OrderUpdateRequestDto orderDto = new OrderUpdateRequestDto();
        orderDto.setStatus(String.valueOf(Status.COMPLETED));

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(Status.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderMapper.toModel(orderDto)).thenReturn(existingOrder);
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);
        when(orderMapper.toUpdateDto(existingOrder)).thenReturn(orderDto);

        final OrderUpdateRequestDto result = orderService.updateOrderStatus(orderId, orderDto);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(existingOrder);
        verify(orderMapper).toModel(orderDto);
        verify(orderMapper).toUpdateDto(existingOrder);
        assertEquals(orderDto, result);
    }

    @Test
    @DisplayName("Verify findAllOrderItems() method works")
    void findAllOrderItems_WithValidOrderId_ShouldReturnListOfAllOrderItems() {
        Long orderId = 1L;

        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(1L);

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setId(2L);

        Order order = new Order();
        order.setId(orderId);
        order.setOrderItems(Set.of(orderItem1, orderItem2));

        OrderItemResponseDto orderItemResponseDto1 = new OrderItemResponseDto();
        OrderItemResponseDto orderItemResponseDto2 = new OrderItemResponseDto();
        orderItemResponseDto1.setId(1L);
        orderItemResponseDto2.setId(2L);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemMapper.toDto(orderItem1)).thenReturn(orderItemResponseDto1);
        when(orderItemMapper.toDto(orderItem2)).thenReturn(orderItemResponseDto2);

        Set<OrderItemResponseDto> result = orderService.findAllOrderItems(orderId);
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).findById(orderId);
        verify(orderItemMapper, times(2)).toDto(any(OrderItem.class));
    }

    @Test
    @DisplayName("Verify findOrderItemById() method works")
    void findOrderItemById_WithValidOrderId_ShouldReturnOrderItem() {
        Long orderId = 1L;
        Long itemId = 2L;

        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);

        Order order = new Order();
        order.setId(orderId);

        Set<OrderItem> orderItems = new HashSet<>();
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemMapper.toDto(orderItem)).thenReturn(new OrderItemResponseDto());

        OrderItemResponseDto result = orderService.findOrderItemById(orderId, itemId);

        assertNotNull(result);
        verify(orderRepository).findById(orderId);
        verify(orderItemMapper).toDto(orderItem);
    }

    @Test
    @DisplayName("Verify findOrderItemById() method throws exception with invalid order id")
    void findOrderItem_WithInvalidOrderId_ShouldThrowEntityNotFoundException() {
        Long orderId = 1L;
        Long itemId = 2L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            orderService.findOrderItemById(orderId, itemId);
        });
    }

    @Test
    @DisplayName("Verify findOrderItemById() method throws exception with invalid order item id")
    void findOrderItem_WithInvalidOrderItemId_ShouldThrowEntityNotFoundException() {
        Long orderId = 1L;

        Order order = new Order();
        order.setId(orderId);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(2L);
        Set<OrderItem> orderItems = Set.of(orderItem);

        order.setOrderItems(orderItems);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Long itemId = 2L;
        assertThrows(EntityNotFoundException.class, () -> {
            orderService.findOrderItemById(orderId, itemId);
        });
    }

    @Test
    @DisplayName("Verify findAllOrderItems() method throws exception with invalid order id")
    public void findAllOrderItems_WithInvalidOrderId_ShouldThrowEntityNotFoundException() {
        Long invalidOrderId = 123L;

        when(orderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            orderService.findAllOrderItems(invalidOrderId);
        });
    }

    private User createUser(String email) {
        User user = new User();
        user.setFirstName("test");
        user.setLastName("test");
        user.setEmail(email);
        user.setPassword("141");
        return userRepository.save(user);
    }

    private Order createOrder(User user) {
        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress("Test");
        order.setStatus(Status.PENDING);
        order.setTotal(BigDecimal.valueOf(100));
        order.setUser(user);
        return orderRepository.save(order);
    }
}
