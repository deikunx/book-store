package com.bookstore.repository.order;

import com.bookstore.model.Order;
import com.bookstore.model.Status;
import com.bookstore.model.User;
import com.bookstore.repository.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import liquibase.pro.packaged.U;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Update status of a certain order")
    void updateOrderByStatus_ShouldUpdateOrderStatusInOrder() {
        User user = createUser("test234@gmail.com");
        Order order = createOrder(user);
        orderRepository.updateOrderByStatus(order.getId(), Status.COMPLETED);

        Order updatedOrder = orderRepository.findById(order.getId()).orElse(null);

        assertNotNull(updatedOrder);
        assertEquals(Status.COMPLETED, updatedOrder.getStatus());
    }

    @Test
    @DisplayName("Find all all orders with pages")
    void findAllOrders_ShouldReturnListOfAllOrdersWithPages() {
        User user1 = createUser("test1@gmail.com");
        User user2 = createUser("test2@gmail.com");

        Order order1 = createOrder(user1);
        Order order2 = createOrder(user2);

        orderRepository.save(order1);
        orderRepository.save(order2);

        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = orderRepository.findAllOrders(pageable);

        assertEquals(2, orders.size());
    }

    @Test
    @DisplayName("Find order by id")
    void findById_ShouldReturnOrderById() {
        User user1 = createUser("test3@gmail.com");

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress("Test");
        order.setStatus(Status.PENDING);
        order.setTotal(BigDecimal.valueOf(100));
        order.setUser(user1);
        orderRepository.save(order);

        Optional<Order> foundOrder = orderRepository.findById(order.getId());

        assertTrue(foundOrder.isPresent());
        assertEquals(order.getId(), foundOrder.get().getId());
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