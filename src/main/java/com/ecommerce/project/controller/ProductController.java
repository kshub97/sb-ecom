package com.ecommerce.project.controller;

import com.ecommerce.project.configuration.AppConstants;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductRequestDTO;
import com.ecommerce.project.payload.ProductResponseDTO;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductRequestDTO> addProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO,
                                                        @PathVariable Long categoryId){
        ProductRequestDTO addedProduct = productService.addProduct(categoryId, productRequestDTO);
        return new ResponseEntity<>(addedProduct, HttpStatus.CREATED);
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponseDTO> getAllProducts(@RequestParam(value = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
                                                             @RequestParam(value = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
                                                             @RequestParam(value = "sortBy",defaultValue = AppConstants.SORT_PRODUCT_BY,required = false) String sortBy,
                                                             @RequestParam(value = "sortOrder",defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder){
        ProductResponseDTO productResponse = productService.getAllProducts(pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponseDTO> getProductByCategory(@RequestParam(value = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
                                                                   @RequestParam(value = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
                                                                   @RequestParam(value = "sortBy",defaultValue = AppConstants.SORT_PRODUCT_BY,required = false) String sortBy,
                                                                   @RequestParam(value = "sortOrder",defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder,
                                                                   @PathVariable Long categoryId){
        ProductResponseDTO productResponse = productService.searchByCategory(pageNumber,pageSize,sortBy,sortOrder,categoryId);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponseDTO> getProductByKeyword(@RequestParam(value = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
                                                                  @RequestParam(value = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
                                                                  @RequestParam(value = "sortBy",defaultValue = AppConstants.SORT_PRODUCT_BY,required = false) String sortBy,
                                                                  @RequestParam(value = "sortOrder",defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder,
                                                                  @PathVariable String keyword){
        ProductResponseDTO productResponse = productService.searchProductByKeyword(pageNumber,pageSize,sortBy,sortOrder,keyword);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductRequestDTO> updateProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO,
                                                            @PathVariable Long productId){
        ProductRequestDTO savedProduct = productService.updateProduct(productId,productRequestDTO);
        return new ResponseEntity<>(savedProduct, HttpStatus.OK);
    }

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductRequestDTO> deleteProduct(@PathVariable Long productId){
        ProductRequestDTO deletedProduct = productService.deleteProduct(productId);
        return new ResponseEntity<>(deletedProduct, HttpStatus.OK);
    }


    @PutMapping("/products/{productId}/image")
    public  ResponseEntity<ProductRequestDTO> updateProductImage(@PathVariable Long productId,
                                                             @RequestParam("image")MultipartFile file) throws IOException {
        ProductRequestDTO updatedProduct = productService.updateProductImage(productId,file);
        return new ResponseEntity<>(updatedProduct,HttpStatus.OK);

    }


}
