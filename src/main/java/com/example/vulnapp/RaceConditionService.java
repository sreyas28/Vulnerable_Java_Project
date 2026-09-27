package com.example.vulnapp;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RaceConditionService {

    private final JdbcTemplate jdbc;
    private final AtomicInteger visitCounter = new AtomicInteger();
    private final Map<String, Boolean> coupons = new ConcurrentHashMap<>();

    public RaceConditionService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        coupons.put("TRAIN50", false);
    }

    // VULN: Race Conditions (TOCTOU) - check file existence then act without locking
    public String claimFileToken(String filename) throws Exception {
        Path p = Path.of("sandbox", filename);
        if (Files.exists(p)) {
            Thread.sleep(50);
            return "token-for-" + p.getFileName();
        }
        return "missing";
    }

    // VULN: Race Conditions (TOCTOU) - non-atomic read-modify-write on account balance
    public String transfer(int accountId, BigDecimal amount) {
        Map<String, Object> row = jdbc.queryForMap("SELECT balance FROM accounts WHERE id = ?", accountId);
        Object raw = row.get("BALANCE") != null ? row.get("BALANCE") : row.get("balance");
        BigDecimal balance = (BigDecimal) raw;
        if (balance.compareTo(amount) >= 0) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            BigDecimal next = balance.subtract(amount);
            jdbc.update("UPDATE accounts SET balance = ? WHERE id = ?", next, accountId);
            return "ok:" + next;
        }
        return "insufficient";
    }

    // VULN: Race Conditions (TOCTOU) - increment counter with separate get/set
    public int incrementVisits() {
        int current = visitCounter.get();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        visitCounter.set(current + 1);
        return visitCounter.get();
    }

    // VULN: Race Conditions (TOCTOU) - coupon check and redeem not atomic
    public String redeemCoupon(String code) {
        if (coupons.getOrDefault(code, true)) {
            return "already used";
        }
        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        coupons.put(code, true);
        return "redeemed";
    }
}
