package com.bookstore.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.dto.user.UserRegistrationRequest;
import com.bookstore.dto.user.UserResponseDto;
import com.bookstore.exception.RegistrationException;
import com.bookstore.mapper.UserMapper;
import com.bookstore.model.Role;
import com.bookstore.model.RoleName;
import com.bookstore.model.ShoppingCart;
import com.bookstore.model.User;
import com.bookstore.repository.role.RoleRepository;
import com.bookstore.repository.shoppingcart.ShoppingCartRepository;
import com.bookstore.repository.user.UserRepository;
import com.bookstore.service.impl.UserServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Test
    @DisplayName("Verify register() method works")
    public void register_WithValidEmail_SuccessfulRegistration() throws RegistrationException {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        Role role = new Role();
        role.setName(RoleName.ROLE_USER);

        User user = new User();
        ShoppingCart shoppingCart = new ShoppingCart();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findRoleByName(RoleName.ROLE_USER)).thenReturn(role);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userMapper.toUserResponse(user)).thenReturn(new UserResponseDto());

        final UserResponseDto response = userService.register(request);

        verify(userRepository).findByEmail(request.getEmail());
        verify(roleRepository).findRoleByName(RoleName.ROLE_USER);
        verify(passwordEncoder).encode(request.getPassword());
        verify(shoppingCartRepository).save(any(ShoppingCart.class));

        assertNotNull(response);
    }

    @Test
    @DisplayName("Verify getCurrentUser() method works")
    public void getCurrentUser_WithValidAuthentication_ShouldReturnUserFromAuthentication() {
        String username = "testuser@example.com";
        User testUser = new User();
        testUser.setEmail(username);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(username)).thenReturn(Optional.of(testUser));

        Optional<User> currentUser = userService.getCurrentUser();

        verify(userRepository).findByEmail(username);

        assertEquals(testUser, currentUser.orElse(null));
    }

    @Test
    @DisplayName("Verify register() method throws exception when user with this email "
            + "is already exist")
    public void register_WithInvalidEmail_ShouldThrowRegistrationException() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RegistrationException.class)
                .hasMessageContaining("Can't register user with login" + request.getEmail());

        verify(userRepository).findByEmail(request.getEmail());
    }

    @Test
    @DisplayName("Verify getCurrentUser() method throws exception with null authentication")
    public void getCurrentUser_WithNullAuthentication_ShouldReturnOptionalEmptyOfUser() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);

        SecurityContextHolder.setContext(securityContext);

        Optional<User> currentUser = userService.getCurrentUser();

        assertTrue(currentUser.isEmpty());
    }
}
