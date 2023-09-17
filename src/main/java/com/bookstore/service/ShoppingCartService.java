package com.bookstore.service;

import com.bookstore.dto.cartitem.CartItemDto;
import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import java.util.List;

public interface ShoppingCartService {
    List<ShoppingCartDto> findAll();

    ShoppingCartDto addItemToCart(Long bookId, int quantity);

    void deleteCartItemById(Long cartItemId);

    ShoppingCartDto findAllByUser();

    CartItemDto updateQuantity(Long cartItemId, CartItemUpdateDto cartItem);
}
