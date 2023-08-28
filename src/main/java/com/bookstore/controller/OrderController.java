package com.bookstore.controller;

import com.bookstore.dto.order.OrderRequestDto;
import com.bookstore.dto.order.OrderResponseDto;
import com.bookstore.dto.order.OrderUpdateRequestDto;
import com.bookstore.dto.orderitem.OrderItemResponseDto;
import com.bookstore.model.Status;
import com.bookstore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders management", description = "Endpoints for managing orders")
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create order", description = "Create order")
    public OrderResponseDto create(@RequestBody @Valid OrderRequestDto orderRequestDto) {
        return orderService.create(orderRequestDto);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Find all orders for current user", description = "Get list "
            + "of all current user's orders")
    public List<OrderResponseDto> findAll(Pageable pageable) {
        return orderService.findAllOrders(pageable);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update order status", description = "Update order status")
    public void updateOrderStatus(@PathVariable Long id,
                                  @RequestBody OrderUpdateRequestDto orderUpdateRequestDto) {
        orderService.updateOrderStatus(id,
                Status.valueOf(orderUpdateRequestDto.getStatus()));
    }

    @GetMapping("/{orderId}/items")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Find all items in order", description = "Get list of all "
            + "order items in order")
    public Set<OrderItemResponseDto> findAllOrderItems(@PathVariable Long orderId) {
        return orderService.findAllOrderItems(orderId);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Find item in order by id", description = "Get item from order by id")
    public OrderItemResponseDto findOrderItemById(@PathVariable Long orderId,
                                                  @PathVariable Long itemId) {
        return orderService.findOrderItemById(orderId, itemId);
    }
}
