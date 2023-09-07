package com.bookstore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookSearchParametersDto;
import com.bookstore.dto.book.CreateBookRequestDto;
import com.bookstore.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WithMockUser(username = "admin", roles = {"ADMIN"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @Sql(scripts = "classpath:db/books/add-three-default-books.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/books/remove-all-books.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get all books")
    void findAll_ShouldReturnListOfAllBooksAndOkStatus() throws Exception {
        List<BookDto> expected = new ArrayList<>();
        expected.add(new BookDto().setTitle("Book 1")
                .setPrice(BigDecimal.valueOf(19.99))
                .setAuthor("Author 1")
                .setIsbn("ISBN123456789")
                .setDescription("Description 1")
                .setCoverImage("image1.jpg")
                .setCategoryIds(Collections.emptySet())
                .setId(1L));

        expected.add(new BookDto().setTitle("Book 2")
                .setPrice(BigDecimal.valueOf(24.99))
                .setAuthor("Author 2")
                .setIsbn("ISBN987654321")
                .setDescription("Description 2")
                .setCoverImage("image2.jpg")
                .setCategoryIds(Collections.emptySet())
                .setId(2L));

        expected.add(new BookDto().setTitle("Book 3")
                .setPrice(BigDecimal.valueOf(14.99))
                .setAuthor("Author 3")
                .setIsbn("ISBN456789123")
                .setDescription("Description 3")
                .setCoverImage("image3.jpg")
                .setCategoryIds(Collections.emptySet())
                .setId(3L));

        when(bookService.findAll(any(Pageable.class))).thenReturn(expected);

        mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Sql(
            scripts = "classpath:db/books/delete-book-1984.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Create a new book")
    public void saveBook_ValidRequestDto_Success() throws Exception {
        CreateBookRequestDto requestDto = new CreateBookRequestDto()
                .setAuthor("Test Author")
                .setIsbn("987001")
                .setPrice(BigDecimal.valueOf(199))
                .setTitle("1984")
                .setDescription("Test Description")
                .setCoverImage("Test CI")
                .setCategoryIds(Collections.emptySet());

        BookDto response = new BookDto()
                .setTitle(requestDto.getTitle())
                .setIsbn(requestDto.getIsbn())
                .setPrice(requestDto.getPrice())
                .setDescription(requestDto.getDescription())
                .setCoverImage(requestDto.getCoverImage())
                .setAuthor(requestDto.getAuthor())
                .setCategoryIds(requestDto.getCategoryIds());

        when(bookService.save(eq(requestDto))).thenReturn(response);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @Sql(scripts = "classpath:db/books/add-one-default-book.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/books/delete-book-1984.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Find book by ID")
    void findById_ShouldReturnBookByIdAndOkStatus() throws Exception {
        Long bookId = 1L;

        BookDto bookDto = new BookDto();

        when(bookService.findById(eq(bookId))).thenReturn(bookDto);

        mockMvc.perform(get("/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Sql(scripts = "classpath:db/books/add-one-default-book.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/books/delete-book-1984.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Delete book by ID")
    void deleteById_ShouldDeleteBookByIdAndReturnNoContentStatus() throws Exception {
        Long bookId = 1L;

        mockMvc.perform(delete("/books/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @Sql(scripts = "classpath:db/books/add-one-default-book.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/books/delete-book-1984.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update book by id")
    void updateBookById_ShouldUpdateBookAndReturnOkStatus() throws Exception {
        Long bookId = 1L;

        CreateBookRequestDto bookRequestDto = new CreateBookRequestDto()
                .setTitle("1984")
                .setAuthor("Updated Author")
                .setPrice(BigDecimal.valueOf(19.99))
                .setIsbn("ISBN123456789")
                .setDescription("Description 1")
                .setCoverImage("image1.jpg")
                .setCategoryIds(Collections.emptySet());

        mockMvc.perform(put("/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @Sql(scripts = "classpath:db/books/add-three-default-books.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/books/remove-all-books.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Search books by params")
    void search_ShouldReturnListWithBookByParamsAndOkStatus() throws Exception {
        List<BookDto> expected = new ArrayList<>();
        expected.add(new BookDto().setTitle("Book 1")
                .setPrice(BigDecimal.valueOf(19.99))
                .setAuthor("Author 1")
                .setIsbn("ISBN123456789")
                .setDescription("Description 1")
                .setCoverImage("image1.jpg")
                .setCategoryIds(Collections.emptySet())
                .setId(1L));

        expected.add(new BookDto().setTitle("Book 2")
                .setPrice(BigDecimal.valueOf(24.99))
                .setAuthor("Author 2")
                .setIsbn("ISBN987654321")
                .setDescription("Description 2")
                .setCoverImage("image2.jpg")
                .setCategoryIds(Collections.emptySet())
                .setId(2L));

        when(bookService.search(any(BookSearchParametersDto.class))).thenReturn(expected);

        mockMvc.perform(get("/books/search")
                        .param("param1", "value1")
                        .param("param2", "value2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
