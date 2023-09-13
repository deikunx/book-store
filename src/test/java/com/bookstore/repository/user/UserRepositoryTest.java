package com.bookstore.repository.user;

import com.bookstore.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Find user by email")
    void findByEmail_ShouldReturnUserByEmail() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setFirstName("test");
        user.setLastName("test");
        user.setPassword("141");
        User expected = userRepository.save(user);

        Optional<User> actual = userRepository.findByEmail(user.getEmail());

        Assertions.assertTrue(actual.isPresent());
        Assertions.assertEquals(expected, actual.get());
    }
}
