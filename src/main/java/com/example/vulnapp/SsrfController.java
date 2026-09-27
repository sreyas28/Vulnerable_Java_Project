package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ssrf")
public class SsrfController {

    private final SsrfGuard ssrfGuard;

    public SsrfController(SsrfGuard ssrfGuard) {
        this.ssrfGuard = ssrfGuard;
    }

    // FIXED: SSRF - fetches validated through SsrfGuard (HTTPS allowlist, no private IPs)
    @GetMapping(value = "/fetch", produces = MediaType.TEXT_PLAIN_VALUE)
    public String fetch(@RequestParam String url) {
        return ssrfGuard.fetchHttps(url);
    }

    @GetMapping(value = "/webhook", produces = MediaType.TEXT_PLAIN_VALUE)
    public String webhook(@RequestParam String callback) {
        return "status:200 body-length:" + ssrfGuard.fetchHttps(callback).length();
    }

    @GetMapping(value = "/proxy", produces = MediaType.TEXT_PLAIN_VALUE)
    public String proxy(@RequestParam String target) {
        return ssrfGuard.fetchHttps(target);
    }

    @GetMapping(value = "/image", produces = MediaType.TEXT_PLAIN_VALUE)
    public String image(@RequestParam String src) {
        String body = ssrfGuard.fetchHttps(src);
        return "bytes:" + body.length();
    }
}
