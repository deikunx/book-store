package com.bookstore.service;

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
import com.bookstore.service.impl.ShoppingCartServiceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import liquibase.pro.packaged.S;
import liquibase.pro.packaged.U;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private UserService userService;


    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    @Test
    @DisplayName("Verify findAll() method works")
    void findAll_ShouldReturnListOfAllShoppingCarts() {
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        shoppingCarts.add(new ShoppingCart());
        shoppingCarts.add(new ShoppingCart());

        when(shoppingCartRepository.findAll()).thenReturn(shoppingCarts);
        when(shoppingCartMapper.toDto(any(ShoppingCart.class))).thenReturn(new ShoppingCartDto());

        List<ShoppingCartDto> result = shoppingCartService.findAll();
        assertEquals(shoppingCarts.size(), result.size());

        verify(shoppingCartRepository).findAll();
        verify(shoppingCartMapper, times(shoppingCarts.size())).toDto(any(ShoppingCart.class));
    }

    @Test
    @DisplayName("Verify addItemToCard() method works")
    void addItemToCard_WhenBookWasFound_ShouldAddBookToCart() {
        Long bookId = 1L;
        int quantity = 2;

        User user = new User();
        user.setId(1L);
        Book book = new Book();
        book.setId(bookId);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(user.getId());

        when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        when(bookRepository.findBookById(bookId)).thenReturn(Optional.of(book));
        when(shoppingCartRepository.findById(user.getId())).thenReturn(Optional.of(shoppingCart));

        shoppingCartService.addItemToCart(bookId, quantity);
        verify(shoppingCartRepository).save(any(ShoppingCart.class));
    }

    @Test
    @DisplayName("Verify addItemToCart() method throws exception when book not found")
    public void addItemToCart_WhenBookNotFound_ShouldThrowEntityNotFoundException() {
        User currentUser = new User();
        currentUser.setId(1L);

        ShoppingCart shoppingCart = new ShoppingCart();

        Long nonExistentBookId = 999L;
        int quantity = 2;

        when(bookRepository.findBookById(nonExistentBookId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            shoppingCartService.addItemToCart(nonExistentBookId, quantity);
        });
    }

    @Test
    @DisplayName("Verify deleteCartItemById() method works")
    void deleteCartItemById_SuccessfulDelete() {
        Long cartItemId = 1L;
        User user = new User();
        user.setId(1L);
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(user.getId());
        CartItem cartItem = new CartItem();
        cartItem.setId(cartItemId);
        cartItem.setShoppingCart(shoppingCart);
        shoppingCart.getCartItems().add(cartItem);

        when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        when(shoppingCartRepository.findById(user.getId())).thenReturn(Optional.of(shoppingCart));

        shoppingCartService.deleteCartItemById(cartItemId);

        verify(cartItemRepository).deleteById(cartItemId);
    }

    @Test
    @DisplayName("Verify findAllByUser() method works")
    void findAllByUser_ShouldReturnShoppingCartOfCurrentUser() {
        ShoppingCart shoppingCart = new ShoppingCart();
        ShoppingCartDto expectedDto = new ShoppingCartDto();

        User user = new User();
        user.setId(1L);
        shoppingCart.setId(user.getId());

        when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        when(shoppingCartRepository.findById(anyLong())).thenReturn(Optional.of(shoppingCart));
        when(shoppingCartMapper.toDto(shoppingCart)).thenReturn(expectedDto);

        ShoppingCartDto result = shoppingCartService.findAllByUser();

        verify(shoppingCartRepository).findById(anyLong());
        verify(shoppingCartMapper).toDto(shoppingCart);
        assertEquals(expectedDto, result);
    }

    @Test
    @DisplayName("Verify updateQuantity() method works")
    void updateQuantity_SuccessfulUpdate() {
        User currentUser = new User();
        currentUser.setId(1L);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(currentUser.getId());
        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setQuantity(2);
        shoppingCart.setCartItems(Set.of(cartItem));

        CartItemUpdateDto cartItemUpdateDto = new CartItemUpdateDto();
        cartItemUpdateDto.setQuantity(3);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(shoppingCartRepository.findById(currentUser.getId())).thenReturn(Optional.of(shoppingCart));

        shoppingCartService.updateQuantity(cartItem.getId(), cartItemUpdateDto);

        assertEquals(3, cartItem.getQuantity());

        verify(cartItemRepository, times(1)).save(cartItem);
    }
}