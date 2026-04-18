package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
// Утилита для JWT. Она выпускает токены, читает из них данные пользователя и проверяет, что токен ещё действителен.
public class JwtTokenProvider {

    @Value("${jwt.secret:mySecretKeyForJWTSigningThatIsAtLeast32CharactersLong}")
    // Поле класса, которое хранит данные, нужные для работы объекта.
    private String jwtSecret;

    @Value("${jwt.access-token-expiration:900000}")
    // Поле класса, которое хранит данные, нужные для работы объекта.
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}")
    // Поле класса, которое хранит данные, нужные для работы объекта.
    private long refreshTokenExpiration;

    // Поле класса, которое хранит данные, нужные для работы объекта.
    private SecretKey signingKey;

    @PostConstruct
    // Отдельный шаг логики внутри класса. Он решает одну локальную задачу и используется из других методов этого же класса.
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }


    // Отдельный шаг логики внутри класса. Он решает одну локальную задачу и используется из других методов этого же класса.
    public String generateAccessToken(String username, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        claims.put("email", email);

        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(signingKey)
                .compact();
    }

    // Отдельный шаг логики внутри класса. Он решает одну локальную задачу и используется из других методов этого же класса.
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("jti", UUID.randomUUID().toString());

        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpiration))
                .signWith(signingKey)
                .compact();
    }


    // Проверяет, не испорчен ли access token и не истёк ли его срок действия.
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "access".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Проверяет refresh token по тем же правилам целостности и срока действия.
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    // Извлекает имя пользователя из JWT, чтобы потом загрузить его из базы.
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    // Отдельный шаг логики внутри класса. Он решает одну локальную задачу и используется из других методов этого же класса.
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    // Отдельный шаг логики внутри класса. Он решает одну локальную задачу и используется из других методов этого же класса.
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
