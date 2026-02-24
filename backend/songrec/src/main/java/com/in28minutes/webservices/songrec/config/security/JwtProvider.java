package com.in28minutes.webservices.songrec.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {
    private final SecretKey key;
    private final long accessExpMillis;
    private final long refreshExpMillis;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-exp-min:60}") long accessExpMin,
            @Value("${jwt.refresh-exp-days:14}") long refreshExpDays
    ){
        this.key= Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpMillis = accessExpMin * 60 * 1000;
        this.refreshExpMillis = refreshExpDays * 24 * 60 * 60 * 1000;
    }

    public String createAccessToken(Long userId, String role){
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpMillis);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role",role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long userId){
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpMillis);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type","refresh")
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
