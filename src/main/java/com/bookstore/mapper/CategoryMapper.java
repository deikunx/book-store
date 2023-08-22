package com.bookstore.mapper;

import com.bookstore.config.MapperConfig;
import com.bookstore.dto.category.CategoryDto;
import com.bookstore.model.Category;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface CategoryMapper {
    CategoryDto toDto(Category category);

    Category toEntity(CategoryDto categoryRequestDto);
}
