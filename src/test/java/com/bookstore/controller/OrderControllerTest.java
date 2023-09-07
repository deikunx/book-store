package com.bookstore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.dto.order.OrderRequestDto;
import com.bookstore.dto.order.OrderResponseDto;
import com.bookstore.dto.order.OrderUpdateRequestDto;
import com.bookstore.dto.orderitem.OrderItemResponseDto;
import com.bookstore.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@WithMockUser(username = "admin@gmail.com", password = "123456", roles = {"ADMIN"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Create a new order")
    void create_WithValidRequest_ShouldCreateOrderAndReturnCreatedStatus() throws Exception {

        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setId(1L);
        responseDto.setUserId(1L);
        responseDto.setOrderDate(LocalDateTime.now());
        responseDto.setTotal(BigDecimal.valueOf(100));
        responseDto.setStatus("PENDING");

        OrderRequestDto requestDto = new OrderRequestDto()
                .setShippingAddress("Test Address");

        when(orderService.create(any(OrderRequestDto.class))).thenReturn(responseDto);

        MvcResult result = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        OrderResponseDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), OrderResponseDto.class);
        EqualsBuilder.reflectionEquals(responseDto, actual, "id");
    }

    @Test
    @DisplayName("Get all orders for current user")
    void findAll_ShouldReturnAllOrdersForCurrentUserAndOkStatus() throws Exception {
        List<OrderResponseDto> responseDtoList = new ArrayList<>();

        when(orderService.findAllOrders(any(Pageable.class))).thenReturn(responseDtoList);

        mockMvc.perform(get("/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Update order status")
    void updateOrderStatus_ShouldUpdateOrderStatusAndReturnOkStatus() throws Exception {
        Long orderId = 1L;
        String newStatus = "DELIVERED";

        OrderUpdateRequestDto updateRequestDto = new OrderUpdateRequestDto();
        updateRequestDto.setStatus(newStatus);

        mockMvc.perform(patch("/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk());

        verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq(updateRequestDto));
    }

    @Test
    @DisplayName("Find all order items by ordere")
    void findAllOrderItems_ShouldReturnListOfAllOrderItemsAndOkStatus() throws Exception {
        Long orderId = 1L;

        Set<OrderItemResponseDto> responseDtoSet = new HashSet<>();

        when(orderService.findAllOrderItems(eq(orderId))).thenReturn(responseDtoSet);

        mockMvc.perform(get("/orders/{orderId}/items", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Find order item by ID in order")
    void findOrderItemById_ShouldReturnOrderItemByIdAndReturnOkStatus() throws Exception {
        Long orderId = 1L;
        Long itemId = 2L;

        OrderItemResponseDto responseDto = new OrderItemResponseDto();

        when(orderService.findOrderItemById(eq(orderId), eq(itemId))).thenReturn(responseDto);

        mockMvc.perform(get("/orders/{orderId}/items/{itemId}", orderId, itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
