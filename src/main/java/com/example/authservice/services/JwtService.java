package com.example.authservice.services;


import com.example.authservice.models.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;


@Service
public class JwtService {

    private final String SECRET = "mysuperfgfdhgfjghjhjkvhjlkjlksecretkeymysupersecretkey123456";


    public String generateToken(AuthUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getKey())
                .compact();
    }

    public String generateToken(String email, List<String> roles) {
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getKey())
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }


    public boolean isValid(String token, String email) {
        return email.equals(extractEmail(token)) && !isExpired(token);
    }
    public List<String> extractRoles(String token) {
        return getClaims(token).get("roles", List.class);
    }
    public Long extractUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }
    private boolean isExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }
    public String generateRefreshToken(AuthUser user) {
        return UUID.randomUUID().toString(); // NOT JWT (best practice)
    }
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }
    private Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}