package com.ecommerce.project.service;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AuthenticationResult;
import com.ecommerce.project.payload.RoleRepository;
import com.ecommerce.project.payload.UserDTO;
import com.ecommerce.project.payload.UserResponse;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.service.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AuthServiceImpl  implements AuthService{

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

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public AuthenticationResult login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),loginRequest.getPassword()));

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
        UserInfoResponse response=new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),
                            userDetails.getEmail(), roles, jwtCookie.getValue());

        return new AuthenticationResult(response, jwtCookie);
    }

    @Override
    public ResponseEntity<MessageResponse> register(SignupRequest signupRequest) {
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

    @Override
    public UserInfoResponse getCurrentUserDetails(Authentication authentication) {
        // Here we are using our own custom user detail we have implemented not inbuilt user detail
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Generate JWT token for the user
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority()).toList();

        //setting jwt from cookie to string so in response no null for jwt field shows
        UserInfoResponse response=new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles, jwtCookie.getValue());

        return response;
    }

    @Override
    public ResponseCookie logoutUser() {
        ResponseCookie cleanJwtCookie = jwtUtils.getCleanJwtCookie();
        return cleanJwtCookie;
    }

    @Override
    public UserResponse getAllSellers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<User> allUsersByRoleName = userRepository.findByRoleName(AppRole.ROLE_SELLER, pageDetails);

        List<UserDTO> userResponseList = allUsersByRoleName.stream()
                .map((element) -> modelMapper.map(element, UserDTO.class)).toList();
        UserResponse response = new UserResponse();
        response.setContent(userResponseList);
        response.setPageNumber(allUsersByRoleName.getNumber());
        response.setPageSize(allUsersByRoleName.getSize());
        response.setTotalPages(allUsersByRoleName.getTotalPages());
        response.setTotalElements(allUsersByRoleName.getTotalElements());
        response.setLastPage(allUsersByRoleName.isLast());

        return response;
    }
}
