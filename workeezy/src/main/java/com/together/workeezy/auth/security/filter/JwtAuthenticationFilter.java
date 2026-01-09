package com.together.workeezy.auth.security.filter;

import com.together.workeezy.auth.security.jwt.JwtTokenProvider;
import com.together.workeezy.auth.service.TokenRedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 토큰 검증 제외할 URL (화이트리스트)
    private static final List<String> WHITELIST = List.of(
            "/api/auth/login",
//            "/api/auth/refresh",
            "/api/programs/**",
            "/api/recommendations/**",
            "/actuator/**",
//            "/api/payments/confirm",
            "/api/reservations/availability/**",
            "/ping",              // debug
            "/error"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // OPTIONS 요청은 항상 허용 (CORS Preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.info("🟢 OPTIONS 요청 통과");
            filterChain.doFilter(request, response);
            return;
        }

        // refresh는 완전 스킵
        if (uri.startsWith("/api/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        // whitelist 스킵
        if (isWhitelisted(request)) {
            log.info("➡️ [JwtFilter SKIP] whitelisted: {}", uri);
            filterChain.doFilter(request, response);
            return;
        }

//        String requestURI = request.getRequestURI();
//        log.info("📌 JwtFilter 요청 경로: " + requestURI);

//        // 화이트리스트 URL은 JWT 인증 스킵
//        for (String pattern : WHITELIST) {
//            if (pathMatcher.match(pattern, requestURI)) {
//                log.info("➡️ [JwtFilter SKIP] whitelist match: {}" + pattern);
//                filterChain.doFilter(request, response);
//                return;
//            }
//            log.info("➡️ [JwtFilter PASS] not whitelisted: {}" + pattern);
//        }
        log.info("========== JWT FILTER START ==========");
        log.info("📌 URI = " + uri);

        // accessToken 추출
        String token = resolveToken(request);
        log.info("🔐 [JwtFilter TOKEN] token = {}", token == null ? "NULL" : "EXISTS");
        // 토큰 없으면 -> 익명 요청으로 통과
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 블랙리스트 토큰이면 인증 세팅 안 함
        if (tokenRedisService.isBlacklisted(token)) {
            log.warn("🚫 [JwtFilter] blacklisted token");
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 검증 + 인증 객체 세팅
        if (jwtTokenProvider.validateToken(token)) {

            // Authentication 생성
            Authentication auth = jwtTokenProvider.getAuthentication(token);

            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("✅ [JwtFilter AUTH] authenticated user = {}", auth.getName());
            }
        }
        // 다음 필터로 이동
        filterChain.doFilter(request, response);

        log.info("========== JWT FILTER END ==========");
    }

    // Authorization 헤더 + HttpOnly 쿠키
    private String resolveToken(HttpServletRequest request) {

        // Authorization 헤더에서 bearer 토큰
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String bearerToken = header.substring(7);
            if (!"undefined".equals(bearerToken) && !bearerToken.isBlank()) {
                return bearerToken;
            }
        }

        // HttpOnly 쿠키에서 accessToken
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isWhitelisted(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return WHITELIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }


}