package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductRequestDTO;
import com.ecommerce.project.payload.ProductResponseDTO;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.upload.path}")
    private  String path;

    @Override
    public ProductRequestDTO addProduct(Long categoryId, ProductRequestDTO productRequestDTO) {
        Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
        List<Product> products = category.getProduct();
        boolean isProductPresent = products.stream().anyMatch(product -> product.getProductName().equalsIgnoreCase(productRequestDTO.getProductName()));
        if (isProductPresent) {
           throw new APIException("Product with name "+ productRequestDTO.getProductName() + " already exist int this category !");
       }
        Product mappedProduct = modelMapper.map(productRequestDTO, Product.class);
        mappedProduct.setImage("default.png");
        mappedProduct.setDescription(productRequestDTO.getDescription());
        mappedProduct.setCategory(category);
        mappedProduct.setSpecialPrice(productRequestDTO.getPrice() - ((productRequestDTO.getDiscount() * 0.01) * productRequestDTO.getPrice()));
        return modelMapper.map(productRepository.save(mappedProduct), ProductRequestDTO.class);
    }

    @Override
    public ProductResponseDTO getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPages = productRepository.findAll(pageDetails);
        List<Product> productPagesContent = productPages.getContent();
        if (productPagesContent.isEmpty()) throw new APIException("No product exists!");
        List<ProductRequestDTO> productDTOS = productPagesContent.stream().map(
                product -> modelMapper.map(product, ProductRequestDTO.class)).toList();
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContent(productDTOS);
        productResponseDTO.setPageNumber(productPages.getNumber());
        productResponseDTO.setTotalPages(productPages.getTotalPages());
        productResponseDTO.setPageSize(productPages.getSize());
        productResponseDTO.setTotalElements(productPages.getTotalElements());
        productResponseDTO.setLastPage(productPages.isLast());
        return productResponseDTO;
    }

    @Override
    public ProductResponseDTO searchByCategory(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPages = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);
        List<Product> productPagesContent = productPages.getContent();
        List<ProductRequestDTO> productDTOS = productPagesContent.stream().map(
                product -> modelMapper.map(product, ProductRequestDTO.class)).toList();
        if (productPagesContent.isEmpty()) throw new APIException(category.getCategoryName() + " Category does not have any product");
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContent(productDTOS);
        productResponseDTO.setPageNumber(productPages.getNumber());
        productResponseDTO.setTotalPages(productPages.getTotalPages());
        productResponseDTO.setPageSize(productPages.getSize());
        productResponseDTO.setTotalElements(productPages.getTotalElements());
        productResponseDTO.setLastPage(productPages.isLast());
        return productResponseDTO;
    }

    @Override
    public ProductResponseDTO searchProductByKeyword(Integer pageNumber, Integer pageSize, String sortOrder, String sortBy,String keyword) {
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPages = productRepository.findByProductNameLikeIgnoreCase("%"+ keyword +"%", pageDetails);
        List<Product> productPagesContent = productPages.getContent();
        List<ProductRequestDTO> productDTOS = productPagesContent.stream().map(
                product -> modelMapper.map(product, ProductRequestDTO.class)).toList();
        if (productPagesContent.isEmpty()) throw new APIException("Product NOt Found with keyword: "+ keyword);
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContent(productDTOS);
        productResponseDTO.setPageNumber(productPages.getNumber());
        productResponseDTO.setTotalPages(productPages.getTotalPages());
        productResponseDTO.setPageSize(productPages.getSize());
        productResponseDTO.setTotalElements(productPages.getTotalElements());
        productResponseDTO.setLastPage(productPages.isLast());
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

    @Override
    public ProductRequestDTO updateProductImage(Long productId, MultipartFile file) throws IOException {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
        String fileName = fileService.uploadImages(path,file);
        productFromDb.setImage(fileName);
        Product savedProduct = productRepository.save(productFromDb);
        return modelMapper.map(savedProduct,ProductRequestDTO.class);
    }

}
