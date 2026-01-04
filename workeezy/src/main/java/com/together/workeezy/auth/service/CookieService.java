package com.together.workeezy.auth.service;

import com.together.workeezy.auth.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieService {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.cookie.secure}")
    private boolean secure;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    @Value("${app.cookie.domain}")
    private String domain;

    // accessToken 쿠키 생성 (인증용)
    public void addAccessCookie(
            HttpServletResponse response,
            String accessToken
    ) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .domain(domain)
                .path("/")                              // 모든 API에 전송
                .maxAge(jwtTokenProvider.getAccessExpiration() / 1000)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // accessToken 쿠키 삭제
    public void deleteAccessCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // refresh 쿠키 만들기
    public void addRefreshCookie(
            HttpServletResponse response,
            String refreshToken,
            boolean autoLogin
    ) {

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .domain(domain)
                .path("/")
                .maxAge(autoLogin
                        ? jwtTokenProvider.getRefreshExpiration() / 1000
                        : -1)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    // logout 시 쿠키 삭제
    public void deleteRefreshCookie(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public String extractAccessToken(HttpServletRequest request) {

        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // request -> refresh 쿠키 추출
    public String extractRefreshToken(HttpServletRequest request) {

        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refreshToken")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}