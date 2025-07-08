package com.ecommerce.project.controller;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    //AuthenticationManager, which performs the actual username-password authentication, kick off authentication process
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
        Authentication authentication;
        try{
            authentication=authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }
        //✅This line stores the authentication object into Spring's SecurityContext,
        // which keeps track of who is currently logged in.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //  On success, get user info like Who logged in, what roles .Extract authenticated user details
        // Here we are using our own custom user detail we have implemented not inbuilt user detail
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate JWT token for the user
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority()).toList();

        //setting jwt from cookie to string so in response no null for jwt field shows
        UserInfoResponse response=new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),roles, jwtCookie.getValue());

        //here in response header we are setting cookie
        return  ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, String.valueOf(jwtCookie)).body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
        if (userRepository.existsByUsername(signupRequest.getUsername()))
            return ResponseEntity.badRequest().
                    body(new MessageResponse("Error: Username is already taken! "));
        if (userRepository.existsByEmail(signupRequest.getEmail()))
            return ResponseEntity.badRequest().
                    body(new MessageResponse("Error: Email is already taken! "));

        User user = new User(signupRequest.getUsername(), signupRequest.getEmail(), encoder.encode(signupRequest.getPassword()));

        Set<String> requestRole = signupRequest.getRole();
        Set<Role> roles= new HashSet<>();
        if (requestRole==null){
            Role autoAssignRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error : Role is not found"));
           roles.add(autoAssignRole);
        }else {
            requestRole.forEach(role->{
                switch (role){
                    case "admin":
                        Role adminnRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error : Role is not found"));
                        roles.add(adminnRole);
                        break;
                    case "seller":
                        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER).orElseThrow(() -> new RuntimeException("Error : Role is not found"));
                        roles.add(sellerRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error : Role is not found"));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);
        return new ResponseEntity<>(new MessageResponse("User registered successfully"),HttpStatus.OK);
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
        // Here we are using our own custom user detail we have implemented not inbuilt user detail
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate JWT token for the user
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority()).toList();

        //setting jwt from cookie to string so in response no null for jwt field shows
        UserInfoResponse response=new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),roles);


        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser(){
        ResponseCookie cleanJwtCookie = jwtUtils.getCleanJwtCookie();
        //here in response header we are setting cookie
        return  ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, String.valueOf(cleanJwtCookie)).body(new MessageResponse("You've been signed out!"));
    }
}
