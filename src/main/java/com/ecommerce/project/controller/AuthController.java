package com.ecommerce.project.controller;


import com.ecommerce.project.configuration.AppConstants;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    //AuthenticationManager, which performs the actual username-password authentication, kick off authentication process
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
        AuthenticationResult authenticationResult = authService.login(loginRequest);
        //here in response header we are setting cookie
        return  ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                authenticationResult.getJwtCookie().toString()).body(authenticationResult.getResponse());
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
       return authService.register(signupRequest);
    }

    @GetMapping("/username")
    public String currentUserName(Authentication authentication){
        if (authentication != null)
            return authentication.getName();
        else
            return "";
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(Authentication authentication){
        return ResponseEntity.ok().body(authService.getCurrentUserDetails(authentication));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser(){
        ResponseCookie responseCookie = authService.logoutUser();

        //here in response header we are setting cookie
        return  ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
                String.valueOf(responseCookie)).body(new MessageResponse("You've been signed out!"));
    }

    @GetMapping("/sellers")
    public ResponseEntity<?> getAllSellers(@RequestParam(value = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                 @RequestParam(value = "pageSize",defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                 @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_USER_BY) String sortBy,
                                                 @RequestParam(value = "sortOrder",defaultValue = AppConstants.SORT_DIR) String sortOrder) {
        UserResponse allSellers = authService.getAllSellers(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(allSellers, HttpStatus.OK);
    }
}
