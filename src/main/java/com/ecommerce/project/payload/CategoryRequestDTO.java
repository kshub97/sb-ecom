package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CategoryRequestDTO {

    private Long categoryId;
    @NotBlank(message = "CategoryName must not be blank/empty")
    @Size(min = 5,message = "CategoryName should be at least 5 characters")
    private String categoryName;
}
