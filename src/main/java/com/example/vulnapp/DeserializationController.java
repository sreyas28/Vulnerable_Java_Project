package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;

@RestController
@RequestMapping("/api/deserialize")
public class DeserializationController {

    // VULN: Insecure Deserialization - ObjectInputStream on attacker-controlled Base64 bytes
    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String deserialize(@RequestBody String base64Payload) throws Exception {
        byte[] data = Base64.getDecoder().decode(base64Payload.trim());
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object obj = ois.readObject();
            return "type:" + obj.getClass().getName();
        }
    }
}
