package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/xss")
public class XssController {

    private final List<String> comments = new ArrayList<>();

    // VULN: Cross-Site Scripting - reflected user input in response body without encoding
    @GetMapping(value = "/reflect", produces = MediaType.TEXT_PLAIN_VALUE)
    public String reflect(@RequestParam String msg) {
        return "You said: " + msg;
    }

    // VULN: Cross-Site Scripting - stored comment rendered back verbatim
    @PostMapping(value = "/comment", produces = MediaType.TEXT_PLAIN_VALUE)
    public String store(@RequestParam String text) {
        comments.add(text);
        return "stored:" + comments.size();
    }

    @GetMapping(value = "/comments", produces = MediaType.TEXT_PLAIN_VALUE)
    public String allComments() {
        return String.join("\n", comments);
    }

    // VULN: Cross-Site Scripting - DOM-based sink via inline script reading location.hash
    @GetMapping(value = "/dom", produces = MediaType.TEXT_HTML_VALUE)
    public String domPage() {
        return "<html><body><div id=\"out\"></div>"
                + "<script>document.getElementById('out').innerHTML=location.hash.slice(1);</script>"
                + "</body></html>";
    }

    // VULN: Cross-Site Scripting - unescaped value injected into HTML attribute
    @GetMapping(value = "/attr", produces = MediaType.TEXT_HTML_VALUE)
    public String attribute(@RequestParam String name) {
        return "<input type=\"text\" value=\"" + name + "\" />";
    }
}
