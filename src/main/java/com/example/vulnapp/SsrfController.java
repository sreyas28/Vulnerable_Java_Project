package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/ssrf")
public class SsrfController {

    // VULN: SSRF - server fetches arbitrary URL from user input
    @GetMapping(value = "/fetch", produces = MediaType.TEXT_PLAIN_VALUE)
    public String fetch(@RequestParam String url) throws Exception {
        return readUrl(new URL(url));
    }

    // VULN: SSRF - webhook caller posts to user-controlled destination
    @GetMapping(value = "/webhook", produces = MediaType.TEXT_PLAIN_VALUE)
    public String webhook(@RequestParam String callback) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(callback).openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        return "status:" + conn.getResponseCode();
    }

    // VULN: SSRF - open proxy forwards request to target parameter
    @GetMapping(value = "/proxy", produces = MediaType.TEXT_PLAIN_VALUE)
    public String proxy(@RequestParam String target) throws Exception {
        return readUrl(URI.create(target).toURL());
    }

    // VULN: SSRF - image fetcher with weak blocklist (localhost string only)
    @GetMapping(value = "/image", produces = MediaType.TEXT_PLAIN_VALUE)
    public String image(@RequestParam String src) throws Exception {
        if (src.toLowerCase().contains("localhost")) {
            return "blocked";
        }
        byte[] body = readUrl(new URL(src)).getBytes(StandardCharsets.UTF_8);
        return "bytes:" + body.length;
    }

    private static String readUrl(URL url) throws Exception {
        try (InputStream in = url.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
