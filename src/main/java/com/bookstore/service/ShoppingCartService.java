package com.bookstore.service;

import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import java.util.List;

public interface ShoppingCartService {
    List<ShoppingCartDto> findAll();

    void addItemToCart(Long bookId, int quantity);

    void deleteCartItemById(Long cartItemId);

    ShoppingCartDto findAllByUser();

    void updateQuantity(Long cartItemId, CartItemUpdateDto cartItem);
}
