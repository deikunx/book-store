package com.bookstore.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.dto.order.OrderRequestDto;
import com.bookstore.dto.order.OrderResponseDto;
import com.bookstore.dto.order.OrderUpdateRequestDto;
import com.bookstore.dto.orderitem.OrderItemResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
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
    @Sql(scripts = {
            "classpath:db/user/add-users-with-shopping-carts.sql",
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/user/delete-users-and-carts.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void create_WithValidRequest_ShouldCreateOrderAndReturnCreatedStatus() throws Exception {

        OrderResponseDto expected = new OrderResponseDto();
        expected.setId(1L);
        expected.setUserId(1L);
        expected.setOrderDate(LocalDateTime.now());
        expected.setTotal(BigDecimal.valueOf(100));
        expected.setStatus("PENDING");

        OrderRequestDto request = new OrderRequestDto()
                .setShippingAddress("Test Address");

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        OrderResponseDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), OrderResponseDto.class);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @DisplayName("Get all orders for current user")
    @Sql(scripts = {
            "classpath:db/user/add-users-with-shopping-carts.sql",
            "classpath:db/orders/add-default-orders.sql",
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/orders/remove-orders.sql",
            "classpath:db/user/delete-users-and-carts.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAll_ShouldReturnAllOrdersForCurrentUserAndOkStatus() throws Exception {
        List<OrderResponseDto> expected = new ArrayList<>();
        expected.add(new OrderResponseDto()
                .setOrderItems(Collections.emptySet())
                .setId(1L)
                .setStatus("PENDING")
                .setUserId(1L)
                .setTotal(BigDecimal.valueOf(199.99))
                .setOrderDate(LocalDateTime.of(2023, 9, 10, 12, 0)));

        MvcResult result = mockMvc.perform(get("/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        OrderResponseDto[] actual = objectMapper
                .readValue(result.getResponse().getContentAsByteArray(), OrderResponseDto[].class);
        Assertions.assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @DisplayName("Update order status")
    @Sql(scripts = {
            "classpath:db/user/add-users-with-shopping-carts.sql",
            "classpath:db/orders/add-default-orders.sql",
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/orders/remove-orders.sql",
            "classpath:db/user/delete-users-and-carts.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateOrderStatus_ShouldUpdateOrderStatusAndReturnOkStatus() throws Exception {
        Long orderId = 1L;
        String newStatus = "DELIVERED";

        OrderResponseDto expected = new OrderResponseDto()
                .setId(1L)
                .setOrderDate(LocalDateTime.of(2023, 9, 10, 12, 0))
                .setStatus(newStatus)
                .setTotal(BigDecimal.valueOf(199.99))
                .setOrderItems(Collections.emptySet())
                .setUserId(1L);

        OrderUpdateRequestDto updateRequestDto = new OrderUpdateRequestDto();
        updateRequestDto.setStatus(newStatus);

        String jsonRequest = objectMapper.writeValueAsString(updateRequestDto);

        MvcResult result = mockMvc.perform(patch("/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        OrderResponseDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), OrderResponseDto.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Find all order items by order")
    @Sql(scripts = {
            "classpath:db/books/add-three-default-books.sql",
            "classpath:db/user/add-users-with-shopping-carts.sql",
            "classpath:db/orders/add-default-orders.sql",
            "classpath:db/orders/add-order-items.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/orders/remove-order-items.sql",
            "classpath:db/books/remove-all-books.sql",
            "classpath:db/orders/remove-orders.sql",
            "classpath:db/user/delete-users-and-carts.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllOrderItems_ShouldReturnListOfAllOrderItemsAndOkStatus() throws Exception {
        Long orderId = 1L;

        Set<OrderItemResponseDto> expected = new HashSet<>();

        expected.add(new OrderItemResponseDto()
                .setId(1L)
                .setQuantity(1)
                .setBookId(1L));

        MvcResult result = mockMvc.perform(get("/orders/{orderId}/items", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        OrderItemResponseDto[] actual = objectMapper
                .readValue(result.getResponse().getContentAsByteArray(),
                        OrderItemResponseDto[].class);
        Assertions.assertEquals(expected,
                Arrays.stream(actual).collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("Find order item by ID in order")
    @Sql(scripts = {
            "classpath:db/books/add-three-default-books.sql",
            "classpath:db/user/add-users-with-shopping-carts.sql",
            "classpath:db/orders/add-default-orders.sql",
            "classpath:db/orders/add-order-items.sql"
    },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/orders/remove-order-items.sql",
            "classpath:db/books/remove-all-books.sql",
            "classpath:db/user/delete-users-and-carts.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findOrderItemById_ShouldReturnOrderItemByIdAndReturnOkStatus() throws Exception {
        Long orderId = 1L;
        Long itemId = 1L;

        OrderItemResponseDto expected = new OrderItemResponseDto()
                .setId(1L)
                .setQuantity(1)
                .setBookId(1L);

        MvcResult result = mockMvc.perform(get("/orders/{orderId}/items/{itemId}", orderId, itemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        OrderItemResponseDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), OrderItemResponseDto.class);
        Assertions.assertEquals(expected, actual);
    }
}
