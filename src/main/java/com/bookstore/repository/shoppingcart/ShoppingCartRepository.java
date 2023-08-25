package com.bookstore.repository.shoppingcart;

import com.bookstore.model.ShoppingCart;
import com.bookstore.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    @EntityGraph(attributePaths = {"cartItems"})
    Optional<ShoppingCart> findById(Long id);

    @EntityGraph(attributePaths = {"cartItems"})
    List<ShoppingCart> findAll();

    @EntityGraph(attributePaths = {"cartItems"})
    List<ShoppingCart> findAllByUser(User user);

}
