package com.example.vulnapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SsrfGuard {

    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 3000;
    private static final int MAX_BYTES = 64 * 1024;

    private final Set<String> allowedHosts;

    public SsrfGuard(@Value("${app.ssrf.allowed-hosts}") String allowedHosts) {
        this.allowedHosts = Arrays.stream(allowedHosts.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    // FIXED: SSRF - HTTPS allowlist, DNS resolution checks, timeouts, size limits, no redirects
    public String fetchHttps(String urlString) {
        InputValidator.requireLength(urlString, "url", 2048);
        try {
            URI uri = URI.create(urlString);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
            }
            String host = uri.getHost();
            if (host == null || !allowedHosts.contains(host.toLowerCase())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
            }
            rejectPrivateAddresses(host);

            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestMethod("GET");
            int status = conn.getResponseCode();
            if (status >= 300 && status < 400) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
            }
            try (InputStream in = conn.getInputStream()) {
                return readLimited(in);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
    }

    private static void rejectPrivateAddresses(String host) throws Exception {
        for (InetAddress addr : InetAddress.getAllByName(host)) {
            if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()
                    || addr.isLinkLocalAddress() || addr.isSiteLocalAddress()
                    || addr.isMulticastAddress()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
            }
        }
    }

    private static String readLimited(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int total = 0;
        int read;
        while ((read = in.read(buf)) != -1) {
            total += read;
            if (total > MAX_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
            }
            out.write(buf, 0, read);
        }
        return out.toString(StandardCharsets.UTF_8);
    }
}
