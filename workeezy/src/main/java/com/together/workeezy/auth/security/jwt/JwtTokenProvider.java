package com.together.workeezy.auth.security.jwt;

import com.together.workeezy.auth.security.user.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Profile("prod")
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    // Access Token 만료 시간 (ms)
    // application.yml의 jwt.expiration-ms 값
    @Value("${jwt.access-expiration-ms}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpiration;

    private final CustomUserDetailsService userDetailsService;
    private Key key;

    // 사용자 정보를 불러오기 위해 DI
    public JwtTokenProvider(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // secretKey를 기반으로 HMAC-SHA 키 생성
    // 서버 시작 시 1번만 실행됨
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // Access Token 생성
    public String createAccessToken(String email, String role, Long userId) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpiration);

        return Jwts.builder()
                .setSubject(email) // 토큰 주인(email)
                .claim("userId", userId)
                .claim("role", role) // role을 claim에 넣는다
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String email, String role, Long userId) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Claims 추출
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰에서 email 추출
    // 요청한 사용자가 누구인지 체크할 때 사용
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    // 관리자/권한 분기용 (현재 미사용)
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role").toString();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(30) // 허용 시간차
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("토큰 만료됨");
            return false;
        } catch (Exception e) {
            System.out.println("토큰 검증 실패");
            return false;
        }
    }

    // Authentication 객체 생성 (스프링 시큐리티 인증용)
    // SecurityContextHolder 에 저장될 Authentication
    public Authentication getAuthentication(String token) {
        String email = getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // Redis에 TTL 저장하기 위해 필요
    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    // Access Token 만료시간(ms)
    public long getAccessExpiration() {
        return accessExpiration;
    }

    // refresh 고도화 시 사용 예정
    // Access/Refresh 토큰 남은 만료시간(ms) 계산
//    public long getRemainingExpiration(String token) {
//        try {
//            Claims claims = getClaims(token);
//            Date expiration = claims.getExpiration();
//            long now = System.currentTimeMillis();
//
//            long diff = expiration.getTime() - now;
//            return Math.max(diff, 0); // 음수 방지
//        } catch (Exception e) {
//            return 0; // 만료된 토큰이면 0 리턴
//        }
//    }
}

