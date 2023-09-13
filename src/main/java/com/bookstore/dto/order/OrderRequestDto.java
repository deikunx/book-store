package com.bookstore.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderRequestDto {
    @NotBlank
    private String shippingAddress;
}
