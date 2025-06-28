package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryRequestDTO;
import com.ecommerce.project.payload.CategoryResponseDTO;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public CategoryResponseDTO getAllCategories(Integer pageNumber, Integer pageSize,String sortOrder, String sortBy) {
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                            ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageable= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page categoryPage= categoryRepository.findAll(pageable);
        List<Category> categoryList = categoryPage.getContent();
        if (categoryList.isEmpty())
           throw new APIException("No Category exist, Please create new category");
        List<CategoryRequestDTO> dtoList = categoryList.stream().map(category -> modelMapper.map(category, CategoryRequestDTO.class)).toList();
        //or use constructor and then return the response dto object if medium small DTOs
/*
        return new CategoryResponseDTO(dtoList,categoryPage.getNumber(),categoryPage.getSize(),
                categoryPage.getTotalElements(),categoryPage.getTotalPages(),categoryPage.isLast()); */
        CategoryResponseDTO response = new CategoryResponseDTO();
        response.setContents(dtoList);
        response.setPageNumber(categoryPage.getNumber());
        response.setPageSize(categoryPage.getSize());
        response.setTotalElements(categoryPage.getTotalElements());
        response.setTotalPages(categoryPage.getTotalPages());
        response.setLastPage(categoryPage.isLast());
        return response;
    }

    @Override
    public CategoryRequestDTO createCategory(CategoryRequestDTO categoryRequest) {
        Category categoryFromDb = categoryRepository.findByCategoryName(categoryRequest.getCategoryName());
        if (categoryFromDb!=null)
            throw new APIException("Category with name: "+categoryRequest.getCategoryName() +" already exist!!!");
        Category mappedCategory = modelMapper.map(categoryRequest, Category.class);
        Category savedCategory = categoryRepository.save(mappedCategory);
        return modelMapper.map(savedCategory,CategoryRequestDTO.class);
    }

    @Override
    public CategoryRequestDTO deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","CategoryId",categoryId));
        categoryRepository.delete(category);

        return modelMapper.map(category,CategoryRequestDTO.class);
    }

    @Override
    public CategoryRequestDTO updateCategory(CategoryRequestDTO categoryRequestDTO, Long categoryId) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","CategoryId",categoryId));
        Category mappedCategory = modelMapper.map(categoryRequestDTO, Category.class);
        mappedCategory.setCategoryId(categoryId);
        existingCategory = categoryRepository.save(mappedCategory);
        return  modelMapper.map(existingCategory,CategoryRequestDTO.class);
    }
}
