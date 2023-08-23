package com.bookstore.service.impl;

import com.bookstore.dto.user.UserRegistrationRequest;
import com.bookstore.dto.user.UserResponseDto;
import com.bookstore.exception.RegistrationException;
import com.bookstore.mapper.UserMapper;
import com.bookstore.model.RoleName;
import com.bookstore.model.User;
import com.bookstore.repository.role.RoleRepository;
import com.bookstore.repository.user.UserRepository;
import com.bookstore.service.UserService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequest request) throws RegistrationException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegistrationException("Can't register user with login" + request.getEmail());
        }
        User user = new User();
        user.setRoles(Set.of(roleRepository.findRoleByName(RoleName.ROLE_USER)));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setShippingAddress(request.getShippingAddress());
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }
}
