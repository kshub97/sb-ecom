package com.ecommerce.project.payload;

import com.ecommerce.project.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    //sellers field
    private Long userId;
    private String userName;
    private String email;
    private String password;
    private Set<Role> roles = new HashSet<>();
    //If seller is user so address and cart added.
    private AddressDTO address;
    private CartDTO cart;
}