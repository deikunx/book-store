package com.bookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookDtoWithoutCategoryIds;
import com.bookstore.dto.book.BookSearchParametersDto;
import com.bookstore.dto.book.CreateBookRequestDto;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.repository.book.BookRepository;
import com.bookstore.repository.book.BookSpecificationBuilder;
import com.bookstore.repository.category.CategoryRepository;
import com.bookstore.service.impl.BookServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private CreateBookRequestDto createBookRequestDto;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    @DisplayName("Verify save() method works")
    public void save_SuccessfulSave() {
        CreateBookRequestDto bookRequestDto = new CreateBookRequestDto();
        bookRequestDto.setAuthor("Test Author");
        bookRequestDto.setIsbn("987001");
        bookRequestDto.setTitle("Test");
        bookRequestDto.setPrice(BigDecimal.valueOf(199));
        bookRequestDto.setCoverImage("test.png");
        bookRequestDto.setDescription("Test Description");
        Set<Long> categoryIds = Set.of(1L);
        bookRequestDto.setCategoryIds(categoryIds);

        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        Book book = new Book();
        book.setTitle(bookRequestDto.getTitle());
        book.setAuthor(bookRequestDto.getAuthor());
        book.setIsbn(bookRequestDto.getIsbn());
        book.setPrice(bookRequestDto.getPrice());
        book.setCoverImage(bookRequestDto.getCoverImage());
        book.setDescription(bookRequestDto.getDescription());
        book.setCategories(Set.of(category));

        BookDto bookDto = new BookDto();
        bookDto.setTitle(book.getTitle());
        bookDto.setPrice(book.getPrice());
        bookDto.setIsbn(book.getIsbn());
        bookDto.setCoverImage(book.getCoverImage());
        bookDto.setDescription(book.getDescription());
        bookDto.setAuthor(book.getAuthor());
        bookDto.setCategoryIds(bookRequestDto.getCategoryIds());

        when(categoryRepository.findByIdIn(categoryIds)).thenReturn(Set.of(category));
        when(bookMapper.toEntity(bookRequestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        final BookDto actual = bookService.save(bookRequestDto);
        verify(categoryRepository).findByIdIn(categoryIds);
        verify(bookMapper).toEntity(bookRequestDto);
        verify(bookRepository).save(book);
        verify(bookMapper).toDto(book);
        assertEquals(bookDto, actual);
    }

    @Test
    @DisplayName("Verify findAll() method works")
    public void findAll_ShouldReturnListOfBooks() {
        List<Book> bookEntities = new ArrayList<>();
        Book book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Book 1");
        bookEntities.add(book1);

        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Book 2");
        bookEntities.add(book2);

        List<BookDto> expected = new ArrayList<>();
        BookDto bookDto1 = new BookDto();
        bookDto1.setId(1L);
        bookDto1.setTitle("Book 1");
        expected.add(bookDto1);

        BookDto bookDto2 = new BookDto();
        bookDto2.setId(2L);
        bookDto2.setTitle("Book 2");
        expected.add(bookDto2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")));

        when(bookRepository.findAllWithCategories(pageable)).thenReturn(bookEntities);
        when(bookMapper.toDto(book1)).thenReturn(bookDto1);
        when(bookMapper.toDto(book2)).thenReturn(bookDto2);

        final List<BookDto> actual = bookService.findAll(pageable);
        verify(bookRepository).findAllWithCategories(pageable);
        verify(bookMapper).toDto(book1);
        verify(bookMapper).toDto(book2);
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify findById() method works")
    public void findBookById_WithValidId_ShouldReturnBook() {
        Long bookId = 1L;
        BookDto expected = new BookDto();
        expected.setId(bookId);
        expected.setTitle("Sample Book");

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Sample Book");

        when(bookRepository.findBookById(bookId)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(expected);

        BookDto actual = bookService.findById(bookId);

        verify(bookRepository).findBookById(bookId);
        verify(bookMapper).toDto(book);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Verify that findBookById() method throws exception with not found book")
    public void findBookById_WithInvalidId_ShouldThrowEntityNotFoundException() {
        Long bookId = 1L;

        when(bookRepository.findBookById(bookId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.findById(bookId));

        verify(bookRepository).findBookById(bookId);
        verify(bookMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Verify deleteById() method works")
    public void deleteById_SuccessfulDelete() {
        Long bookId = 1L;

        bookService.deleteById(bookId);

        verify(bookRepository).deleteById(bookId);
    }

    @Test
    @DisplayName("Verify updateBookById() method works")
    public void updateBookById_SuccessfulUpdate() {
        Long bookId = 1L;

        Category category1 = new Category();
        category1.setId(1L);
        Category category2 = new Category();
        category2.setId(2L);

        Book book = new Book();
        book.setId(bookId);

        Set<Long> categoryIds = Set.of(1L, 2L);

        when(createBookRequestDto.getCategoryIds()).thenReturn(categoryIds);
        when(categoryRepository.findByIdIn(categoryIds)).thenReturn(Set.of(category1, category2));
        when(bookRepository.findBookById(bookId)).thenReturn(Optional.of(book));

        bookService.updateBookById(bookId, createBookRequestDto);

        verify(categoryRepository).findByIdIn(categoryIds);
        verify(bookRepository).findBookById(bookId);

        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("Verify search() method works")
    public void search_ShouldReturnBooksWithCriteria() {
        final String[] params = new String[0];
        Book book = new Book();
        book.setId(1L);
        book.setAuthor("George Orwell");
        book.setTitle("1984");
        book.setIsbn("978001");
        book.setPrice(BigDecimal.valueOf(299.99));
        book.setDescription("Anti-utopia");
        book.setCoverImage("1984.jpg");
        book.setCategories(Collections.emptySet());

        BookDto expected = new BookDto();
        expected.setId(book.getId());
        expected.setAuthor(book.getAuthor());
        expected.setTitle(book.getTitle());
        expected.setIsbn(book.getIsbn());
        expected.setPrice(book.getPrice());
        expected.setDescription(book.getDescription());
        expected.setCoverImage(book.getCoverImage());
        expected.setCategoryIds(Collections.emptySet());

        Specification<Book> spec = Specification.where(null);
        BookSearchParametersDto bookSearchParametersDto =
                new BookSearchParametersDto(params, params);

        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookSpecificationBuilder.build(bookSearchParametersDto)).thenReturn(spec);
        when(bookRepository.findAll(spec, pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(expected);

        List<BookDto> actual = bookService.search(bookSearchParametersDto, pageable);

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(expected);
        verify(bookRepository, times(1)).findAll(spec, pageable);
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify findAllByCategoryId() method works")
    public void findAllByCategoryId_WithValidId_ShouldReturnBooksForCategory() {
        Long categoryId = 1L;
        BookDtoWithoutCategoryIds bookDto1 = new BookDtoWithoutCategoryIds();
        bookDto1.setId(1L);
        BookDtoWithoutCategoryIds bookDto2 = new BookDtoWithoutCategoryIds();
        bookDto2.setId(2L);

        when(bookRepository.findAllByCategoryId(categoryId))
                .thenReturn(List.of(new Book(), new Book()));
        when(bookMapper.toDtoWithoutCategories(any(Book.class))).thenReturn(bookDto1, bookDto2);

        List<BookDtoWithoutCategoryIds> result = bookService.findAllByCategoryId(categoryId);

        verify(bookRepository).findAllByCategoryId(categoryId);
        verify(bookMapper, times(2)).toDtoWithoutCategories(any(Book.class));

        List<BookDtoWithoutCategoryIds> expected = List.of(bookDto1, bookDto2);
        assertEquals(expected, result);
    }
}
