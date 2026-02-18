package com.example.saas.chatbot.infrastructure.auth.adapter.in;

import com.example.saas.chatbot.domain.auth.model.AuthToken;
import com.example.saas.chatbot.domain.auth.model.User;
import com.example.saas.chatbot.domain.auth.port.in.AuthUseCase;
import com.example.saas.chatbot.infrastructure.auth.security.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody AuthRequest.RequestRegisterAndLogin request) {
        User user = authUseCase.register(request.email(), request.password());
        return ResponseEntity.ok(Map.of("message", "User registered: " + user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest.RequestRegisterAndLogin request,
                                                     HttpServletResponse response) {
        AuthToken authToken = authUseCase.login(request.email(), request.password());
        CookieUtil.addRefreshTokenCookie(response, authToken.getRefreshToken());
        return ResponseEntity.ok(Map.of("accessToken", authToken.getAccessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request,
                                                      HttpServletResponse response) {
        String accessToken = extractAccessToken(request);
        String refreshToken = CookieUtil.extractRefreshTokenFromCookies(request.getCookies());

        if (accessToken != null) {
            authUseCase.logout(accessToken, refreshToken);
        }

        CookieUtil.clearRefreshTokenCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }

    private String extractAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
