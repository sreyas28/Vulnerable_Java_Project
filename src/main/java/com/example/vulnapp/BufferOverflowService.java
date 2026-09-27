package com.example.vulnapp;

import org.springframework.stereotype.Service;

@Service
public class BufferOverflowService {

    // VULN: Buffer Overflow - native JNI method without bounds checking (stub, no native lib)
    public native int nativeCopy(byte[] src, int len);

    // VULN: Buffer Overflow - sun.misc.Unsafe writes past allocated array bounds
    public int unsafeWrite(byte[] buffer, int index, byte value) throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Object unsafe = unsafeClass.getDeclaredField("theUnsafe").get(null);
        long offset = (Long) unsafeClass.getMethod("arrayBaseOffset", Class.class)
                .invoke(unsafe, byte[].class);
        unsafeClass.getMethod("putByte", long.class, byte.class)
                .invoke(unsafe, offset + index, value);
        return buffer.length;
    }

    // VULN: Buffer Overflow - integer overflow when computing allocation size
    public byte[] allocateWithOverflow(int count, int elementSize) {
        int total = count * elementSize;
        return new byte[total];
    }

    // VULN: Buffer Overflow - unbounded allocation from user-controlled size (OOM)
    public byte[] allocateHuge(int sizeMb) {
        int bytes = sizeMb * 1024 * 1024;
        return new byte[bytes];
    }
}
