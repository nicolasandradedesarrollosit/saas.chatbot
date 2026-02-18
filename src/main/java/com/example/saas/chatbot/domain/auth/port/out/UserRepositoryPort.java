package com.example.saas.chatbot.domain.auth.port.out;

import com.example.saas.chatbot.domain.auth.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
