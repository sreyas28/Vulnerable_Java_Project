package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sql")
public class SqlInjectionController {

    private final UserService users;
    private final AuthService authService;

    public SqlInjectionController(UserService users, AuthService authService) {
        this.users = users;
        this.authService = authService;
    }

    @GetMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public String login(@RequestParam String username, @RequestParam String password) {
        Optional<Map<String, Object>> row = users.login(username, password);
        if (row.isEmpty()) {
            return "{\"authenticated\":false,\"token\":null}";
        }
        String role = String.valueOf(row.get().get("ROLE") != null ? row.get().get("ROLE") : row.get().get("role"));
        String token = authService.createSession(username, role);
        return "{\"authenticated\":true,\"token\":\"" + token + "\"}";
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> search(@RequestParam String q) {
        return users.searchByUsername(q);
    }

    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> sorted(@RequestParam(defaultValue = "id") String sort,
                                            @RequestParam(defaultValue = "asc") String dir) {
        String column = sort.contains(",") ? sort.split(",")[0] : sort;
        String direction = sort.contains(",") ? sort.split(",")[1] : dir;
        return users.listUsersSorted(column, direction);
    }
}
