package com.example.vulnapp;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final Map<String, UserPrincipal> sessions = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public String createSession(String username, String role) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        sessions.put(token, new UserPrincipal(username, role));
        return token;
    }

    public Optional<UserPrincipal> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(token));
    }

    public record UserPrincipal(String username, String role) {

        public boolean isAdmin() {
            return "admin".equalsIgnoreCase(role);
        }
    }
}
