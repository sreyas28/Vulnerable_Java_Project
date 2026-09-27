package com.example.vulnapp;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PasswordBootstrap implements ApplicationRunner {

    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public PasswordBootstrap(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Map<String, Object>> users = jdbc.queryForList("SELECT id, password FROM users");
        for (Map<String, Object> row : users) {
            String password = (String) row.get("PASSWORD");
            if (password == null) {
                password = (String) row.get("password");
            }
            if (password != null && !password.startsWith("$2a$")) {
                jdbc.update("UPDATE users SET password = ? WHERE id = ?", encoder.encode(password), row.get("ID") != null ? row.get("ID") : row.get("id"));
            }
        }
    }
}
