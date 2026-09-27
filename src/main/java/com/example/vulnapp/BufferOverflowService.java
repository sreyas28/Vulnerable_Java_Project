package com.example.vulnapp;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BufferOverflowService {

    private static final int MAX_ARRAY_BYTES = 16 * 1024 * 1024;
    private static final int MAX_MB = 64;

    // FIXED: Buffer Overflow - bounds-checked array write replaces sun.misc.Unsafe and JNI
    public int safeWrite(byte[] buffer, int index, byte value) {
        if (index < 0 || index >= buffer.length) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        buffer[index] = value;
        return buffer.length;
    }

    // FIXED: Buffer Overflow - Math.multiplyExact prevents integer overflow in size calculation
    public byte[] allocateWithOverflow(int count, int elementSize) {
        int total = Math.multiplyExact(count, elementSize);
        if (total > MAX_ARRAY_BYTES || total < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        return new byte[total];
    }

    // FIXED: Buffer Overflow - capped allocation size prevents unbounded OOM requests
    public byte[] allocateHuge(int sizeMb) {
        if (sizeMb < 0 || sizeMb > MAX_MB) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        int bytes = Math.multiplyExact(sizeMb, 1024 * 1024);
        return new byte[bytes];
    }
}
