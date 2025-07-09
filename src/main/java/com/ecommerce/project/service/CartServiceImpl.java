package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartItemDTO;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductRequestDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {

        //Find or create the user's cart
        Cart cart = createCart();

        //Retrieve Product Details
        Product dbProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        //Perform Validation like product already exist in cart, out of stock etc., here quantity represent stock
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());

        if (cartItem!=null) throw new APIException("Product " + dbProduct.getProductName() + " already exists in the cart");

        if (dbProduct.getQuantity()==0) throw new APIException(dbProduct.getProductName() + " is not available.");

        if (dbProduct.getQuantity()<quantity) throw new APIException("Please, make an order of the " + dbProduct.getProductName()
                + " less than or equal to the quantity " + dbProduct.getQuantity() + ".");

        //Create a new CartItem and add to cart
        CartItem newCartItem= new CartItem();
        newCartItem.setCart(cart);
        newCartItem.setProduct(dbProduct);
        newCartItem.setQuantity(quantity);
        newCartItem.setProductPrice(dbProduct.getSpecialPrice());
        newCartItem.setDiscount(dbProduct.getDiscount());

        //Save Cart Item
        cartItemRepository.save(newCartItem);

        //save cart with total price
        cart.setTotalPrice(cart.getTotalPrice() + (dbProduct.getSpecialPrice() * quantity));
        cartRepository.save(cart);


        //to update product from stock in db after user add to cart , we can do this while placing order or here u can do
        //dbProduct.setQuantity(dbProduct.getQuantity()-quantity);

        //Map the cart entity to a DTO (`CartRequestDTO`)
        CartDTO mapCartDTO = modelMapper.map(cart, CartDTO.class);

        // Add the new item manually to the in-memory list,Ensures cartItems has the latest entry, else it will fetch the
        //earlier cartItem list which might be empty or previous data present in db
        cart.getCartItemList().add(newCartItem);

       // Map each CartItem to a ProductRequestDTO, including product details and quantity, to set in CartDTO
         Stream<ProductRequestDTO> productStream = getProductRequestDTOStream(cart);


        //Add product DTOs to CartRequestDTO and return
        mapCartDTO.setProducts(productStream.toList());

        //Return updated Cart
        return mapCartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) throw new APIException("No cart exists");

        // To populate the CartDTO, extract product details from each CartItem.
        List<CartDTO> cartDTOS = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            // Each CartItem contains a Product, quantity, and pricing info.
            // Map the Product to a ProductDTO, set the quantity from CartItem,
            // and collect them into the product list inside CartDTO.
            List<ProductRequestDTO> products = cart.getCartItemList().stream().map(cartItem -> {
                ProductRequestDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductRequestDTO.class);
                productDTO.setQuantity(cartItem.getQuantity()); // Set the quantity from CartItem
                return productDTO;
            }).toList();
            cartDTO.setProducts(products);
            return cartDTO;
        }).toList();
    return cartDTOS;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId,cartId);
        if (cart==null) throw new ResourceNotFoundException("Cart", "cartId", cartId);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        //to show quantity in cart else if omit this it will reflect quantity in product table
        cart.getCartItemList().forEach(
                item ->item.getProduct().setQuantity(item.getQuantity()) );

        //cartDTO has productDTO list so need to map it
        List<ProductRequestDTO> products = cart.getCartItemList().stream().map(cartItem ->
            modelMapper.map(cartItem.getProduct(), ProductRequestDTO.class)
        ).toList();
        cartDTO.setProducts(products);

        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {

        String loggedInEmail = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(loggedInEmail);
        Long cartId = userCart.getCartId();
        Cart cart = cartRepository.findById(cartId).orElseThrow(
                () -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Product", "productId", productId));

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);
        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        // Calculate new quantity
        int newQuantity = cartItem.getQuantity() + quantity;
        // Validation to prevent negative quantities
        if (newQuantity < 0) {
            throw new APIException("The resulting quantity cannot be negative.");
        }
        if (newQuantity==0){
            deleteProductFromCart(cartId,productId);
        }else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(newQuantity);
            cartItem.setDiscount(product.getDiscount());

            //calculate total price after updating items in cart
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));

            cartRepository.save(cart);
        }

        CartItem updatedItem = cartItemRepository.save(cartItem);

        if(updatedItem.getQuantity()==0){
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        Stream<ProductRequestDTO> productRequestDTOStream = getProductRequestDTOStream(cart);

        //Set product list in cartDto
        cartDTO.setProducts(productRequestDTOStream.toList());

        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        //First retrieve check cart and cartItem
        Cart cart = cartRepository.findById(cartId).orElseThrow(
                () -> new ResourceNotFoundException("Cart", "cartId", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }
        //Before deleting update total cart price
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(productId,cartId);
        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }


    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (userCart!=null) return  userCart;
        Cart newCart=new Cart();
        newCart.setTotalPrice(0.00);
        newCart.setUser(authUtil.loggedInUser());
        return cartRepository.save(newCart);
    }


    private Stream<ProductRequestDTO> getProductRequestDTOStream(Cart cart) {
        List<CartItem> cartItems = cart.getCartItemList();
        //CartItem Dto have Product Dto so need to map product here along with quantity and set this as list in Cart DTO
        Stream<ProductRequestDTO> productStream = cartItems.stream().map(item -> {
            ProductRequestDTO mappedProduct = modelMapper.map(item.getProduct(), ProductRequestDTO.class);
            mappedProduct.setQuantity(item.getQuantity());
            return mappedProduct;
        });
        return productStream;
    }
}
