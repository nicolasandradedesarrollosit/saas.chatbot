package com.example.saas.chatbot.domain.port.out;

import com.example.saas.chatbot.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}