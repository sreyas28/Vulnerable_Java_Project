package com.example.vulnapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(HardcodedCredentials.class)
public class VulnAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(VulnAppApplication.class, args);
    }
}
