# vulnapp — secured training baseline

This project is a **hardened** Spring Boot application used to compare SonarQube (and other scanner) results against the earlier intentionally vulnerable revision. Endpoints and file layout match the vulnerable version **1:1**; fixes are documented below.

**Run only on `127.0.0.1` in a local lab.** Do not expose to untrusted networks.

## Requirements

- Java 17+
- Maven 3.9+
- Local SonarQube (optional): `http://localhost:9000`

## Configuration

Copy `.env.example` and export required variables before starting (no defaults in code):

```bash
export DB_PASSWORD='local-dev-only'
export AWS_ACCESS_KEY_ID='AKIAEXAMPLEEXAMPLE00'
export AWS_SECRET_ACCESS_KEY='wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY'
export JWT_SECRET='jwt-training-secret-do-not-use'
export SSH_PRIVATE_KEY='-----BEGIN FAKE KEY-----'
export API_TOKEN='sk_live_example_0123456789abcdef'
export AES_KEY_BASE64="$(openssl rand -base64 32)"
```

## Run

```bash
mvn spring-boot:run
```

Server: `http://127.0.0.1:8080`

Login (`GET /api/sql/login`) returns an `X-Auth-Token` value on success. Send it on `/api/access/**` requests:

```bash
curl -H "X-Auth-Token: <token>" http://127.0.0.1:8080/api/access/users/alice
```

## SonarQube

```bash
mvn clean verify sonar:sonar -Dsonar.token=YOUR_TOKEN
```

## Before / After

| # | Category | Fix applied |
|---|----------|-------------|
| 1 | SQL Injection | Parameterized queries; BCrypt login; ORDER BY column/direction allowlists |
| 2 | Cross-Site Scripting | HtmlUtils escaping, plain-text where possible, textContent DOM sink, CSP header |
| 3 | Command Injection | No shell; Java reachability check and sandbox file listing with regex allowlists |
| 4 | Path Traversal | Sandbox root resolution with normalize/toRealPath and prefix checks; write extension allowlist |
| 5 | Insecure Deserialization | JSON DTO via Jackson; removed Java serialization gadget behavior |
| 6 | Hardcoded Credentials | Secrets from environment/`application.properties`; startup validation; `.env.example` |
| 7 | Weak Cryptography | SHA-256 integrity, BCrypt passwords, AES-256/GCM, SecureRandom tokens, config-derived AES key |
| 8 | XXE Injection | Secure XML parser/transformer factory hardening (DTD/entities disabled, secure processing) |
| 9 | SSRF | HTTPS host allowlist, DNS private/loopback/metadata blocking, timeouts, size cap, no redirects |
| 10 | Buffer Overflow / Memory | Bounds-checked writes, Math.multiplyExact, max allocation caps; removed Unsafe/JNI |
| 11 | Improper Access Control | Token auth interceptor, IDOR checks, admin role gate, no mass role assignment, no password in API |
| 12 | Race Conditions | Direct file open, atomic SQL balance/coupon updates, AtomicInteger counter |

## Layout

- `src/main/java/com/example/vulnapp/` — controllers and services (same paths as vulnerable revision)
- `src/main/resources/schema.sql` — H2 seed data (`users`, `accounts`, `coupons`)
- `sandbox/` — sample files for file/command demos
