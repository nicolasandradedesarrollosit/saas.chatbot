package com.example.saas.chatbot.infrastructure.auth.config;

import com.example.saas.chatbot.application.service.auth.AuthService;
import com.example.saas.chatbot.domain.auth.port.in.AuthUseCase;
import com.example.saas.chatbot.domain.auth.port.out.PasswordEncoderPort;
import com.example.saas.chatbot.domain.auth.port.out.RefreshTokenRepositoryPort;
import com.example.saas.chatbot.domain.auth.port.out.TokenBlacklistPort;
import com.example.saas.chatbot.domain.auth.port.out.TokenProviderPort;
import com.example.saas.chatbot.domain.auth.port.out.UserRepositoryPort;
import com.example.saas.chatbot.infrastructure.auth.security.JwtAuthenticationFilter;
import com.example.saas.chatbot.infrastructure.auth.security.SpringPasswordEncoderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordEncoderPort passwordEncoderPort(PasswordEncoder passwordEncoder) {
        return new SpringPasswordEncoderAdapter(passwordEncoder);
    }

    @Bean
    public AuthUseCase authUseCase(UserRepositoryPort userRepository,
                                   TokenProviderPort tokenProvider,
                                   PasswordEncoderPort passwordEncoder,
                                   RefreshTokenRepositoryPort refreshTokenRepository,
                                   TokenBlacklistPort tokenBlacklist) {
        return new AuthService(userRepository, tokenProvider, passwordEncoder,
                refreshTokenRepository, tokenBlacklist);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(TokenProviderPort tokenProvider,
                                                           TokenBlacklistPort tokenBlacklist,
                                                           AuthUseCase authUseCase) {
        return new JwtAuthenticationFilter(tokenProvider, tokenBlacklist, authUseCase);
    }
}
