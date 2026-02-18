package com.example.saas.chatbot.application.service;

import com.example.saas.chatbot.domain.model.Role;
import com.example.saas.chatbot.domain.model.User;
import com.example.saas.chatbot.domain.port.in.AuthUseCase;
import com.example.saas.chatbot.domain.port.out.UserRepositoryPort;
import com.example.saas.chatbot.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!passwordEncoder.matches(password, user.getPassword())){
            throw new RuntimeException("Contraseña incorrecta");
        }
        return jwtService.generateToken(user);
    }

    @Override
    public User register(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .build();
        return userRepository.save(newUser);
    }
}