package com.bookstore.repository.shoppingcart;

import com.bookstore.model.ShoppingCart;
import com.bookstore.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    @Query("SELECT sc FROM ShoppingCart sc LEFT JOIN FETCH sc.cartItems WHERE sc.id = :id")
    Optional<ShoppingCart> findById(Long id);

    @Query("SELECT sc FROM ShoppingCart sc LEFT JOIN FETCH sc.cartItems")
    List<ShoppingCart> findAll();

    @Query("SELECT sc FROM ShoppingCart sc LEFT JOIN FETCH sc.cartItems WHERE sc.user = :user")
    List<ShoppingCart> findAllByUser(User user);

}
