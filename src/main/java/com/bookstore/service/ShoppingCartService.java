package com.bookstore.service;

import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import java.util.List;

public interface ShoppingCartService {
    List<ShoppingCartDto> findAll();

    void addItemToCard(Long bookId, int quantity);

    void deleteCartItemById(Long cartItemId);

    List<ShoppingCartDto> findAllByUser();

    void updateQuantity(Long cartItemId, CartItemUpdateDto cartItem);
}
