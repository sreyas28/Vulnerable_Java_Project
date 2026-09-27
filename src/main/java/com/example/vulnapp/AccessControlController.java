package com.example.vulnapp;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/access")
public class AccessControlController {

    private static final Set<String> ASSIGNABLE_ROLES = Set.of("user", "admin");

    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AccessControlController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // FIXED: Improper Access Control - auth token required; owner or admin for account access
    @GetMapping(value = "/accounts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getAccount(@PathVariable int id, HttpServletRequest request) {
        AuthService.UserPrincipal user = currentUser(request);
        Map<String, Object> account = jdbc.queryForMap(
                "SELECT id, owner, balance FROM accounts WHERE id = ?", id);
        String owner = String.valueOf(account.get("OWNER") != null ? account.get("OWNER") : account.get("owner"));
        if (!user.isAdmin() && !user.username().equals(owner)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return account;
    }

    // FIXED: Improper Access Control - admin role enforced server-side
    @PostMapping(value = "/admin/reset-balances", produces = MediaType.TEXT_PLAIN_VALUE)
    public String resetAll(HttpServletRequest request) {
        requireAdmin(request);
        jdbc.update("UPDATE accounts SET balance = 0");
        return "all balances reset";
    }

    // FIXED: Improper Access Control - only admin may assign allowlisted roles
    @PostMapping(value = "/users/role", produces = MediaType.TEXT_PLAIN_VALUE)
    public String setRole(@RequestParam String username,
                          @RequestParam String role,
                          HttpServletRequest request) {
        requireAdmin(request);
        InputValidator.requireLength(username, "username", 64);
        String safeRole = InputValidator.requireLength(role, "role", 32).toLowerCase();
        if (!ASSIGNABLE_ROLES.contains(safeRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        jdbc.update("UPDATE users SET role = ? WHERE username = ?", safeRole, username);
        return "role updated";
    }

    // FIXED: Improper Access Control - limited fields; client role ignored; self or admin only
    @PostMapping(value = "/users/update", produces = MediaType.TEXT_PLAIN_VALUE)
    public String updateUser(@RequestParam String username,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) String email,
                             HttpServletRequest request) {
        AuthService.UserPrincipal user = currentUser(request);
        InputValidator.requireLength(username, "username", 64);
        if (!user.isAdmin() && !user.username().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (email != null) {
            InputValidator.requireLength(email, "email", 128);
            jdbc.update("UPDATE users SET email = ? WHERE username = ?", email, username);
        }
        if (password != null) {
            InputValidator.requireLength(password, "password", 128);
            jdbc.update("UPDATE users SET password = ? WHERE username = ?",
                    passwordEncoder.encode(password), username);
        }
        return "user updated";
    }

    // FIXED: Improper Access Control - password excluded from response
    @GetMapping(value = "/users/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> userProfile(@PathVariable String username, HttpServletRequest request) {
        AuthService.UserPrincipal user = currentUser(request);
        InputValidator.requireLength(username, "username", 64);
        if (!user.isAdmin() && !user.username().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return jdbc.queryForList(
                "SELECT id, username, role, email FROM users WHERE username = ?", username);
    }

    private static AuthService.UserPrincipal currentUser(HttpServletRequest request) {
        return (AuthService.UserPrincipal) request.getAttribute(AuthInterceptor.REQUEST_USER);
    }

    private static void requireAdmin(HttpServletRequest request) {
        if (!currentUser(request).isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }
}
