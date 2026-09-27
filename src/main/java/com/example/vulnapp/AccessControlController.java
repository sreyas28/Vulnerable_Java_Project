package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/access")
public class AccessControlController {

    private final JdbcTemplate jdbc;

    public AccessControlController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // VULN: Improper Access Control - IDOR: any client can read any account by id
    @GetMapping(value = "/accounts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getAccount(@PathVariable int id) {
        return jdbc.queryForMap("SELECT id, owner, balance FROM accounts WHERE id = ?", id);
    }

    // VULN: Improper Access Control - admin action with no authentication
    @PostMapping(value = "/admin/reset-balances", produces = MediaType.TEXT_PLAIN_VALUE)
    public String resetAll() {
        jdbc.update("UPDATE accounts SET balance = 0");
        return "all balances reset";
    }

    // VULN: Improper Access Control - privilege escalation via client-supplied role
    @PostMapping(value = "/users/role", produces = MediaType.TEXT_PLAIN_VALUE)
    public String setRole(@RequestParam String username, @RequestParam String role) {
        jdbc.update("UPDATE users SET role = ? WHERE username = ?", role, username);
        return "role updated";
    }

    // VULN: Improper Access Control - mass assignment updates all fields from parameters
    @PostMapping(value = "/users/update", produces = MediaType.TEXT_PLAIN_VALUE)
    public String updateUser(@RequestParam String username,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) String role,
                             @RequestParam(required = false) String email) {
        jdbc.update("UPDATE users SET password = COALESCE(?, password), role = COALESCE(?, role), "
                + "email = COALESCE(?, email) WHERE username = ?", password, role, email, username);
        return "user updated";
    }

    // VULN: Improper Access Control - sensitive data exposure (password returned in API)
    @GetMapping(value = "/users/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> userProfile(@PathVariable String username) {
        return jdbc.queryForList("SELECT id, username, password, role, email FROM users WHERE username = ?",
                username);
    }
}
