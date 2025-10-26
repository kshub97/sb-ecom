package com.ecommerce.project.service;

import com.ecommerce.project.payload.AnalyticsResponseDTO;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsServiceImpl implements AnalyticsService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;


    @Override
    public AnalyticsResponseDTO getAnalyticsData() {

        AnalyticsResponseDTO analyticsResponseDTO = new AnalyticsResponseDTO();

        long productCount = productRepository.count();
        long totalOrders = orderRepository.count();
        Double totalRevenue = orderRepository.getTotalRevenue();
        analyticsResponseDTO.setProductCount(String.valueOf(productCount));
        analyticsResponseDTO.setTotalOrders(String.valueOf(totalOrders));
        analyticsResponseDTO.setTotalRevenue(String.valueOf(totalRevenue != null ? totalRevenue : 0));
        return analyticsResponseDTO;
    }
}
