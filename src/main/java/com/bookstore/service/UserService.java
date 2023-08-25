package com.bookstore.service;

import com.bookstore.dto.user.UserRegistrationRequest;
import com.bookstore.dto.user.UserResponseDto;
import com.bookstore.exception.RegistrationException;
import com.bookstore.model.User;
import java.util.Optional;

public interface UserService {
    UserResponseDto register(UserRegistrationRequest userRegistrationRequest)
            throws RegistrationException;

    Optional<User> getCurrentUser();
}
