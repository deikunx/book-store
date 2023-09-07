package com.bookstore.mapper;

import com.bookstore.config.MapperConfig;
import com.bookstore.dto.order.OrderResponseDto;
import com.bookstore.dto.order.OrderUpdateRequestDto;
import com.bookstore.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(config = MapperConfig.class, uses = {OrderItemMapper.class})
public interface OrderMapper {
    @Mappings({
            @Mapping(target = "userId", source = "user.id")
    })
    OrderResponseDto toDto(Order order);

    Order toModel(OrderUpdateRequestDto orderDto);

    OrderUpdateRequestDto toUpdateDto(Order order);
}
