package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/xss")
public class XssController {

    private static final int MAX_COMMENTS = 100;
    private final List<String> comments = new ArrayList<>();

    // FIXED: Cross-Site Scripting - HTML-escape reflected input; plain text response
    @GetMapping(value = "/reflect", produces = MediaType.TEXT_PLAIN_VALUE)
    public String reflect(@RequestParam String msg) {
        String safe = HtmlUtils.htmlEscape(InputValidator.requireLength(msg, "msg", 500));
        return "You said: " + safe;
    }

    // FIXED: Cross-Site Scripting - stored comments escaped on read
    @PostMapping(value = "/comment", produces = MediaType.TEXT_PLAIN_VALUE)
    public String store(@RequestParam String text) {
        InputValidator.requireLength(text, "text", 500);
        if (comments.size() >= MAX_COMMENTS) {
            comments.remove(0);
        }
        comments.add(text);
        return "stored:" + comments.size();
    }

    @GetMapping(value = "/comments", produces = MediaType.TEXT_PLAIN_VALUE)
    public String allComments() {
        return comments.stream()
                .map(HtmlUtils::htmlEscape)
                .collect(Collectors.joining("\n"));
    }

    // FIXED: Cross-Site Scripting - DOM sink removed; hash rendered with textContent only
    @GetMapping(value = "/dom", produces = MediaType.TEXT_HTML_VALUE)
    public String domPage() {
        return "<html><body><div id=\"out\"></div>"
                + "<script>document.getElementById('out').textContent=location.hash.slice(1);</script>"
                + "</body></html>";
    }

    // FIXED: Cross-Site Scripting - attribute value HTML-escaped and quoted
    @GetMapping(value = "/attr", produces = MediaType.TEXT_HTML_VALUE)
    public String attribute(@RequestParam String name) {
        String safe = HtmlUtils.htmlEscape(InputValidator.requireLength(name, "name", 100));
        return "<input type=\"text\" value=\"" + safe + "\" />";
    }
}
