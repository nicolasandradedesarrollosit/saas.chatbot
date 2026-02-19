package com.example.saas.chatbot.infrastructure.auth.adapter.in;

import com.example.saas.chatbot.application.shared.dto.ApiResponse;
import com.example.saas.chatbot.domain.auth.model.AuthToken;
import com.example.saas.chatbot.domain.auth.model.User;
import com.example.saas.chatbot.domain.auth.port.in.AuthUseCase;
import com.example.saas.chatbot.infrastructure.auth.security.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody AuthRequest.RequestRegisterAndLogin request) {
        User user = authUseCase.register(request.email(), request.password());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "User Registered", "User registered: " + user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody AuthRequest.RequestRegisterAndLogin request,
                                             HttpServletResponse response) {
        AuthToken authToken = authUseCase.login(request.email(), request.password());
        CookieUtil.addAccessTokenCookie(response, authToken.getAccessToken());
        CookieUtil.addRefreshTokenCookie(response, authToken.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(200, "Login Successful", "Authentication completed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request,
                                              HttpServletResponse response) {
        String accessToken = extractAccessToken(request);
        String refreshToken = CookieUtil.extractRefreshTokenFromCookies(request.getCookies());

        if (accessToken != null) {
            authUseCase.logout(accessToken, refreshToken);
        }

        CookieUtil.clearCookies(response);
        return ResponseEntity.ok(ApiResponse.success(200, "Logout Successful", "Session terminated"));
    }

    private String extractAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return CookieUtil.extractAccessTokenFromCookies(request.getCookies());
    }
}
