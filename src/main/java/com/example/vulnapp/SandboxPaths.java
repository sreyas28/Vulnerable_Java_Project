package com.example.vulnapp;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public final class SandboxPaths {

    private static final Path SANDBOX_ROOT = Path.of("sandbox").toAbsolutePath().normalize();
    private static final Set<String> ALLOWED_WRITE_EXTENSIONS = Set.of(".txt", ".log", ".flag");

    private SandboxPaths() {
    }

    public static Path root() {
        return SANDBOX_ROOT;
    }

    // FIXED: Path Traversal - resolve, normalize, toRealPath, and verify under sandbox root
    public static Path resolveSafe(String relativePath) {
        InputValidator.rejectAbsolutePath(relativePath);
        InputValidator.requireLength(relativePath, "path", 256);
        Path resolved = SANDBOX_ROOT.resolve(relativePath).normalize();
        if (!resolved.startsWith(SANDBOX_ROOT)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        try {
            if (java.nio.file.Files.exists(resolved)) {
                resolved = resolved.toRealPath();
                if (!resolved.startsWith(SANDBOX_ROOT)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
                }
            }
            return resolved;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
    }

    public static void requireAllowedWriteExtension(Path target) {
        String name = target.getFileName().toString().toLowerCase();
        boolean allowed = ALLOWED_WRITE_EXTENSIONS.stream().anyMatch(name::endsWith);
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
    }
}
