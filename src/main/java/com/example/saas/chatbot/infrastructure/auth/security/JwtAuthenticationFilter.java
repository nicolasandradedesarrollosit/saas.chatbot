package com.example.saas.chatbot.infrastructure.auth.security;

import com.example.saas.chatbot.domain.auth.model.AuthToken;
import com.example.saas.chatbot.domain.auth.port.in.AuthUseCase;
import com.example.saas.chatbot.domain.auth.port.out.TokenBlacklistPort;
import com.example.saas.chatbot.domain.auth.port.out.TokenProviderPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProviderPort tokenProvider;
    private final TokenBlacklistPort tokenBlacklist;
    private final AuthUseCase authUseCase;

    public JwtAuthenticationFilter(TokenProviderPort tokenProvider,
                                   TokenBlacklistPort tokenBlacklist,
                                   AuthUseCase authUseCase) {
        this.tokenProvider = tokenProvider;
        this.tokenBlacklist = tokenBlacklist;
        this.authUseCase = authUseCase;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String accessToken = extractAccessToken(request);

        if (accessToken != null && !tokenBlacklist.isBlacklisted(accessToken)) {
            if (tokenProvider.isTokenValid(accessToken)) {
                setAuthentication(accessToken, request);
            } else if (tokenProvider.isTokenExpired(accessToken)) {
                String refreshToken = CookieUtil.extractRefreshTokenFromCookies(request.getCookies());
                if (refreshToken != null) {
                    try {
                        AuthToken newTokens = authUseCase.refresh(refreshToken);
                        CookieUtil.addRefreshTokenCookie(response, newTokens.getRefreshToken());
                        response.setHeader("X-New-Access-Token", newTokens.getAccessToken());
                        setAuthentication(newTokens.getAccessToken(), request);
                    } catch (Exception ignored) {
                        // refresh failed, user will get 401
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void setAuthentication(String token, HttpServletRequest request) {
        String email = tokenProvider.extractEmail(token);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
