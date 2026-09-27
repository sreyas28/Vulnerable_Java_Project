package com.example.vulnapp;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;

@Service
public class WeakCryptoService {

    private static final byte[] STATIC_IV = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);
    private static final byte[] WEAK_AES_KEY = "key".getBytes(StandardCharsets.UTF_8);

    // VULN: Weak Cryptography - MD5 used for password hashing
    public String md5Hash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    // VULN: Weak Cryptography - SHA-1 used for integrity
    public String sha1Hash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return bytesToHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
    }

    // VULN: Weak Cryptography - DES with short key material
    public String desEncrypt(String plaintext) throws Exception {
        SecretKeySpec key = new SecretKeySpec("8bytekey".getBytes(StandardCharsets.UTF_8), "DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    // VULN: Weak Cryptography - AES/ECB mode with static IV and weak key length
    public String aesEcbEncrypt(String plaintext) throws Exception {
        SecretKeySpec key = new SecretKeySpec(WEAK_AES_KEY, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(STATIC_IV));
        return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)));
    }

    // VULN: Weak Cryptography - java.util.Random used for security-sensitive token
    public String randomToken() {
        Random r = new Random();
        return Integer.toHexString(r.nextInt()) + Integer.toHexString(r.nextInt());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
