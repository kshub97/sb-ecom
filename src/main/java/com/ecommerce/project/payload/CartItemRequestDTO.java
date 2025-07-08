package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemRequestDTO {

    private Long cartItemId;
    private CartRequestDTO cart;
    private ProductRequestDTO productRequestDTO;
    private Integer quantity;
    private double discount;
    private double productPrice;
}
