package com.ecommerce.project.service;

import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductRequestDTO;
import com.ecommerce.project.payload.ProductResponseDTO;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProductRequestDTO addProduct(Long categoryId, ProductRequestDTO productRequestDTO) {
        Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
        Product mappedProduct = modelMapper.map(productRequestDTO, Product.class);
        mappedProduct.setImage("default.png");
        mappedProduct.setDescription(productRequestDTO.getDescription());
        mappedProduct.setCategory(category);
        mappedProduct.setSpecialPrice(productRequestDTO.getPrice()- ((productRequestDTO.getDiscount()*0.01)* productRequestDTO.getPrice()));
        return modelMapper.map(productRepository.save(mappedProduct),ProductRequestDTO.class);
    }

    @Override
    public ProductResponseDTO getAllProducts() {
        List<Product> productList = productRepository.findAll();
        List<ProductRequestDTO> productDTOS = productList.stream().map(
                product -> modelMapper.map(product, ProductRequestDTO.class)).toList();
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContent(productDTOS);
        return productResponseDTO;
    }

    @Override
    public ProductResponseDTO searchByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
        List<Product> productList = productRepository.findByCategoryOrderByPriceAsc(category);
        List<ProductRequestDTO> productDTOS = productList.stream().map(
                product -> modelMapper.map(product, ProductRequestDTO.class)).toList();
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContent(productDTOS);
        return productResponseDTO;
    }

    @Override
    public ProductResponseDTO searchProductByKeyword(String keyword) {
        List<Product> productList = productRepository.findByProductNameLikeIgnoreCase("%"+ keyword +"%");
        List<ProductRequestDTO> productDTOS = productList.stream().map(
                product -> modelMapper.map(product, ProductRequestDTO.class)).toList();
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContent(productDTOS);
        return productResponseDTO;
    }

    @Override
    public ProductRequestDTO updateProduct(Long productId, ProductRequestDTO productRequestDTO) {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
        productFromDb.setProductName(productRequestDTO.getProductName());
        productFromDb.setDescription(productRequestDTO.getDescription());
        productFromDb.setQuantity(productRequestDTO.getQuantity());
        productFromDb.setDiscount(productRequestDTO.getDiscount());
        productFromDb.setPrice(productRequestDTO.getPrice());
        productFromDb.setSpecialPrice(productRequestDTO.getSpecialPrice());
        Product savedProduct = productRepository.save(productFromDb);
        return modelMapper.map(savedProduct,ProductRequestDTO.class);
    }

    @Override
    public ProductRequestDTO deleteProduct(Long productId) {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
        productRepository.delete(productFromDb);
        return modelMapper.map(productFromDb,ProductRequestDTO.class);
    }

}
