package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long>{

    Category findByCategoryName(@NotBlank(message = "CategoryName must not be blank/empty") @Size(min = 5,message = "CategoryName should be at least 5 characters") String categoryName);
}
