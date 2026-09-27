package com.example.vulnapp;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private static final Map<String, String> ORDER_BY_QUERIES = Map.ofEntries(
            Map.entry("id_asc", "SELECT id, username, role FROM users ORDER BY id ASC"),
            Map.entry("id_desc", "SELECT id, username, role FROM users ORDER BY id DESC"),
            Map.entry("username_asc", "SELECT id, username, role FROM users ORDER BY username ASC"),
            Map.entry("username_desc", "SELECT id, username, role FROM users ORDER BY username DESC"),
            Map.entry("email_asc", "SELECT id, username, role FROM users ORDER BY email ASC"),
            Map.entry("email_desc", "SELECT id, username, role FROM users ORDER BY email DESC"));

    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // FIXED: SQL Injection - parameterized login with BCrypt password verification
    public Optional<Map<String, Object>> login(String username, String password) {
        InputValidator.requireLength(username, "username", 64);
        InputValidator.requireLength(password, "password", 128);
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, username, password, role, email FROM users WHERE username = ?",
                username);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> row = rows.get(0);
        String hash = (String) (row.get("PASSWORD") != null ? row.get("PASSWORD") : row.get("password"));
        if (!passwordEncoder.matches(password, hash)) {
            return Optional.empty();
        }
        row.remove("PASSWORD");
        row.remove("password");
        return Optional.of(row);
    }

    // FIXED: SQL Injection - bound parameter for LIKE search
    public List<Map<String, Object>> searchByUsername(String term) {
        String safeTerm = InputValidator.requireLength(term, "q", 64);
        return jdbc.queryForList(
                "SELECT id, username, role FROM users WHERE username LIKE ?",
                "%" + safeTerm + "%");
    }

    // FIXED: SQL Injection - ORDER BY selected only from constant SQL map keys (column_direction)
    public List<Map<String, Object>> listUsersSorted(String sortColumn, String direction) {
        String column = sortColumn == null ? "id" : sortColumn.toLowerCase();
        String dir = direction == null ? "asc" : direction.toLowerCase();
        String sql = ORDER_BY_QUERIES.get(column + "_" + dir);
        if (sql == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        return jdbc.queryForList(sql);
    }
}
