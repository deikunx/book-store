package com.bookstore.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.dto.book.BookDtoWithoutCategoryIds;
import com.bookstore.dto.category.CategoryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
class CategoryControllerTest {

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
    @Sql(scripts = "classpath:db/categories/remove-all-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Create a new category")
    void createCategory_WithValidRequest_ShouldReturnNewCategoryAndStatusCreated()
            throws Exception {
        CategoryDto expected = new CategoryDto()
                .setName("Test")
                .setDescription("Test");

        String jsonRequest = objectMapper.writeValueAsString(expected);

        MvcResult result = mockMvc.perform(post("/categories")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        CategoryDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), CategoryDto.class);

        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @Sql(scripts = "classpath:db/categories/add-three-default-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/categories/remove-all-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get all caregories")
    void getAll_ShouldReturnListOfAllCategoriesAndStatusOk() throws Exception {
        List<CategoryDto> expected = new ArrayList<>();
        expected.add(new CategoryDto()
                .setId(1L)
                .setName("Fiction")
                .setDescription("Fiction books"));
        expected.add(new CategoryDto().setId(2L)
                .setName("Business")
                .setDescription("Business books"));
        expected.add(new CategoryDto().setId(3L)
                .setName("Self-development")
                .setDescription("Self-development books"));

        MvcResult mvcResult = mockMvc.perform(get("/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CategoryDto[] actual = objectMapper
                .readValue(mvcResult.getResponse().getContentAsByteArray(), CategoryDto[].class);
        Assertions.assertEquals(3, actual.length);
        Assertions.assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @Sql(scripts = "classpath:db/categories/add-three-default-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/categories/remove-all-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get category by ID")
    void getCategoryById_ShouldReturnCategoryByIdAndReturnOkStatus() throws Exception {
        Long categoryId = 1L;
        CategoryDto expected = new CategoryDto()
                .setId(categoryId)
                .setName("Fiction")
                .setDescription("Fiction books");
        MvcResult result = mockMvc.perform(get("/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CategoryDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), CategoryDto.class);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @Sql(scripts = "classpath:db/categories/add-three-default-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/categories/remove-all-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Delete category by ID")
    void deleteCategory_ShouldDeleteCategoryByIdAndReturnNoContentStatus() throws Exception {
        Long categoryId = 1L;

        mockMvc.perform(delete("/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @Sql(scripts = "classpath:db/categories/add-three-default-categories.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:db/categories/remove-all-categories.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Update category by ID")
    void updateCategory_ShouldUpdateCategoryAndReturnOkStatus() throws Exception {
        Long categoryId = 3L;

        CategoryDto expected = new CategoryDto().setId(categoryId)
                .setName("Self-development")
                .setDescription("Self-development books");

        MvcResult result = mockMvc.perform(put("/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expected)))
                .andExpect(status().isOk())
                .andReturn();

        CategoryDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), CategoryDto.class);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test

    @Sql(scripts = {
            "classpath:db/books/add-three-default-books.sql",
            "classpath:db/categories/add-three-default-categories.sql",
            "classpath:db/categories/add-books-with-categories.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:db/categories/remove-all-books-with-categories.sql",
            "classpath:db/books/remove-all-books.sql",
            "classpath:db/categories/remove-all-categories.sql",
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Get all books from certain category")
    void getBooksByCategoryId_ShouldReturnAllBooksWithCategoryIdAndStatusOk() throws Exception {
        Long categoryId = 1L;

        List<BookDtoWithoutCategoryIds> expected = new ArrayList<>();

        expected.add(new BookDtoWithoutCategoryIds().setTitle("Book 1")
                .setPrice(BigDecimal.valueOf(19.99))
                .setAuthor("Author 1")
                .setIsbn("ISBN123456789")
                .setDescription("Description 1")
                .setCoverImage("image1.jpg")
                .setId(1L));

        expected.add(new BookDtoWithoutCategoryIds().setTitle("Book 3")
                .setPrice(BigDecimal.valueOf(14.99))
                .setAuthor("Author 3")
                .setIsbn("ISBN456789123")
                .setDescription("Description 3")
                .setCoverImage("image3.jpg")
                .setId(3L));

        MvcResult result = mockMvc.perform(get("/categories/{id}/books", categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookDtoWithoutCategoryIds[] actual = objectMapper
                .readValue(result.getResponse().getContentAsString(),
                        BookDtoWithoutCategoryIds[].class);

        Assertions.assertEquals(expected.size(), actual.length);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }
}
