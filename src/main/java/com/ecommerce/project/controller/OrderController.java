package com.ecommerce.project.controller;

import com.ecommerce.project.configuration.AppConstants;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.StripeService;
import com.ecommerce.project.util.AuthUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StripeService stripeService;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderResponseDTO> orderProducts(@PathVariable String paymentMethod ,
                                                          @RequestBody OrderRequestDTO orderRequestDTO){
        System.out.println("Frontend data : " +orderRequestDTO);
        String loggedInEmail = authUtil.loggedInEmail();

        //[Product] → added to → [CartItem in Cart] -> checkout (create order) -> [CartItem] → converted to → [OrderItem]

        OrderResponseDTO order= orderService.placeOrder(loggedInEmail,paymentMethod,orderRequestDTO);

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }


    @PostMapping("/order/stripe-client-secret")
    public ResponseEntity<String> createStripClientSecret(@RequestBody StripePaymentDTO stripePaymentDTO) throws StripeException {
        System.out.println("stripePaymentDTO Received : " + stripePaymentDTO);
        PaymentIntent paymentIntent = stripeService.paymentIntent(stripePaymentDTO);
        return new ResponseEntity<>(paymentIntent.getClientSecret(),HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET,value = "/admin/orders")
    public ResponseEntity<OrderReceivedResponseDTO> getAllReceivedOrder(@RequestParam(value = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                        @RequestParam(value = "pageSize",defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                        @RequestParam(value = "sortBy",defaultValue = AppConstants.SORT_ORDER_BY) String sortBy,
                                                                        @RequestParam(value = "sortOrder",defaultValue = AppConstants.SORT_DIR) String sortOrder) {
        OrderReceivedResponseDTO orderReceivedResponseDTO = orderService.getAllReceivedOrder(pageNumber, pageSize, sortOrder, sortBy);
        return new ResponseEntity<>(orderReceivedResponseDTO,HttpStatus.OK);
    }

    @PutMapping("/admin/orders/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(@PathVariable Long orderId,
                                                    @RequestBody OrderStatusUpdateDTO orderStatusUpdateDTO) throws StripeException {
        OrderResponseDTO orderResponseDTO = orderService.updateOrder(orderId, orderStatusUpdateDTO.getUpdateStatus());
        return new ResponseEntity<OrderResponseDTO>(orderResponseDTO,HttpStatus.OK);
    }

}
