package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponseDTO;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.OrderItemRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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

    @Value("${image.base.url}")
    private String imageBaseUrl;
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
        List<Product> products = category.getProduct();
        boolean isProductPresent = products.stream().anyMatch(product -> product.getProductName().equalsIgnoreCase(productDTO.getProductName()));
        if (isProductPresent) {
           throw new APIException("Product with name "+ productDTO.getProductName() + " already exist int this category !");
       }
        Product mappedProduct = modelMapper.map(productDTO, Product.class);
        mappedProduct.setImage("default.png");
        mappedProduct.setDescription(productDTO.getDescription());
        mappedProduct.setCategory(category);
        mappedProduct.setSpecialPrice(productDTO.getPrice() - ((productDTO.getDiscount() * 0.01) * productDTO.getPrice()));
        return modelMapper.map(productRepository.save(mappedProduct), ProductDTO.class);
    }

    @Override
    public ProductResponseDTO getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String keyword, String category) {
        // Create Pageable with dynamic sorting
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        // Build dynamic specifications
            //You build filters dynamically at runtime.Easy to combine multiple optional filters using .and() / .or().
            //Works nicely with pagination and sorting via Pageable.More maintainable for complex filtering scenarios.
            //Dynamic sorting is tricky — JPQL doesn’t allow dynamic ORDER BY easily without Specification or Criteria API.
        Specification<Product> spec = null;
        if (keyword!=null && !keyword.isEmpty()){
           spec = (root, query, criteriaBuilder) ->
                    criteriaBuilder.like((criteriaBuilder.lower(root.get("productName"))),"%"+ keyword.toLowerCase() + "%");
        }
        if (category!=null && !category.isEmpty()){
           spec =  (root, query, criteriaBuilder) ->
                    criteriaBuilder.like((root.get("category").get("categoryName")),category);
        }

        // Fetch page from repository
        Page<Product> productPages = productRepository.findAll(spec,pageDetails);  //keep 1st arg as specification as it is expected in JpaSpecification
        List<Product> productPagesContent = productPages.getContent();
        if (productPagesContent.isEmpty()) throw new APIException("No product exists!");

        // Map entities to DTOs
        List<ProductDTO> productDTOS = productPagesContent.stream().map(
                        product -> {
                            ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
                            productDTO.setImage(ConstructImageUrl(productDTO.getImage()));
                            return productDTO;
                        }
                ).toList();

        // Prepare response
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContent(productDTOS);
        productResponseDTO.setPageNumber(productPages.getNumber());
        productResponseDTO.setTotalPages(productPages.getTotalPages());
        productResponseDTO.setPageSize(productPages.getSize());
        productResponseDTO.setTotalElements(productPages.getTotalElements());
        productResponseDTO.setLastPage(productPages.isLast());
        return productResponseDTO;
    }

    private String ConstructImageUrl(String imageName){
        return imageBaseUrl.endsWith("/")?imageBaseUrl + imageName : imageBaseUrl + "/" +imageName;
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
        List<ProductDTO> productDTOS = productPagesContent.stream().map(
                product -> modelMapper.map(product, ProductDTO.class)).toList();
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
    public ProductResponseDTO searchProductByKeyword(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder,String keyword) {
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPages = productRepository.findByProductNameLikeIgnoreCase("%"+ keyword +"%", pageDetails);
        List<Product> productPagesContent = productPages.getContent();
        List<ProductDTO> productDTOS = productPagesContent.stream().map(
                product -> modelMapper.map(product, ProductDTO.class)).toList();
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
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
        productFromDb.setProductName(productDTO.getProductName());
        productFromDb.setDescription(productDTO.getDescription());
        productFromDb.setStockQuantity(productDTO.getStockQuantity());
        productFromDb.setDiscount(productDTO.getDiscount());
        productFromDb.setPrice(productDTO.getPrice());
        productFromDb.setSpecialPrice(productDTO.getSpecialPrice());
        Product savedProduct = productRepository.save(productFromDb);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
        boolean existsInOrders  = orderItemRepository.existsById(productId);
//should NOT delete a product if it’s already part of an order, because those orders are historical records.
        if (existsInOrders) {
            throw new APIException("Cannot delete product: It has existing order references");
        }
        productRepository.delete(productFromDb);
        return modelMapper.map(productFromDb, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile file) throws IOException {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
        String fileName = fileService.uploadImages(path,file);
        productFromDb.setImage(fileName);
        Product savedProduct = productRepository.save(productFromDb);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponseDTO getAllProductsForAdmin(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Create Pageable with dynamic sorting
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);

        // Fetch page from repository
        Page<Product> productPages = productRepository.findAll(pageDetails);  //keep 1st arg as specification as it is expected in JpaSpecification
        List<Product> productPagesContent = productPages.getContent();
        if (productPagesContent.isEmpty()) throw new APIException("No product exists!");

        // Map entities to DTOs
        List<ProductDTO> productDTOS = productPagesContent.stream().map(
                product -> {
                    ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
                    productDTO.setImage(ConstructImageUrl(productDTO.getImage()));
                    return productDTO;
                }
        ).toList();

        // Prepare response
        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContent(productDTOS);
        productResponseDTO.setPageNumber(productPages.getNumber());
        productResponseDTO.setTotalPages(productPages.getTotalPages());
        productResponseDTO.setPageSize(productPages.getSize());
        productResponseDTO.setTotalElements(productPages.getTotalElements());
        productResponseDTO.setLastPage(productPages.isLast());
        return productResponseDTO;
    }

}
