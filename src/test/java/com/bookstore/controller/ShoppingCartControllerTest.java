package com.bookstore.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.dto.cartitem.CartItemDto;
import com.bookstore.dto.cartitem.CartItemRequestDto;
import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WithMockUser(username = "admin@gmail.com", password = "123456", roles = {"ADMIN"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("Add item to cart")
    @Sql(scripts = {
            "classpath:db/user/add-users-with-shopping-carts.sql",
            "classpath:db/books/add-three-default-books.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/user/delete-users-and-carts.sql",
            "classpath:db/books/remove-all-books.sql",
            "classpath:db/shoppingcart/remove-all-cart-items.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void addItemToCart_ShouldAddItemToCartAndReturnOkStatus() throws Exception {
        CartItemRequestDto request = new CartItemRequestDto()
                .setBookId(2L)
                .setQuantity(3);

        CartItemDto cartItem = new CartItemDto()
                .setId(1L)
                .setBookId(2L)
                .setQuantity(3)
                .setBookTitle("Book 2");

        ShoppingCartDto expected = new ShoppingCartDto()
                .setId(1L)
                .setUserId(1L)
                .setCartItems(Set.of(cartItem));

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/cart")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ShoppingCartDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), ShoppingCartDto.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Get all items from cart for current user")
    @Sql(scripts = {
            "classpath:db/user/add-users-with-shopping-carts.sql",
            "classpath:db/books/add-three-default-books.sql",
            "classpath:db/shoppingcart/add-cart-items-to-cart.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/user/delete-users-and-carts.sql",
            "classpath:db/books/remove-all-books.sql",
            "classpath:db/shoppingcart/remove-all-cart-items.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByUser_ShouldReturnAllItemsFromCartOfCurrentUserAndOkStatus() throws Exception {
        ShoppingCartDto expected = getShoppingCartDtoWithOneCartItem();

        MvcResult result = mockMvc.perform(get("/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        ShoppingCartDto actual = objectMapper.readValue(result.getResponse()
                .getContentAsString(), ShoppingCartDto.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Delete cart item by ID")
    @Sql(scripts = {
            "classpath:db/user/add-users-with-shopping-carts.sql",
            "classpath:db/books/add-three-default-books.sql",
            "classpath:db/shoppingcart/add-cart-items-to-cart.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/user/delete-users-and-carts.sql",
            "classpath:db/books/remove-all-books.sql",
            "classpath:db/shoppingcart/remove-all-cart-items.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteCartItemById_ShouldDeleteItemFromCartAndReturnNoContentStatus() throws Exception {
        Long cartItemId = 1L;

        mockMvc.perform(delete("/cart/cart-items/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Update quantity of cart item by ID")
    @Sql(scripts = {
            "classpath:db/user/add-users-with-shopping-carts.sql",
            "classpath:db/books/add-three-default-books.sql",
            "classpath:db/shoppingcart/add-cart-items-to-cart.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/user/delete-users-and-carts.sql",
            "classpath:db/books/remove-all-books.sql",
            "classpath:db/shoppingcart/remove-all-cart-items.sql"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateQuantity_ShouldUpdateQuantityOfCartItemInCartAndReturnOkStatus() throws Exception {
        Long cartItemId = 1L;

        CartItemDto expected = new CartItemDto();
        expected.setId(cartItemId);
        expected.setQuantity(5);
        expected.setBookTitle("1984");
        expected.setBookId(1L);

        CartItemUpdateDto updateDto = new CartItemUpdateDto();
        updateDto.setQuantity(5);

        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        MvcResult result = mockMvc.perform(put("/cart/cart-items/{cartItemId}", cartItemId)
                        .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CartItemDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), CartItemDto.class);
        Assertions.assertEquals(expected, actual);
    }

    private ShoppingCartDto getShoppingCartDtoWithOneCartItem() {
        ShoppingCartDto shoppingCartDto = new ShoppingCartDto();
        shoppingCartDto.setId(1L);
        shoppingCartDto.setUserId(1L);

        CartItemDto cartItemResponseDto = new CartItemDto();
        cartItemResponseDto.setId(1L);
        cartItemResponseDto.setBookId(1L);
        cartItemResponseDto.setBookTitle("1984");
        cartItemResponseDto.setQuantity(1);

        Set<CartItemDto> cartItems = new HashSet<>();
        cartItems.add(cartItemResponseDto);
        shoppingCartDto.setCartItems(cartItems);
        return shoppingCartDto;
    }
}
