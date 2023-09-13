package com.bookstore.dto.cartitem;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CartItemRequestDto {
    @NotNull
    private Long bookId;
    @NotNull
    private int quantity;
}
