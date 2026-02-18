package com.example.saas.chatbot.infrastructure.auth.adapter.in;

import com.example.saas.chatbot.domain.auth.model.User;
import com.example.saas.chatbot.domain.auth.port.in.AuthUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        User user = authUseCase.register(request.email(), request.password());
        return ResponseEntity.ok("Usuario registrado: " + user.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        String token = authUseCase.login(request.email(), request.password());
        return ResponseEntity.ok(token);
    }
}
