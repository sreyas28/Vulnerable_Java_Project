package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/cmd")
public class CommandInjectionController {

    private static final Path SANDBOX = Path.of("sandbox").toAbsolutePath().normalize();

    // VULN: Command Injection - user input concatenated into shell command for Runtime.exec
    @GetMapping(value = "/exec", produces = MediaType.TEXT_PLAIN_VALUE)
    public String execPing(@RequestParam(defaultValue = "127.0.0.1") String host) throws Exception {
        String cmd = "ping -c 1 " + host;
        Process p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
        return readProcessOutput(p);
    }

    // VULN: Command Injection - ProcessBuilder runs sh -c with raw user input
    @GetMapping(value = "/build", produces = MediaType.TEXT_PLAIN_VALUE)
    public String processBuilder(@RequestParam String cmd) throws Exception {
        Process p = new ProcessBuilder("sh", "-c", "ls -la " + cmd)
                .directory(SANDBOX.toFile())
                .start();
        return readProcessOutput(p);
    }

    private static String readProcessOutput(Process p) throws Exception {
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
            p.waitFor();
            return sb.toString();
        }
    }
}
