package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDTO {
    private Long productId;
    private String productName;
    private Double price;
    private String description;
    private Integer quantity;
    private String image; // or use MultipartFile if uploading
    private Double discount;
    private Double specialPrice;

}
