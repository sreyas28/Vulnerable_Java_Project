package com.example.vulnapp;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final JdbcTemplate jdbc;

    public UserService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // VULN: SQL Injection - username/password concatenated into WHERE clause (auth bypass)
    public List<Map<String, Object>> login(String username, String password) {
        String sql = "SELECT id, username, role, email FROM users WHERE username='"
                + username + "' AND password='" + password + "'";
        return jdbc.queryForList(sql);
    }

    // VULN: SQL Injection - search term concatenated into LIKE expression
    public List<Map<String, Object>> searchByUsername(String term) {
        String sql = "SELECT id, username, role FROM users WHERE username LIKE '%" + term + "%'";
        return jdbc.queryForList(sql);
    }

    // VULN: SQL Injection - ORDER BY column name taken from user input
    public List<Map<String, Object>> listUsersSorted(String sortColumn) {
        String sql = "SELECT id, username, role FROM users ORDER BY " + sortColumn;
        return jdbc.queryForList(sql);
    }
}
