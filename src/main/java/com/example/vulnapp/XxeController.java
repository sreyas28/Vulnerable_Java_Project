package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/xxe")
public class XxeController {

    // FIXED: XXE Injection - hardened XML parsing via SecureXml
    @PostMapping(value = "/dom", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String parseDom(@RequestBody String xml) throws Exception {
        InputValidator.requireLength(xml, "xml", 32_768);
        return SecureXml.parseDom(xml).getDocumentElement().getTextContent();
    }

    @PostMapping(value = "/sax", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String parseSax(@RequestBody String xml) throws Exception {
        InputValidator.requireLength(xml, "xml", 32_768);
        SecureXml.parseSax(xml);
        return "parsed-length:" + xml.length();
    }

    @PostMapping(value = "/transform", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String transform(@RequestBody String xml) throws Exception {
        InputValidator.requireLength(xml, "xml", 32_768);
        return SecureXml.transform(xml);
    }
}
