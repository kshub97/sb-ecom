package com.ecommerce.project.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;


// a singleton utility class to create/validate tokens.
@Component
public class JwtUtils {

    private static final Logger logger= LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs; //how long token is valid


    @Value("${spring.app.jwtSecret}")
    private String jwtSecret; //the secret key used to sign and verify tokens

    //Getting JWT from header
    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}",bearerToken);
        if (bearerToken!=null && bearerToken.startsWith("Bearer")){
            return bearerToken.substring(7);
        }

        return null;
    }
    //Generating Username from JWT  Token
    public String getUserNameFromJwtToken(String token){
        String userCredentials = Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(token).getPayload().getSubject();
        return userCredentials;
    }

    //Generating token from username ,Builds a new token for a given user, Sets expiration time, Signs with secret key
    public String generateTokenFromUsername(UserDetails userDetails){
        String username = userDetails.getUsername();
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
