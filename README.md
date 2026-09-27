# vulnapp — intentional vulnerable Java training app

**WARNING:** This application is deliberately insecure for security education, SAST/DAST benchmarking, and lab exercises. **Never deploy it, expose it on a network, or run it except on `127.0.0.1` on an isolated machine.** It contains SQL injection, command execution, SSRF, XXE, and other flaws by design.

## Requirements

- Java 17+
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

The server listens on `http://127.0.0.1:8080` only.

For `/api/buffer/unsafe` (uses `sun.misc.Unsafe` at runtime), start with:

```bash
MAVEN_OPTS="--add-opens=java.base/sun.misc=ALL-UNNAMED" mvn spring-boot:run
```

## Vulnerability catalog

| # | Category | Endpoint | Example request |
|---|----------|----------|-----------------|
| 1 | SQL Injection | `GET /api/sql/login` | `curl 'http://127.0.0.1:8080/api/sql/login?username=admin'%27%20OR%20%271%27=%271&password=x'` |
| 1 | SQL Injection | `GET /api/sql/search` | `curl 'http://127.0.0.1:8080/api/sql/search?q=%25'%27%20UNION%20SELECT%20id,password,role%20FROM%20users--'` |
| 1 | SQL Injection | `GET /api/sql/users` | `curl 'http://127.0.0.1:8080/api/sql/users?sort=id;DROP%20TABLE%20users--'` |
| 2 | Cross-Site Scripting | `GET /api/xss/reflect` | `curl 'http://127.0.0.1:8080/api/xss/reflect?msg=<script>alert(1)</script>'` |
| 2 | Cross-Site Scripting | `POST /api/xss/comment` | `curl -X POST 'http://127.0.0.1:8080/api/xss/comment?text=<img%20src=x%20onerror=alert(1)>'` |
| 2 | Cross-Site Scripting | `GET /api/xss/dom` | Open `http://127.0.0.1:8080/api/xss/dom#<img src=x onerror=alert(1)>` in a browser |
| 2 | Cross-Site Scripting | `GET /api/xss/attr` | `curl 'http://127.0.0.1:8080/api/xss/attr?name="><script>alert(1)</script>'` |
| 3 | Command Injection | `GET /api/cmd/exec` | `curl 'http://127.0.0.1:8080/api/cmd/exec?host=127.0.0.1;id'` |
| 3 | Command Injection | `GET /api/cmd/build` | `curl 'http://127.0.0.1:8080/api/cmd/build?cmd=.;id'` |
| 4 | Path Traversal | `GET /api/files/read` | `curl 'http://127.0.0.1:8080/api/files/read?path=../README.md'` |
| 4 | Path Traversal | `POST /api/files/write` | `curl -X POST 'http://127.0.0.1:8080/api/files/write?path=../escaped.txt&content=oops'` |
| 4 | Path Traversal | `DELETE /api/files/delete` | `curl -X DELETE 'http://127.0.0.1:8080/api/files/delete?path=hello.txt'` |
| 4 | Path Traversal | `GET /api/files/list` | `curl 'http://127.0.0.1:8080/api/files/list?path=..'` |
| 5 | Insecure Deserialization | `POST /api/deserialize` | Serialize `com.example.vulnapp.Gadget` to bytes, Base64-encode, `curl -X POST --data-binary @payload.b64 http://127.0.0.1:8080/api/deserialize` |
| 6 | Hardcoded Credentials | _(source)_ | SAST: scan `HardcodedCredentials.java` for embedded dummy AWS/JWT/SSH/API secrets |
| 7 | Weak Cryptography | `GET /api/crypto/md5` | `curl 'http://127.0.0.1:8080/api/crypto/md5?value=password'` |
| 7 | Weak Cryptography | `GET /api/crypto/sha1` | `curl 'http://127.0.0.1:8080/api/crypto/sha1?value=password'` |
| 7 | Weak Cryptography | `GET /api/crypto/des` | `curl 'http://127.0.0.1:8080/api/crypto/des?value=secret'` |
| 7 | Weak Cryptography | `GET /api/crypto/aes-ecb` | `curl 'http://127.0.0.1:8080/api/crypto/aes-ecb?value=secret'` |
| 7 | Weak Cryptography | `GET /api/crypto/token` | `curl 'http://127.0.0.1:8080/api/crypto/token'` |
| 8 | XXE Injection | `POST /api/xxe/dom` | `curl -X POST -H 'Content-Type: application/xml' --data '<!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><root>&xxe;</root>' http://127.0.0.1:8080/api/xxe/dom` |
| 8 | XXE Injection | `POST /api/xxe/sax` | Same XML body to `http://127.0.0.1:8080/api/xxe/sax` |
| 8 | XXE Injection | `POST /api/xxe/transform` | Same XML body to `http://127.0.0.1:8080/api/xxe/transform` |
| 9 | SSRF | `GET /api/ssrf/fetch` | `curl 'http://127.0.0.1:8080/api/ssrf/fetch?url=http://169.254.169.254/latest/meta-data/'` |
| 9 | SSRF | `GET /api/ssrf/webhook` | `curl 'http://127.0.0.1:8080/api/ssrf/webhook?callback=http://127.0.0.1:8080/api/xss/reflect?msg=pwned'` |
| 9 | SSRF | `GET /api/ssrf/proxy` | `curl 'http://127.0.0.1:8080/api/ssrf/proxy?target=file:///etc/passwd'` |
| 9 | SSRF | `GET /api/ssrf/image` | `curl 'http://127.0.0.1:8080/api/ssrf/image?src=http://127.0.0.1:8080/'` (bypasses `localhost` string blocklist) |
| 10 | Buffer Overflow | `GET /api/buffer/unsafe` | `curl 'http://127.0.0.1:8080/api/buffer/unsafe?index=128&value=1'` |
| 10 | Buffer Overflow | `GET /api/buffer/multiply` | `curl 'http://127.0.0.1:8080/api/buffer/multiply?count=2000000000&size=2'` |
| 10 | Buffer Overflow | `GET /api/buffer/oom` | `curl 'http://127.0.0.1:8080/api/buffer/oom?mb=4096'` |
| 11 | Improper Access Control | `GET /api/access/accounts/{id}` | `curl 'http://127.0.0.1:8080/api/access/accounts/102'` |
| 11 | Improper Access Control | `POST /api/access/admin/reset-balances` | `curl -X POST 'http://127.0.0.1:8080/api/access/admin/reset-balances'` |
| 11 | Improper Access Control | `POST /api/access/users/role` | `curl -X POST 'http://127.0.0.1:8080/api/access/users/role?username=alice&role=admin'` |
| 11 | Improper Access Control | `POST /api/access/users/update` | `curl -X POST 'http://127.0.0.1:8080/api/access/users/update?username=bob&role=admin&password=hijacked'` |
| 11 | Improper Access Control | `GET /api/access/users/{username}` | `curl 'http://127.0.0.1:8080/api/access/users/alice'` |
| 12 | Race Conditions | `GET /api/race/file-token` | `curl 'http://127.0.0.1:8080/api/race/file-token?file=hello.txt'` (race delete between check/use) |
| 12 | Race Conditions | `POST /api/race/transfer` | Parallel `curl -X POST 'http://127.0.0.1:8080/api/race/transfer?accountId=100&amount=400'` |
| 12 | Race Conditions | `GET /api/race/counter` | Parallel `curl 'http://127.0.0.1:8080/api/race/counter'` |
| 12 | Race Conditions | `POST /api/race/coupon` | Parallel `curl -X POST 'http://127.0.0.1:8080/api/race/coupon?code=TRAIN50'` |

## Layout

- `src/main/java/com/example/vulnapp/` — Spring Boot controllers and services (one pattern per category)
- `src/main/resources/schema.sql` — H2 seed data for `users` and `accounts`
- `sandbox/` — sample files for path traversal and command demos
