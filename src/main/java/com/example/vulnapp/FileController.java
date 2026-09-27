package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Path SANDBOX = Path.of("sandbox");

    // VULN: Path Traversal - user path joined without canonicalization before read
    @GetMapping(value = "/read", produces = MediaType.TEXT_PLAIN_VALUE)
    public String read(@RequestParam String path) throws Exception {
        Path target = SANDBOX.resolve(path);
        return Files.readString(target);
    }

    // VULN: Path Traversal - arbitrary write under resolved path (../ escapes sandbox)
    @PostMapping(value = "/write", produces = MediaType.TEXT_PLAIN_VALUE)
    public String write(@RequestParam String path, @RequestParam String content) throws Exception {
        Path target = SANDBOX.resolve(path);
        Files.createDirectories(target.getParent());
        Files.writeString(target, content);
        return "written:" + target;
    }

    // VULN: Path Traversal - delete using unvalidated relative path
    @DeleteMapping(value = "/delete", produces = MediaType.TEXT_PLAIN_VALUE)
    public String delete(@RequestParam String path) throws Exception {
        Path target = SANDBOX.resolve(path);
        Files.deleteIfExists(target);
        return "deleted:" + target;
    }

    // VULN: Path Traversal - directory listing follows user-supplied path
    @GetMapping(value = "/list", produces = MediaType.TEXT_PLAIN_VALUE)
    public String list(@RequestParam(defaultValue = ".") String path) throws Exception {
        Path target = SANDBOX.resolve(path);
        return Files.list(target).map(p -> p.getFileName().toString())
                .collect(Collectors.joining("\n"));
    }
}
