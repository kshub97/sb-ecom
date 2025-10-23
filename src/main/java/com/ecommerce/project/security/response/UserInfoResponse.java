package com.ecommerce.project.security.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserInfoResponse {

    private Long Id;
    private String jwtToken;
    private String username;
    private String email;
    private List<String> roles;

    public UserInfoResponse(Long id, String username, String email, List<String> roles,String jwtToken) {
        this.Id = id;
        this.jwtToken = jwtToken;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public UserInfoResponse(Long id, String username,String email,  List<String> roles) {
        this.Id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}