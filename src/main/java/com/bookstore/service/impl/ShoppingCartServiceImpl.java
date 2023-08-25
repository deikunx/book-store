package com.bookstore.service.impl;

import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.ShoppingCartMapper;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import com.bookstore.model.ShoppingCart;
import com.bookstore.model.User;
import com.bookstore.repository.book.BookRepository;
import com.bookstore.repository.cartitem.CartItemRepository;
import com.bookstore.repository.shoppingcart.ShoppingCartRepository;
import com.bookstore.service.ShoppingCartService;
import com.bookstore.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final BookRepository bookRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final UserService userService;
    private final ShoppingCartMapper shoppingCartMapper;
    private final CartItemRepository cartItemRepository;

    @Override
    public List<ShoppingCartDto> findAll() {
        return shoppingCartRepository
                .findAll()
                .stream()
                .map(shoppingCartMapper::toDto)
                .toList();
    }

    @Override
    public void addItemToCard(Long bookId, int quantity) {
        User currentUser = userService
                .getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("Can't get current user"));

        Book book = bookRepository
                .findBookById(bookId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find book with id" + bookId));

        CartItem cartItem = new CartItem();
        cartItem.setBook(book);
        cartItem.setQuantity(quantity);
        ShoppingCart shoppingCart = shoppingCartRepository
                .findById(currentUser.getId())
                .orElseThrow();

        cartItem.setShoppingCart(shoppingCart);
        shoppingCart.getCartItems().add(cartItem);
        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    public void deleteCartItemById(Long cartItemId) {
        User currentUser = userService
                .getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("Can't get current user"));

        ShoppingCart shoppingCart = shoppingCartRepository
                .findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Can't "
                        + "get shopping cart with id " + currentUser.getId()));

        CartItem cartItem = shoppingCart.getCartItems()
                .stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Can't find cart item with id "
                        + cartItemId + " in cart"));

        cartItemRepository.deleteById(cartItem.getId());
    }

    @Override
    public List<ShoppingCartDto> findAllByUser() {
        User user = userService.getCurrentUser().orElseThrow();

        return shoppingCartRepository
                .findAllByUser(user)
                .stream()
                .map(shoppingCartMapper::toDto)
                .toList();
    }

    @Override
    public void updateQuantity(Long cartItemId, CartItemUpdateDto cartItemUpdateDto) {
        User currentUser = userService
                .getCurrentUser()
                .orElseThrow(() -> new EntityNotFoundException("Can't get current user"));

        ShoppingCart shoppingCart = shoppingCartRepository
                .findById(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Can't "
                        + "get shopping cart with id " + currentUser.getId()));

        CartItem cartItem = shoppingCart.getCartItems()
                .stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Can't find cart item with id "
                        + cartItemId + " in cart"));

        int newQuantity = cartItemUpdateDto.getQuantity();
        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
    }
}
