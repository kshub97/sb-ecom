package com.ecommerce.project.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDTO {
    private Long productId;

    @NotBlank(message = "productName must not be blank/empty")
    @Size(min = 3,message = "productName should be at least 3 characters")
    private String productName;
    private Double price;

    @NotBlank(message = "description must not be blank/empty")
    @Size(min = 6,message = "description should be at least 6 characters")
    private String description;
    private Integer quantity;
    private String image; // or use MultipartFile if uploading
    private Double discount;
    private Double specialPrice;

}
