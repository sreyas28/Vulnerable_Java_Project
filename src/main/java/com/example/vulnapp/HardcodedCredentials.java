package com.example.vulnapp;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "app.credentials")
public class HardcodedCredentials {

    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private String jwtSecret;
    private String sshPrivateKey;
    private String apiToken;

    // FIXED: Hardcoded Credentials - secrets loaded from environment via configuration properties
    @PostConstruct
    void validateRequiredSecrets() {
        requireNonBlank(awsAccessKeyId, "AWS_ACCESS_KEY_ID");
        requireNonBlank(awsSecretAccessKey, "AWS_SECRET_ACCESS_KEY");
        requireNonBlank(jwtSecret, "JWT_SECRET");
        requireNonBlank(sshPrivateKey, "SSH_PRIVATE_KEY");
        requireNonBlank(apiToken, "API_TOKEN");
    }

    private static void requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required secret: " + name);
        }
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public String getSshPrivateKey() {
        return sshPrivateKey;
    }

    public void setSshPrivateKey(String sshPrivateKey) {
        this.sshPrivateKey = sshPrivateKey;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
}
