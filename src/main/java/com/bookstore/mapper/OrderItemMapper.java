package com.bookstore.mapper;

import com.bookstore.config.MapperConfig;
import com.bookstore.dto.orderitem.OrderItemResponseDto;
import com.bookstore.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(config = MapperConfig.class)
public interface OrderItemMapper {
    @Mappings({
            @Mapping(target = "bookId", source = "book.id")
    })
    OrderItemResponseDto toDto(OrderItem orderItem);
}
