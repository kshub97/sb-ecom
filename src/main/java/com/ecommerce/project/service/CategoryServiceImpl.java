package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService{
    //private List<Category> categories=new ArrayList<>();

    @Autowired
    private CategoryRepository categoryRepository;
    //private Long nextId=1L;

    @Override
    public List<Category> getAllCategories() {
        List<Category> categoryList = categoryRepository.findAll();
        if (categoryList.isEmpty())
        throw new APIException("No Category exist, Please create new category");
        return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
        //category.setCategoryId(nextId++);
        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if (savedCategory!=null)
            throw new APIException("Category with name: "+category.getCategoryName() +" already exist!!!");
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","CategoryId",categoryId));
        categoryRepository.delete(category);
        return "Category With  categoryId " + categoryId +" deleted successfully";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","CategoryId",categoryId));
        existingCategory.setCategoryName(category.getCategoryName());
        return  categoryRepository.save(existingCategory);
    }
}
