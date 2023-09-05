package com.bookstore.repository.shoppingcart;

import com.bookstore.model.ShoppingCart;
import com.bookstore.model.User;
import com.bookstore.repository.user.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShoppingCartRepositoryTest {
    
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Find shopping cart by id")
    void findById_ShouldReturnShoppingCartById() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setCartItems(Collections.emptySet());
        shoppingCart.setUser(createUser("test@gmail.com"));
        ShoppingCart expected = shoppingCartRepository.save(shoppingCart);

        Optional<ShoppingCart> actual = shoppingCartRepository.findById(shoppingCart.getId());

        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    @DisplayName("Find all shopping carts")
    void findAll_ShouldReturnListOfAllShoppingCarts() {
        ShoppingCart shoppingCart1 = new ShoppingCart();
        shoppingCart1.setCartItems(Collections.emptySet());
        shoppingCart1.setUser(createUser("test1@gmail.com"));
        shoppingCartRepository.save(shoppingCart1);

        ShoppingCart shoppingCart2 = new ShoppingCart();
        shoppingCart2.setCartItems(Collections.emptySet());
        shoppingCart2.setUser(createUser("test2@gmail.com"));
        shoppingCartRepository.save(shoppingCart2);

        List<ShoppingCart> expected = List.of(shoppingCart1, shoppingCart2);

        List<ShoppingCart> actual = shoppingCartRepository.findAll();

        assertEquals(expected, actual);
    }

    private User createUser(String email) {
        User user = new User();
        user.setFirstName("test");
        user.setLastName("test");
        user.setEmail(email);
        user.setPassword("141");
        return userRepository.save(user);
    }
}