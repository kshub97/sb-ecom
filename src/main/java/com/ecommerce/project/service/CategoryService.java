package com.ecommerce.project.service;

import com.ecommerce.project.payload.CategoryRequestDTO;
import com.ecommerce.project.payload.CategoryResponseDTO;


public interface CategoryService {
    CategoryResponseDTO getAllCategories(Integer pageNumber, Integer pageSize, String sortOrder, String SortBy);
    CategoryRequestDTO createCategory(CategoryRequestDTO category);

    CategoryRequestDTO deleteCategory(Long categoryId);

    CategoryRequestDTO updateCategory(CategoryRequestDTO category, Long categoryId);
}
