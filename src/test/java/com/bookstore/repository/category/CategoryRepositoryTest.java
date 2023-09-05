package com.bookstore.repository.category;

import com.bookstore.model.Category;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Find categories by their id")
    void findByIdIn_ShouldReturnListOfCategoriesWithIdFromCategoryIdsSet() {
        Category category1 = new Category();
        category1.setName("Novel");
        Category category2 = new Category();
        category2.setName("Fiction");
        Category category3 = new Category();
        category3.setName("Business");
        categoryRepository.saveAll(List.of(category1, category2, category3));

        Set<Long> categoryIds = Set.of(category1.getId(), category2.getId());
        Set<Category> foundCategories = categoryRepository.findByIdIn(categoryIds);

        assertEquals(2, foundCategories.size());
        assertTrue(foundCategories.contains(category1));
        assertTrue(foundCategories.contains(category2));
    }

    @Test
    @DisplayName("Find all books with categories")
    void findAllWithPages_ShouldReturnListOfAllCategoriesWithPages() {
        Category category1 = new Category();
        category1.setName("Novel");
        Category category2 = new Category();
        category2.setName("Fiction");
        Category category3 = new Category();
        category3.setName("Business");
        categoryRepository.saveAll(List.of(category1, category2, category3));

        Pageable pageable = PageRequest.of(0, 2);

        List<Category> categories = categoryRepository.findAllWithPages(pageable);

        assertEquals(2, categories.size());
    }
}