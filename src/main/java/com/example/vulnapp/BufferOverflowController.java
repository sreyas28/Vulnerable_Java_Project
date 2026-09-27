package com.example.vulnapp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buffer")
public class BufferOverflowController {

    private final BufferOverflowService buffers;

    public BufferOverflowController(BufferOverflowService buffers) {
        this.buffers = buffers;
    }

    @GetMapping(value = "/unsafe", produces = MediaType.TEXT_PLAIN_VALUE)
    public String unsafe(@RequestParam(defaultValue = "16") int index,
                         @RequestParam(defaultValue = "42") int value) throws Exception {
        byte[] buf = new byte[8];
        buffers.unsafeWrite(buf, index, (byte) value);
        return "len:" + buf.length;
    }

    @GetMapping(value = "/multiply", produces = MediaType.TEXT_PLAIN_VALUE)
    public String multiply(@RequestParam int count, @RequestParam int size) {
        return "allocated:" + buffers.allocateWithOverflow(count, size).length;
    }

    @GetMapping(value = "/oom", produces = MediaType.TEXT_PLAIN_VALUE)
    public String oom(@RequestParam(defaultValue = "512") int mb) {
        return "allocated:" + buffers.allocateHuge(mb).length;
    }
}
