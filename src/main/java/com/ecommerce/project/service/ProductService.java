package com.ecommerce.project.service;

import com.ecommerce.project.payload.ProductRequestDTO;
import com.ecommerce.project.payload.ProductResponseDTO;

public interface ProductService {
    ProductRequestDTO addProduct(Long categoryId, ProductRequestDTO productRequestDTO);

    ProductResponseDTO getAllProducts();

    ProductResponseDTO searchByCategory(Long categoryId);

    ProductResponseDTO searchProductByKeyword(String keyword);

    ProductRequestDTO updateProduct(Long productId, ProductRequestDTO productRequestDTO);

    ProductRequestDTO deleteProduct(Long productId);
}
