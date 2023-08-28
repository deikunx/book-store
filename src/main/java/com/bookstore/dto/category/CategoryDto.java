package com.bookstore.dto.category;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryDto {
    private Long id;

    @NotNull
    private String name;

    private String description;
}
