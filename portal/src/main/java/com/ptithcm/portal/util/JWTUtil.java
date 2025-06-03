package com.ptithcm.portal.util;

import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JWTUtil {
    @Value("${app.secret.key}")
    private String secretKey;

    public String generateToken(String subject){
        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hours
                .signWith(SignatureAlgorithm.HS512, Base64.getEncoder().encodeToString(secretKey.getBytes()))
                .compact();
    }

    public Claims getClaims(String token){
        return Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(secretKey.getBytes()))
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token){
        return getClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, String subject){
        return getClaims(token).getSubject().equals(subject) && !isTokenExpired(token);
    }

    public String getSubject(String token){
        return getClaims(token).getSubject();
    }

    public String getExpireDate(String token){
        return getClaims(token).getExpiration().toString();
    }
}
