package com.ecommerce.project.controller;

import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.payload.OrderResponseDTO;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.util.AuthUtil;
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

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderResponseDTO> orderProducts(@PathVariable String paymentMethod ,
                                                          @RequestBody OrderRequestDTO orderRequestDTO){
        String loggedInEmail = authUtil.loggedInEmail();

        //[Product] → added to → [CartItem in Cart] -> checkout (create order) -> [CartItem] → converted to → [OrderItem]

        OrderResponseDTO order= orderService.placeOrder(loggedInEmail,paymentMethod,orderRequestDTO);

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}
