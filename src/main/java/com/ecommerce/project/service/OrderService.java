package com.ecommerce.project.service;

import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.payload.OrderResponseDTO;
import jakarta.transaction.Transactional;

public interface OrderService {
    @Transactional
    OrderResponseDTO placeOrder(String loggedInEmail, String paymentMethod, OrderRequestDTO orderRequestDTO);
}
