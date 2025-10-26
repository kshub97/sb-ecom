package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderResponseDTO placeOrder(String loggedInEmail, String paymentMethod, OrderRequestDTO orderRequestDTO) {
        Long addressId = orderRequestDTO.getAddressId();
        String pgName = orderRequestDTO.getPgName();
        String pgPaymentId = orderRequestDTO.getPgPaymentId();
        String pgStatus = orderRequestDTO.getPgStatus();
        String pgResponseMessage = orderRequestDTO.getPgResponseMessage();

        //1. Getting   User Cart , need as later this will be converted to Order
        Cart userCart = cartRepository.findCartByEmail(loggedInEmail);
        if (userCart==null) throw new ResourceNotFoundException("Cart","Email",loggedInEmail);

        //In order we have address object so get address
        Address address = addressRepository.findById(addressId).orElseThrow(
                () -> new ResourceNotFoundException("Address","addressId",addressId) );

        //Retrieve using constructor  and save payment in db
        Payment payment = new Payment(paymentMethod,pgPaymentId, pgStatus, pgResponseMessage, pgName);

    //2.Create a new order with payment info
        //Set Order
        Order order = new Order();
        order.setEmail(loggedInEmail);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(userCart.getTotalPrice());
        order.setOrderStatus("Accepted");
        order.setAddress(address);
        
        //Set order in payment entity as it is bidirectional
        payment.setOrder(order);
        Payment savePayment = paymentRepository.save(payment);
        order.setPayment(savePayment);
        Order savedOrder = orderRepository.save(order);
        //Order is saved, but it has order items as well need to set that too

        //###Now after placing order post steps like update stock , clear cartItem list
    //3. Get Items from cart map into the  order items
        //From cart get cartItems and then for each field set the orderItem ,bcz cartItems later becomes orderItem
        List<CartItem> cartItemList = userCart.getCartItemList();
        if (cartItemList.isEmpty()) throw new APIException("Cart is Empty !.");

        //Set order Item field and save it
        List<OrderItem> orderItems = cartItemList.stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setOrder(savedOrder);
            return orderItem;
        }).toList();

         orderItems = orderItemRepository.saveAll(orderItems);

        //4.update product stock(get the quantity from cartItem first which was ordered and then update product)
        userCart.getCartItemList().forEach(cartItem ->{
            int quantity = cartItem.getQuantity();
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);

            //5. Clear the  cart after order is placed
            cartService.deleteProductFromCart(userCart.getCartId(), product.getProductId());
        });

        //6. Send back the order summary(OrderResponseDTO)
        OrderResponseDTO orderResponseDTO = modelMapper.map(savedOrder, OrderResponseDTO.class);
        List<OrderItemDTO> orderItemDTOS = orderItems.stream().map(
                (item) -> modelMapper.map(item, OrderItemDTO.class)).toList();
        orderResponseDTO.setOrderItems(orderItemDTOS);
        orderResponseDTO.setAddressId(addressId);
        return orderResponseDTO;
    }

    @Override
    public OrderReceivedResponseDTO getAllReceivedOrder(Integer pageNumber,
                                                        Integer pageSize, String sortOrder, String sortBy) {
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Order> pageOrders = orderRepository.findAll(pageDetails); //so in result pagination will be included
        List<Order> orders = pageOrders.getContent(); //get order data in list
        //Now using mapper convert each to orderResponseDTO
        List<OrderResponseDTO> orderDTOS = orders.stream().map((order) -> modelMapper.map(order, OrderResponseDTO.class)).toList();

        OrderReceivedResponseDTO orderReceivedResponseDTO = new OrderReceivedResponseDTO();
            orderReceivedResponseDTO.setContent(orderDTOS);
            orderReceivedResponseDTO.setPageNumber(pageOrders.getNumber());
            orderReceivedResponseDTO.setTotalPages(pageOrders.getTotalPages());
            orderReceivedResponseDTO.setTotalElements(pageOrders.getTotalElements());
            orderReceivedResponseDTO.setPageSize(pageOrders.getSize());
            orderReceivedResponseDTO.setLastPage(pageOrders.isLast());

        return orderReceivedResponseDTO;
    }

    @Override
    public OrderResponseDTO updateOrder(Long orderId, String orderStatusUpdateDTO) {
        Order order = orderRepository.findById(orderId).
                orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        order.setOrderStatus(orderStatusUpdateDTO);
        orderRepository.save(order);
       return modelMapper.map(order, OrderResponseDTO.class);
    }

}
