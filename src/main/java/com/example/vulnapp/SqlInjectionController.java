package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sql")
public class SqlInjectionController {

    private final UserService users;

    public SqlInjectionController(UserService users) {
        this.users = users;
    }

    @GetMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public String login(@RequestParam String username, @RequestParam String password) {
        List<Map<String, Object>> rows = users.login(username, password);
        return "{\"authenticated\":" + !rows.isEmpty() + ",\"rows\":" + rows.size() + "}";
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> search(@RequestParam String q) {
        return users.searchByUsername(q);
    }

    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> sorted(@RequestParam(defaultValue = "id") String sort) {
        return users.listUsersSorted(sort);
    }
}
