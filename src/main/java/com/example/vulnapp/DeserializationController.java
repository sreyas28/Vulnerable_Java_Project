package com.example.vulnapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deserialize")
public class DeserializationController {

    private final ObjectMapper objectMapper;

    public DeserializationController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // FIXED: Insecure Deserialization - JSON parsed into GadgetPayload DTO instead of ObjectInputStream
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String deserialize(@RequestBody GadgetPayload payload) {
        if (payload.getLabel() != null) {
            InputValidator.requireLength(payload.getLabel(), "label", 256);
        }
        return "type:" + GadgetPayload.class.getName() + " label:" + payload.getLabel();
    }
}
