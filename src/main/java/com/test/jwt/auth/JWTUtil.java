package com.test.jwt.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey secretKey;
    private final Long accessExpiredMs; // 액세스 토큰의 만료시간(짧게 주는 편, 만료되면 버리고 새로발급하는 방식)
    private final Long refreshExpiredMs; // 리프레쉬 토큰의 만료시간, getter 필요

    public JWTUtil(
            @Value("${spring.jwt.secret}") String secretKey,
            @Value("${spring.jwt.access-token-expiration}") Long accessExpiredMs,
            @Value("${spring.jwt.refresh-token-expiration}") Long refreshExpiredMs) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessExpiredMs = accessExpiredMs;
        this.refreshExpiredMs = refreshExpiredMs;
    }

    // JWT 문자열을 생성하는 메서드
    // 넣고 싶은것 더 추가 가능(세션에 저장할 정보 선택했던거랑 비슷한 느낌인듯..)
    // - 인증 과정을 거친 후 생성된 JWT 문자열은 클라이언트에게 전달된다.
    public String createJWT(String username, String role, Long expiredMs) {
        // claim(): 토큰의 페이로드에 사용자 정보를 저장
        // issuedAt(): 토큰 생성 시간
        // expiration() : 토큰 만료 시간
        // signWith(): 서명(위변조 방지)
        // compact(): header.payload.signature 형태의 최종 JWT 문자열 생성
        return Jwts.builder()
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
    
    // 클라이언트가 서버에 접속할 때 이전에 받은 JWT 토큰 전달 + 요청
    // - 토큰 검증
    // - JWT 문자열을 파싱 -> 정보 추출
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)          // 토큰 검증
                .build()              
                .parseSignedClaims(token)       // 토큰 분해
                .getPayload();                  // 페이로드에서 가져온 정보를 Claims에 담기
    }

    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Boolean isExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // JWT 토큰 종류
    // 1. 액세스 토큰
    public String createAccessToken(String username, String role) {
        return createJWT(username, role, accessExpiredMs);
    }
    
    // 2. 리프레시 토큰
    public String createRefreshToken(String username, String role) {
        return createJWT(username, role, refreshExpiredMs);
    }

    public Long getRefreshExpiredMs() {
        return refreshExpiredMs;
    }
}
