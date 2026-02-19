package com.example.saas.chatbot.application.service.auth;

import com.example.saas.chatbot.domain.auth.exception.InvalidCredentialsException;
import com.example.saas.chatbot.domain.auth.exception.InvalidTokenException;
import com.example.saas.chatbot.domain.auth.exception.UserAlreadyExistsException;
import com.example.saas.chatbot.domain.auth.model.AuthToken;
import com.example.saas.chatbot.domain.auth.model.RefreshToken;
import com.example.saas.chatbot.domain.auth.model.Role;
import com.example.saas.chatbot.domain.auth.model.User;
import com.example.saas.chatbot.domain.auth.port.in.AuthUseCase;
import com.example.saas.chatbot.domain.auth.port.out.PasswordEncoderPort;
import com.example.saas.chatbot.domain.auth.port.out.RefreshTokenRepositoryPort;
import com.example.saas.chatbot.domain.auth.port.out.TokenBlacklistPort;
import com.example.saas.chatbot.domain.auth.port.out.TokenProviderPort;
import com.example.saas.chatbot.domain.auth.port.out.UserRepositoryPort;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;
    private final PasswordEncoderPort passwordEncoder;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final TokenBlacklistPort tokenBlacklist;

    public AuthService(UserRepositoryPort userRepository,
                       TokenProviderPort tokenProvider,
                       PasswordEncoderPort passwordEncoder,
                       RefreshTokenRepositoryPort refreshTokenRepository,
                       TokenBlacklistPort tokenBlacklist) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    public AuthToken login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshTokenValue = tokenProvider.generateRefreshToken(user);

        RefreshToken refreshToken = new RefreshToken(
                refreshTokenValue,
                user.getEmail(),
                Instant.now().plus(7, ChronoUnit.DAYS)
        );
        refreshTokenRepository.save(refreshToken);

        return new AuthToken(accessToken, refreshTokenValue);
    }

    @Override
    public User register(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        User newUser = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .build();

        return userRepository.save(newUser);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        tokenBlacklist.blacklist(accessToken);

        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(rt -> {
                        refreshTokenRepository.revokeAllByUserEmail(rt.getUserEmail());
                    });
        }
    }

    @Override
    public AuthToken refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!stored.isValid()) {
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }

        User user = userRepository.findByEmail(stored.getUserEmail())
                .orElseThrow(InvalidCredentialsException::new);

        refreshTokenRepository.revokeAllByUserEmail(user.getEmail());

        String newAccessToken = tokenProvider.generateAccessToken(user);
        String newRefreshTokenValue = tokenProvider.generateRefreshToken(user);

        RefreshToken newRefreshToken = new RefreshToken(
                newRefreshTokenValue,
                user.getEmail(),
                Instant.now().plus(7, ChronoUnit.DAYS)
        );
        refreshTokenRepository.save(newRefreshToken);

        return new AuthToken(newAccessToken, newRefreshTokenValue);
    }
}
