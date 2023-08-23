package com.bookstore.service.impl;

import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookDtoWithoutCategoryIds;
import com.bookstore.dto.book.BookSearchParametersDto;
import com.bookstore.dto.book.CreateBookRequestDto;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.repository.book.BookRepository;
import com.bookstore.repository.book.BookSpecificationBuilder;
import com.bookstore.service.BookService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder bookSpecificationBuilder;
    private final CategoryRepository categoryRepository;

    @Override
    public BookDto save(CreateBookRequestDto bookRequestDto) {
        Set<Category> categories = categoryRepository
                .findByIdIn(bookRequestDto.getCategoryIds());
        Book book = bookMapper.toEntity(bookRequestDto);
        book.setCategories(categories);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public List<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAllWithCategories(pageable)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public BookDto findById(Long id) {
        return bookRepository.findBookById(id)
                .map(bookMapper::toDto)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find book by id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public void updateBookById(Long id, CreateBookRequestDto createBookRequestDto) {
        Set<Category> categories = categoryRepository
                .findByIdIn(createBookRequestDto.getCategoryIds());
        Book book = bookRepository
                .findBookById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        book.setTitle(createBookRequestDto.getTitle());
        book.setAuthor(createBookRequestDto.getAuthor());
        book.setIsbn(createBookRequestDto.getIsbn());
        book.setPrice(createBookRequestDto.getPrice());
        book.setCoverImage(createBookRequestDto.getCoverImage());
        book.setDescription(createBookRequestDto.getDescription());
        book.setCategories(categories);
        bookRepository.save(book);
    }

    @Override
    public List<BookDto> search(BookSearchParametersDto bookSearchParametersDto) {
        return bookRepository
                .findAll(bookSpecificationBuilder.build(bookSearchParametersDto))
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public List<BookDtoWithoutCategoryIds> findAllByCategoryId(Long categoryId) {
        return bookRepository.findAllByCategoryId(categoryId)
                .stream().map(bookMapper::toDtoWithoutCategories)
                .toList();
    }
}
