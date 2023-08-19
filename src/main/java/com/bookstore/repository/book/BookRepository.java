package com.bookstore.repository.book;

import com.bookstore.model.Book;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.title = :title, b.author = :author, "
            + "b.isbn = :isbn, b.price = :price, b.description = :description, "
            + "b.coverImage = :coverImage WHERE b.id = :id AND b.isDeleted = FALSE")
    void updateBookById(@Param(value = "id") Long id,
                        @Param(value = "title") String title,
                        @Param(value = "author") String author,
                        @Param(value = "isbn") String isbn,
                        @Param(value = "price") BigDecimal price,
                        @Param(value = "description") String description,
                        @Param(value = "coverImage") String coverImage);
}
