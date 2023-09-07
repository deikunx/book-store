package com.bookstore.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.dto.user.UserLoginRequestDto;
import com.bookstore.dto.user.UserRegistrationRequest;
import com.bookstore.dto.user.UserResponseDto;
import com.bookstore.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

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
    @DisplayName("Register with valid request should register new user")
    void registerUser_WithValidRequest_ShouldRegister() throws Exception {
        UserRegistrationRequest userRequest = new UserRegistrationRequest();
        userRequest.setEmail("johndoe@gmail.com");
        userRequest.setFirstName("John");
        userRequest.setLastName("Doe");
        userRequest.setPassword("123456");
        userRequest.setRepeatPassword("123456");
        userRequest.setShippingAddress("15 Avenue");

        UserResponseDto expected = new UserResponseDto();
        expected.setEmail(userRequest.getEmail());
        expected.setFirstName(userRequest.getFirstName());
        expected.setLastName(userRequest.getLastName());
        expected.setShippingAddress(userRequest.getShippingAddress());
        expected.setId(1L);

        Mockito.when(userService
                .register(
                        Mockito.any(UserRegistrationRequest.class))).thenReturn(expected);

        String jsonRequest = objectMapper.writeValueAsString(userRequest);

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), UserResponseDto.class);

        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @DisplayName("Register with invalid password should return 400 status")
    void registerUser_WithInvalidPasswords_ShouldReturn400Status() throws Exception {
        UserRegistrationRequest userRequest = new UserRegistrationRequest();
        userRequest.setEmail("johndoe@gmail.com");
        userRequest.setFirstName("John");
        userRequest.setLastName("Doe");
        userRequest.setPassword("123456");
        userRequest.setRepeatPassword("wrong password");
        userRequest.setShippingAddress("15 Avenue");

        String jsonRequest = objectMapper.writeValueAsString(userRequest);

        mockMvc.perform(post("/auth/register")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Sql(scripts = "classpath:db/authentication/add-default-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/authentication/remove-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Login with valid email and password should authenticate user")
    void login() throws Exception {
        UserLoginRequestDto loginRequest = new UserLoginRequestDto()
                .setEmail("johndoe@gmail.com")
                .setPassword("123456");

        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/auth/login")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Sql(scripts = "classpath:db/authentication/add-default-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/authentication/remove-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Login with invalid password should return 401 status")
    void login_WithInvalidPassword_ShouldReturn401Status() throws Exception {
        UserLoginRequestDto loginRequest = new UserLoginRequestDto()
                .setEmail("johndoe@gmail.com")
                .setPassword("wrong password");

        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/auth/login")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
