package com.example.vulnapp;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class WeakCryptoService {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final byte[] aesKey;

    public WeakCryptoService(@Value("${app.crypto.aes-key-base64}") String aesKeyBase64) {
        this.aesKey = Base64.getDecoder().decode(aesKeyBase64);
    }

    @PostConstruct
    void validateKey() {
        if (aesKey.length != 32) {
            throw new IllegalStateException("AES_KEY_BASE64 must decode to 32 bytes");
        }
    }

    // FIXED: Weak Cryptography - SHA-256 used instead of MD5 for integrity hashing
    public String md5Hash(String input) throws Exception {
        return sha256Hex(input);
    }

    // FIXED: Weak Cryptography - SHA-256 used instead of SHA-1 for integrity hashing
    public String sha1Hash(String input) throws Exception {
        return sha256Hex(input);
    }

    // FIXED: Weak Cryptography - AES-256/GCM with random IV replaces DES
    public String desEncrypt(String plaintext) throws Exception {
        return encryptAesGcm(plaintext);
    }

    // FIXED: Weak Cryptography - AES-256/GCM with random IV per message replaces AES/ECB
    public String aesEcbEncrypt(String plaintext) throws Exception {
        return encryptAesGcm(plaintext);
    }

    // FIXED: Weak Cryptography - SecureRandom replaces java.util.Random for tokens
    public String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashPassword(String plaintext) {
        return passwordEncoder.encode(plaintext);
    }

    private String encryptAesGcm(String plaintext) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    private static String sha256Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
