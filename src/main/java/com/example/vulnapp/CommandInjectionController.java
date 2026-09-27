package com.example.vulnapp;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cmd")
public class CommandInjectionController {

    private static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]{1,253}$");
    private static final Pattern SANDBOX_NAME = Pattern.compile("^[a-zA-Z0-9._-]{1,64}$");

    // FIXED: Command Injection - no shell; reachability check via Java API with strict host allowlist pattern
    @GetMapping(value = "/exec", produces = MediaType.TEXT_PLAIN_VALUE)
    public String execPing(@RequestParam(defaultValue = "127.0.0.1") String host) throws Exception {
        String safeHost = InputValidator.requireLength(host, "host", 253);
        if (!HOST_PATTERN.matcher(safeHost).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        InetAddress address = InetAddress.getByName(safeHost);
        boolean reachable = address.isReachable(2000);
        return "host:" + safeHost + " reachable:" + reachable;
    }

    // FIXED: Command Injection - pure Java directory listing; filename validated against allowlist regex
    @GetMapping(value = "/build", produces = MediaType.TEXT_PLAIN_VALUE)
    public String processBuilder(@RequestParam String cmd) throws Exception {
        String name = InputValidator.requireLength(cmd, "cmd", 64);
        if (!SANDBOX_NAME.matcher(name).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        Path sandbox = SandboxPaths.root();
        try (var stream = Files.list(sandbox)) {
            return stream
                    .filter(p -> p.getFileName().toString().contains(name))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.joining("\n"));
        }
    }
}
