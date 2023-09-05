package com.bookstore.service;

import com.bookstore.dto.category.CategoryDto;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.CategoryMapper;
import com.bookstore.model.Category;
import com.bookstore.repository.category.CategoryRepository;
import com.bookstore.service.impl.CategoryServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("Verify findAllWithPages() method works")
    public void findAllWithPages_ShouldReturnListOfCategories() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("name")));

        List<Category> categoryEntities = new ArrayList<>();
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Category 1");
        category1.setDescription("Description 1");
        categoryEntities.add(category1);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Category 2");
        category2.setDescription("Description 2");
        categoryEntities.add(category2);

        List<CategoryDto> expectedCategoryDtos = new ArrayList<>();
        CategoryDto categoryDto1 = new CategoryDto();
        categoryDto1.setId(1L);
        categoryDto1.setName("Category 1");
        categoryDto1.setDescription("Description 1");
        expectedCategoryDtos.add(categoryDto1);

        CategoryDto categoryDto2 = new CategoryDto();
        categoryDto2.setId(2L);
        categoryDto2.setName("Category 2");
        categoryDto2.setDescription("Description 2");
        expectedCategoryDtos.add(categoryDto2);

        when(categoryRepository.findAllWithPages(pageable)).thenReturn(categoryEntities);
        when(categoryMapper.toDto(category1)).thenReturn(categoryDto1);
        when(categoryMapper.toDto(category2)).thenReturn(categoryDto2);

        List<CategoryDto> result = categoryService.findAllWithPages(pageable);

        verify(categoryRepository).findAllWithPages(pageable);
        verify(categoryMapper).toDto(category1);
        verify(categoryMapper).toDto(category2);

        assertEquals(expectedCategoryDtos, result);
    }

    @Test
    @DisplayName("Verify getById() method works")
    public void getById_WithValidId_ShouldReturnCategory() {
        Long categoryId = 1L;
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(categoryId);
        categoryDto.setName("Sample Category");
        categoryDto.setDescription("Sample Description");

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Sample Category");
        existingCategory.setDescription("Sample Description");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryMapper.toDto(existingCategory)).thenReturn(categoryDto);

        CategoryDto result = categoryService.getById(categoryId);

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toDto(existingCategory);

        assertEquals(categoryDto, result);
    }

    @Test
    @DisplayName("Verify getById() method throws exception when category not found")
    public void getById_WithInvalidId_ShouldThrowEntityNotFoundException() {
        Long categoryId = 1L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.getById(categoryId));

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Verify save() method works")
    public void save_SuccessfulSave() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Fiction");
        categoryDto.setDescription("Fiction books");

        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());

        when(categoryMapper.toEntity(categoryDto)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        CategoryDto actual = categoryService.save(categoryDto);

        assertEquals(categoryDto, actual);
    }

    @Test
    @DisplayName("Verify update() method works")
    public void update_WithValidId_SuccessfulUpdate() {
        Long categoryId = 1L;
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(categoryId);
        categoryDto.setName("Updated Category");
        categoryDto.setDescription("Updated Description");

        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Old Category");
        existingCategory.setDescription("Old Description");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(existingCategory)).thenReturn(existingCategory); // Можно изменить на возвращение обновленной сущности
        when(categoryMapper.toDto(existingCategory)).thenReturn(categoryDto);

        CategoryDto result = categoryService.update(categoryId, categoryDto);

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toDto(existingCategory);

        assertEquals(categoryDto, result);
    }

    @Test
    @DisplayName("Verify update() method throws exception when category not found")
    public void update_WithInvalidId_ShouldThrowEntityNotFoundException() {
        Long categoryId = 1L;
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(categoryId);
        categoryDto.setName("Updated Category");
        categoryDto.setDescription("Updated Description");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.update(categoryId, categoryDto));

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Verify deleteById() method works")
    public void deleteById_SuccessfulDelete() {
        Long categoryId = 1L;

        categoryService.deleteById(categoryId);

        verify(categoryRepository).deleteById(categoryId);
    }
}