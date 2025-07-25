package com.example.EmpApp.config;

import com.example.EmpApp.Entity.Employee;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtService {
    private final String jwtSecret = "replace_this_with_a_very_long_secret_key_for_security_1234567890";
    private final Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    private final long accessTokenValidityMs = 5 * 60 * 1000; // 5 minutes
    private final long refreshTokenValidityMs = 7 * 24 * 60 * 60 * 1000; // 7 days

    public String generateAccessToken(Employee employee) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", employee.getEmpId());
        claims.put("name", employee.getEmpName());
        claims.put("email", employee.getEmail());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(employee.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidityMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Employee employee) {
        return Jwts.builder()
                .setSubject(employee.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidityMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new com.example.EmpApp.exceptionhandler.TokenValidationException("Invalid JWT token", e);
        }
    }
}
