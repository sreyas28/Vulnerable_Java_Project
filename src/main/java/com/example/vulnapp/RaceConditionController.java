package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/race")
public class RaceConditionController {

    private final RaceConditionService races;

    public RaceConditionController(RaceConditionService races) {
        this.races = races;
    }

    @GetMapping(value = "/file-token", produces = MediaType.TEXT_PLAIN_VALUE)
    public String fileToken(@RequestParam String file) throws Exception {
        return races.claimFileToken(file);
    }

    @PostMapping(value = "/transfer", produces = MediaType.TEXT_PLAIN_VALUE)
    public String transfer(@RequestParam int accountId, @RequestParam BigDecimal amount) {
        return races.transfer(accountId, amount);
    }

    @GetMapping(value = "/counter", produces = MediaType.TEXT_PLAIN_VALUE)
    public String counter() {
        return "visits:" + races.incrementVisits();
    }

    @PostMapping(value = "/coupon", produces = MediaType.TEXT_PLAIN_VALUE)
    public String coupon(@RequestParam(defaultValue = "TRAIN50") String code) {
        return races.redeemCoupon(code);
    }
}
