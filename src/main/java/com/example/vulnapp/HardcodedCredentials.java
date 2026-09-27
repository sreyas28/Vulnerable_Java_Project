package com.example.vulnapp;

/**
 * Intentionally hardcoded dummy secrets for SAST training only.
 */
public final class HardcodedCredentials {

    // VULN: Hardcoded Credentials - fake AWS access key embedded in source
    public static final String AWS_ACCESS_KEY_ID = "AKIAEXAMPLEEXAMPLE00";
    // VULN: Hardcoded Credentials - fake AWS secret key embedded in source
    public static final String AWS_SECRET_ACCESS_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    // VULN: Hardcoded Credentials - database password constant
    public static final String DB_PASSWORD = "SuperSecretDbPass123!";

    // VULN: Hardcoded Credentials - JWT signing secret in code
    public static final String JWT_SECRET = "jwt-training-secret-do-not-use";

    // VULN: Hardcoded Credentials - SSH private key stub in source
    public static final String SSH_PRIVATE_KEY = "-----BEGIN OPENSSH PRIVATE KEY-----\n"
            + "FAKE-KEY-FOR-TRAINING-ONLY\n"
            + "-----END OPENSSH PRIVATE KEY-----";

    // VULN: Hardcoded Credentials - third-party API token constant
    public static final String API_TOKEN = "sk_live_example_0123456789abcdef";

    private HardcodedCredentials() {
    }
}
