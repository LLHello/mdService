package com.mdservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    //定义密钥，长度需要超过 32 个字符，才符合 HS256 标准
    @Value("${jwt.secret}")
    private String SECRET_STRING;

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME;
    private  Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    }
    /**
     * 下发 Token (生成)
     * @param userId 用户ID
     * @param username 用户名
     * @return String token
     */
    public String generateToken(String userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims) // 自定义载荷
                .setSubject(username) // 主题
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 签名算法和密钥
                .compact();
    }
    /**
     * 解析 Token
     * @param token 客户端传来的 token
     * @return Claims 载荷信息
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            // 解析失败（过期、篡改、格式错误）
            throw new RuntimeException("Token 无效或已过期");
        }
    }
    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
