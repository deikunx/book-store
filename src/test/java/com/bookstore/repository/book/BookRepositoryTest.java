package com.bookstore.repository.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.repository.category.CategoryRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Find all books from a certain category")
    void findAllByCategoryId_FromSameCategory_ReturnListOfBooksWithSameCategory() {
        Category category = new Category();
        category.setName("Fantasy");
        categoryRepository.save(category);

        Book book1 = new Book();
        book1.setTitle("Test 1");
        book1.setCoverImage("Test");
        book1.setIsbn("Test");
        book1.setAuthor("Test");
        book1.setPrice(BigDecimal.valueOf(10));
        book1.setCategories(Set.of(category));
        bookRepository.save(book1);

        Book book2 = new Book();
        book2.setAuthor("Test");
        book2.setCoverImage("Test");
        book2.setIsbn("Test2");
        book2.setPrice(BigDecimal.valueOf(10));
        book2.setTitle("Test 2");
        book2.setCategories(Collections.emptySet());
        bookRepository.save(book2);

        List<Book> books = bookRepository.findAllByCategoryId(category.getId());
        assertEquals(1, books.size());
    }

    @Test
    @DisplayName("Find book by id")
    void findBookById_ReturnOneBookWithId() {
        Category category = new Category();
        category.setName("Fantasy");
        categoryRepository.save(category);

        Book book = new Book();
        book.setTitle("Test 1");
        book.setCoverImage("Test");
        book.setIsbn("Test");
        book.setAuthor("Test");
        book.setPrice(BigDecimal.valueOf(10));
        book.setCategories(Set.of(category));
        bookRepository.save(book);

        Optional<Book> foundBook = bookRepository.findBookById(book.getId());
        assertTrue(foundBook.isPresent());
        assertEquals("Test 1", foundBook.get().getTitle());
    }

    @Test
    @DisplayName("Find all books with categories")
    void findAllWithCategories_ReturnListOfBooksWithCategories() {
        Category category1 = new Category();
        category1.setName("Fantasy");
        categoryRepository.save(category1);

        Category category2 = new Category();
        category2.setName("Science Fiction");
        categoryRepository.save(category2);

        Book book1 = new Book();
        book1.setTitle("Test 1");
        book1.setCoverImage("Test");
        book1.setIsbn("Test");
        book1.setAuthor("Test");
        book1.setPrice(BigDecimal.valueOf(10));
        bookRepository.save(book1);
        book1.setCategories(Set.of(category1));

        Book book2 = new Book();
        book2.setAuthor("Test");
        book2.setCoverImage("Test");
        book2.setIsbn("Test2");
        book2.setPrice(BigDecimal.valueOf(10));
        book2.setTitle("Test 2");
        book2.setCategories(Set.of(category2));
        bookRepository.save(book2);

        List<Book> books = bookRepository.findAllWithCategories(PageRequest.of(0, 10));
        assertEquals(2, books.size());
    }
}
