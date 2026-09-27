package com.example.vulnapp;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RaceConditionService {

    private final JdbcTemplate jdbc;
    private final AtomicInteger visitCounter = new AtomicInteger();

    public RaceConditionService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // FIXED: Race Conditions (TOCTOU) - open file directly without separate existence check
    public String claimFileToken(String filename) throws Exception {
        InputValidator.requireLength(filename, "file", 128);
        var path = SandboxPaths.resolveSafe(filename);
        try (InputStream in = Files.newInputStream(path)) {
            byte[] header = in.readNBytes(16);
            return "token-for-" + path.getFileName() + "-bytes:" + header.length;
        }
    }

    // FIXED: Race Conditions (TOCTOU) - single atomic UPDATE with balance guard
    public String transfer(int accountId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return "invalid amount";
        }
        int updated = jdbc.update(
                "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?",
                amount, accountId, amount);
        return updated == 1 ? "ok" : "insufficient";
    }

    // FIXED: Race Conditions (TOCTOU) - AtomicInteger incrementAndGet
    public int incrementVisits() {
        return visitCounter.incrementAndGet();
    }

    // FIXED: Race Conditions (TOCTOU) - atomic coupon redeem via conditional UPDATE
    public String redeemCoupon(String code) {
        String safeCode = InputValidator.requireLength(code, "code", 32);
        int updated = jdbc.update(
                "UPDATE coupons SET redeemed = TRUE WHERE code = ? AND redeemed = FALSE", safeCode);
        return updated == 1 ? "redeemed" : "already used";
    }
}
