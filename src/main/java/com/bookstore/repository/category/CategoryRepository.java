package com.bookstore.repository.category;

import com.bookstore.model.Category;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Set<Category> findByIdIn(Set<Long> categoryIds);

    @Query("SELECT c FROM Category c")
    List<Category> findAllWithPages(Pageable pageable);
}
