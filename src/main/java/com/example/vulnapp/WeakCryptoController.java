package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crypto")
public class WeakCryptoController {

    private final WeakCryptoService crypto;

    public WeakCryptoController(WeakCryptoService crypto) {
        this.crypto = crypto;
    }

    @GetMapping(value = "/md5", produces = MediaType.TEXT_PLAIN_VALUE)
    public String md5(@RequestParam String value) throws Exception {
        return crypto.md5Hash(value);
    }

    @GetMapping(value = "/sha1", produces = MediaType.TEXT_PLAIN_VALUE)
    public String sha1(@RequestParam String value) throws Exception {
        return crypto.sha1Hash(value);
    }

    @GetMapping(value = "/des", produces = MediaType.TEXT_PLAIN_VALUE)
    public String des(@RequestParam String value) throws Exception {
        return crypto.desEncrypt(value);
    }

    @GetMapping(value = "/aes-ecb", produces = MediaType.TEXT_PLAIN_VALUE)
    public String aes(@RequestParam String value) throws Exception {
        return crypto.aesEcbEncrypt(value);
    }

    @GetMapping(value = "/token", produces = MediaType.TEXT_PLAIN_VALUE)
    public String token() {
        return crypto.randomToken();
    }
}
