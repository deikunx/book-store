package com.bookstore.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.CreateBookRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@WithMockUser(username = "admin", roles = {"ADMIN"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        expected.add(new BookDto().setTitle("1984")
                .setPrice(BigDecimal.valueOf(299.99))
                .setAuthor("George Orwell")
                .setIsbn("97800")
                .setDescription("Anti-utopia")
                .setCoverImage("1984.jpg")
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

        MvcResult result = mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookDto[] actual = objectMapper
                .readValue(result.getResponse().getContentAsByteArray(), BookDto[].class);
        Assertions.assertEquals(3, actual.length);
        Assertions.assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @Sql(scripts = "classpath:db/books/remove-all-books.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create a new book")
    public void saveBook_ValidRequestDto_Success() throws Exception {
        CreateBookRequestDto createBookRequestDto =
                new CreateBookRequestDto()
                        .setAuthor("George Orwell")
                        .setTitle("1984")
                        .setIsbn("97800")
                        .setPrice(BigDecimal.valueOf(299))
                        .setDescription("Anti-utopia")
                        .setCoverImage("1984.jpg")
                        .setCategoryIds(Collections.emptySet());

        BookDto expected =
                new BookDto()
                        .setAuthor(createBookRequestDto.getAuthor())
                        .setTitle(createBookRequestDto.getTitle())
                        .setIsbn(createBookRequestDto.getIsbn())
                        .setPrice(createBookRequestDto.getPrice())
                        .setDescription(createBookRequestDto.getDescription())
                        .setCoverImage(createBookRequestDto.getCoverImage())
                        .setCategoryIds(createBookRequestDto.getCategoryIds());

        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        MvcResult result = mockMvc.perform(post("/books")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        BookDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(), BookDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @Sql(scripts = "classpath:db/books/add-three-default-books.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/books/remove-all-books.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Find book by ID")
    void findById_ShouldReturnBookByIdAndOkStatus() throws Exception {
        Long bookId = 1L;

        BookDto expected = new BookDto()
                .setPrice(BigDecimal.valueOf(299.99))
                .setTitle("1984")
                .setAuthor("George Orwell")
                .setIsbn("97800")
                .setDescription("Anti-utopia")
                .setCoverImage("1984.jpg")
                .setCategoryIds(Collections.emptySet())
                .setId(1L);

        MvcResult result = mockMvc.perform(get("/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                            .andReturn();

        BookDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), BookDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @Sql(scripts = "classpath:db/books/add-one-default-book.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/books/remove-all-books.sql",
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
    @Sql(scripts = "classpath:db/books/remove-all-books.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update book by id")
    void updateBookById_ShouldUpdateBookAndReturnOkStatus() throws Exception {
        Long bookId = 1L;

        CreateBookRequestDto createBookRequestDto = new CreateBookRequestDto()
                .setTitle("1984")
                .setAuthor("Updated Author")
                .setPrice(BigDecimal.valueOf(19.99))
                .setIsbn("ISBN123456789")
                .setDescription("Description 1")
                .setCoverImage("image1.jpg")
                .setCategoryIds(Collections.emptySet());

        BookDto expected = new BookDto()
                .setId(bookId)
                .setAuthor(createBookRequestDto.getAuthor())
                .setTitle(createBookRequestDto.getTitle())
                .setIsbn(createBookRequestDto.getIsbn())
                .setPrice(createBookRequestDto.getPrice())
                .setDescription(createBookRequestDto.getDescription())
                .setCoverImage(createBookRequestDto.getCoverImage())
                .setCategoryIds(createBookRequestDto.getCategoryIds());

        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        MvcResult result = mockMvc.perform(put("/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        BookDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), BookDto.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @Sql(scripts = "classpath:db/books/add-three-default-books.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/books/remove-all-books.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Search books by params")
    void search_WithMatchingParam_ShouldReturnListWithOneBook() throws Exception {
        List<BookDto> expected = new ArrayList<>();
        expected.add(new BookDto().setAuthor("George Orwell")
                .setId(1L)
                .setTitle("1984")
                .setIsbn("97800")
                .setPrice(BigDecimal.valueOf(299.99))
                .setDescription("Anti-utopia")
                .setCoverImage("1984.jpg")
                .setCategoryIds(Collections.emptySet()));

        MvcResult result = mockMvc.perform(get("/books/search?titles=1984")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookDto[] actual = objectMapper
                .readValue(result.getResponse().getContentAsByteArray(), BookDto[].class);

        Assertions.assertEquals(1, actual.length);
        Assertions.assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @Sql(scripts = "classpath:db/books/add-three-default-books.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/books/remove-all-books.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Search books by params")
    void search_WithNonMatchingParam_ShouldReturnEmptyList() throws Exception {

        MvcResult result = mockMvc.perform(get("/books/search?titles=1985")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookDto[] actual = objectMapper
                .readValue(result.getResponse().getContentAsByteArray(), BookDto[].class);

        Assertions.assertEquals(0, actual.length);
    }
}
