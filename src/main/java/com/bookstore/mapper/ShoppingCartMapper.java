package com.bookstore.mapper;

import com.bookstore.config.MapperConfig;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import com.bookstore.model.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(config = MapperConfig.class, uses = {CartItemMapper.class})
public interface ShoppingCartMapper {
    @Mappings({
            @Mapping(target = "userId", source = "user.id")
    })
    ShoppingCartDto toDto(ShoppingCart shoppingCart);

}
