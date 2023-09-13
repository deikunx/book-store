package com.bookstore.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.dto.cartitem.CartItemRequestDto;
import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import com.bookstore.service.ShoppingCartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WithMockUser(username = "admin@gmail.com", password = "123456", roles = {"ADMIN"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShoppingCartService shoppingCartService;

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
    void addItemToCart_ShouldAddItemToCartAndReturnOkStatus() throws Exception {
        CartItemRequestDto requestDto = new CartItemRequestDto();
        requestDto.setBookId(1L);
        requestDto.setQuantity(2);

        mockMvc.perform(post("/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(shoppingCartService, times(1))
                .addItemToCart(eq(requestDto.getBookId()), eq(requestDto.getQuantity()));
    }

    @Test
    @DisplayName("Get all items from cart for current user")
    void findAllByUser_ShouldReturnAllItemsFromCartOfCurrentUserAndOkStatus() throws Exception {
        ShoppingCartDto shoppingCartDto = new ShoppingCartDto()
                .setCartItems(Collections.emptySet())
                .setUserId(1L)
                .setId(1L);

        when(shoppingCartService.findAllByUser()).thenReturn(shoppingCartDto);

        mockMvc.perform(get("/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("Delete cart item by ID")
    void deleteCartItemById_ShouldDeleteItemFromCartAndReturnNoContentStatus() throws Exception {
        Long cartItemId = 1L;

        mockMvc.perform(delete("/cart/cart-items/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(shoppingCartService, times(1)).deleteCartItemById(eq(cartItemId));
    }

    @Test
    @DisplayName("Update quantity of cart item by ID")
    void updateQuantity_ShouldUpdateQuantityOfCartItemInCartAndReturnOkStatus() throws Exception {
        Long cartItemId = 1L;

        CartItemUpdateDto updateDto = new CartItemUpdateDto();
        updateDto.setQuantity(5);

        mockMvc.perform(put("/cart/cart-items/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        verify(shoppingCartService, times(1)).updateQuantity(eq(cartItemId), eq(updateDto));
    }
}
