package com.bookstore.service;

import com.bookstore.dto.user.UserRegistrationRequest;
import com.bookstore.dto.user.UserResponseDto;
import com.bookstore.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequest userRegistrationRequest)
            throws RegistrationException;
}
