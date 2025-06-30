package com.ecommerce.project.service;

import com.ecommerce.project.payload.ProductRequestDTO;
import com.ecommerce.project.payload.ProductResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductRequestDTO addProduct(Long categoryId, ProductRequestDTO productRequestDTO);

    ProductResponseDTO getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductResponseDTO searchByCategory(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, Long categoryId);

    ProductResponseDTO searchProductByKeyword(Integer pageNumber, Integer pageSize, String sortOrder , String sortBy, String keyword);

    ProductRequestDTO updateProduct(Long productId, ProductRequestDTO productRequestDTO);

    ProductRequestDTO deleteProduct(Long productId);

    ProductRequestDTO updateProductImage(Long productId, MultipartFile file) throws IOException;

}
