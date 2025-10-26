package com.ecommerce.project.service;

import com.ecommerce.project.payload.OrderReceivedResponseDTO;
import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.payload.OrderResponseDTO;
import jakarta.transaction.Transactional;

public interface OrderService {
    @Transactional
    OrderResponseDTO placeOrder(String loggedInEmail, String paymentMethod, OrderRequestDTO orderRequestDTO);

    OrderReceivedResponseDTO getAllReceivedOrder(Integer pageNumber, Integer pageSize, String sortOrder, String sortBy);

    OrderResponseDTO updateOrder(Long orderId, String orderStatusUpdateDTO);
}
