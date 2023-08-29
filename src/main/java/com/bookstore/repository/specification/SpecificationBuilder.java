package com.bookstore.repository.specification;

import com.bookstore.dto.book.BookSearchParametersDto;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationBuilder<T> {
    Specification<T> build(BookSearchParametersDto searchParametersDto);
}
