package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.service.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;


// a singleton utility class to create/validate tokens.
@Component
public class JwtUtils {

    private static final Logger logger= LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs; //how long token is valid

    @Value("${spring.com.app.jwtCookieName}")
    private String jwtCookieName;

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret; //the secret key used to sign and verify tokens

    //Getting JWT from header (needed for swagger)
    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}",bearerToken);
        if (bearerToken!=null && bearerToken.startsWith("Bearer")){
            return bearerToken.substring(7);
        }

        return null;
    }

    public String getJwtFromCookies(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request, jwtCookieName);
        if (cookie!=null){
            return cookie.getValue();
        }
        return null;
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userDetails){
        String jwt = generateTokenFromUsername(userDetails.getUsername());
        // Create Response Cookie
        ResponseCookie responseCookie = ResponseCookie.from(jwtCookieName,jwt)
                .path("/api")
                .maxAge(24 * 60 * 60)
                .httpOnly(false)
                .secure(false)
                .build();
        return responseCookie;
    }

    public ResponseCookie getCleanJwtCookie(){
        // Create Response Cookie with null so cookie will be empty when user again send request and it will be treated as signout
         ResponseCookie cookie = ResponseCookie.from(jwtCookieName,null)
                .path("/api").build();
        return cookie;
    }


    //Generating Username from JWT  Token
    public String getUserNameFromJwtToken(String token){
        // Parse username from JWT
        String userCredentials = Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(token).getPayload().getSubject();
        return userCredentials;
    }

    //Generating token from username ,Builds a new token for a given user, Sets expiration time, Signs with secret key
    public String generateTokenFromUsername(String username){
        // Generate JWT from username
        String generatedToken = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration((new Date(new Date().getTime() + jwtExpirationMs))).signWith(key()).compact();
        return generatedToken;
    }

    //Generating signing key , Decodes Base64 secret and returns an HMAC key,Used to sign and verify tokens
    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    //Validate JWT token
    public boolean validateJwtToken(String authToken) {
        try {
            System.out.println("Validate");
            Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken);
            return  true;
        }catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
