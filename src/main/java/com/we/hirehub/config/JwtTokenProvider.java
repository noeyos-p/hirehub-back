package com.we.hirehub.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenMillis;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-expire-seconds:3600}") long accessExpireSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMillis = accessExpireSeconds * 1000L;
    }

    public String createToken(Authentication authentication) {
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails details) {
            username = details.getUsername();
        } else {
            username = String.valueOf(principal);
        }

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenMillis);

        return Jwts.builder()
                .setSubject(username)         // 보통 email/username
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);

    }
    public Long getUserId(String token) {
        Claims claims = parseClaims(token).getBody();
        Object id = claims.get("id"); // 토큰에 "id" 클레임을 넣어둔 경우
        if (id instanceof Integer i) return i.longValue();
        if (id instanceof Long l) return l;
        if (id instanceof String s) return Long.parseLong(s);
        throw new IllegalStateException("토큰에 userId 정보가 없습니다.");
    }

}
