package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileController {

    // FIXED: Path Traversal - all paths validated under sandbox root via SandboxPaths
    @GetMapping(value = "/read", produces = MediaType.TEXT_PLAIN_VALUE)
    public String read(@RequestParam String path) throws Exception {
        var target = SandboxPaths.resolveSafe(path);
        return Files.readString(target);
    }

    @PostMapping(value = "/write", produces = MediaType.TEXT_PLAIN_VALUE)
    public String write(@RequestParam String path, @RequestParam String content) throws Exception {
        InputValidator.requireLength(content, "content", 16_384);
        var target = SandboxPaths.resolveSafe(path);
        SandboxPaths.requireAllowedWriteExtension(target);
        Files.createDirectories(target.getParent());
        Files.writeString(target, content);
        return "written:" + target.getFileName();
    }

    @DeleteMapping(value = "/delete", produces = MediaType.TEXT_PLAIN_VALUE)
    public String delete(@RequestParam String path) throws Exception {
        var target = SandboxPaths.resolveSafe(path);
        Files.deleteIfExists(target);
        return "deleted:" + target.getFileName();
    }

    @GetMapping(value = "/list", produces = MediaType.TEXT_PLAIN_VALUE)
    public String list(@RequestParam(defaultValue = ".") String path) throws Exception {
        var target = SandboxPaths.resolveSafe(path);
        if (!Files.isDirectory(target)) {
            return "";
        }
        try (var stream = Files.list(target)) {
            return stream.map(p -> p.getFileName().toString()).collect(Collectors.joining("\n"));
        }
    }
}
