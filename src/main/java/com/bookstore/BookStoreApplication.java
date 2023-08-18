package com.bookstore;

import com.bookstore.model.Book;
import java.math.BigDecimal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookStoreApplication {

    public static void main(String[] args) {
        Book book = new Book();
        book.setIsbn("ww");
        book.setPrice(BigDecimal.TEN);
        book.setAuthor("Geroge");
        book.setTitle("1984");
        SpringApplication.run(BookStoreApplication.class, args);
    }

}
