package com.ecommerce.project.controller;

import com.ecommerce.project.configuration.AppConstants;
import com.ecommerce.project.payload.CategoryRequestDTO;
import com.ecommerce.project.payload.CategoryResponseDTO;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    //@GetMapping("/public/categories")
    @RequestMapping(method = RequestMethod.GET,value = "/public/categories")
    public ResponseEntity<CategoryResponseDTO> getAllCategories(@RequestParam(value = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                @RequestParam(value = "pageSize",defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                @RequestParam(value = "sortBy",defaultValue = AppConstants.SORT_CATEGORY_BY) String sortBy,
                                                                @RequestParam(value = "sortOrder",defaultValue = AppConstants.SORT_DIR) String sortOrder) {
        CategoryResponseDTO allCategories = categoryService.getAllCategories(pageNumber, pageSize, sortOrder, sortBy);
        return new ResponseEntity<>(allCategories,HttpStatus.OK);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryRequestDTO> createCategory(@Valid @RequestBody CategoryRequestDTO categoryRequestDTO){;
        CategoryRequestDTO savedCategoryDTO = categoryService.createCategory(categoryRequestDTO);
        return  new ResponseEntity<>(savedCategoryDTO,HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryRequestDTO> deleteCategory(@PathVariable Long categoryId){
        CategoryRequestDTO deletedCategory = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(deletedCategory,HttpStatus.OK);

    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryRequestDTO> updateCategory(@Valid @RequestBody CategoryRequestDTO categoryRequestDTO, @PathVariable Long categoryId){
        CategoryRequestDTO updateCategoryDTO = categoryService.updateCategory(categoryRequestDTO, categoryId);
            return new ResponseEntity<>(updateCategoryDTO,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET,value = "/admin/categories")
    public ResponseEntity<CategoryResponseDTO> getAllCategoriesForAdmin(@RequestParam(value = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                @RequestParam(value = "pageSize",defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                @RequestParam(value = "sortBy",defaultValue = AppConstants.SORT_CATEGORY_BY) String sortBy,
                                                                @RequestParam(value = "sortOrder",defaultValue = AppConstants.SORT_DIR) String sortOrder) {
        CategoryResponseDTO allCategories = categoryService.getAllCategories(pageNumber, pageSize, sortOrder, sortBy);
        return new ResponseEntity<>(allCategories,HttpStatus.OK);
    }

    @PutMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryRequestDTO> updateCategoryForAdmin(@Valid @RequestBody CategoryRequestDTO categoryRequestDTO, @PathVariable Long categoryId){
        CategoryRequestDTO updateCategoryDTO = categoryService.updateCategory(categoryRequestDTO, categoryId);
        return new ResponseEntity<>(updateCategoryDTO,HttpStatus.OK);
    }

    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryRequestDTO> createCategoryForAdmin(@Valid @RequestBody CategoryRequestDTO categoryRequestDTO){;
        CategoryRequestDTO savedCategoryDTO = categoryService.createCategory(categoryRequestDTO);
        return  new ResponseEntity<>(savedCategoryDTO,HttpStatus.CREATED);
    }

}
