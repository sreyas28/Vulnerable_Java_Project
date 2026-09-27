package com.example.vulnapp;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class InputValidator {

    private InputValidator() {
    }

    public static String requireLength(String value, String name, int max) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        if (value.length() > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        if (value.indexOf('\0') >= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        return value;
    }

    public static void rejectAbsolutePath(String path) {
        if (path.startsWith("/") || path.startsWith("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
    }
}
