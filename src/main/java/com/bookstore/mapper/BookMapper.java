package com.bookstore.mapper;

import com.bookstore.config.MapperConfig;
import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookDtoWithoutCategoryIds;
import com.bookstore.dto.book.CreateBookRequestDto;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface BookMapper {
    @Mapping(target = "categoryIds", ignore = true)
    BookDto toDto(Book book);

    Book toEntity(CreateBookRequestDto bookRequestDto);

    BookDtoWithoutCategoryIds toDtoWithoutCategories(Book book);

    @AfterMapping default void setCategoryIds(@MappingTarget BookDto bookDto, Book book) {
        bookDto.setCategoryIds(book
                .getCategories()
                .stream()
                .map(Category::getId)
                .collect(Collectors.toSet()));
    }

    @Named("bookFromId")
    default Book bookFromId(Long id) {
        if (id == null) {
            return null;
        }
        Book book = new Book();
        book.setId(id);
        return book;
    }
}
