package com.essaycheck;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class EncodingDetectorTest {

    @Test
    public void testUtf8NoBom() {
        byte[] data = "你好世界".getBytes(StandardCharsets.UTF_8);
        assertEquals("你好世界", EncodingDetector.decode(data));
    }

    @Test
    public void testUtf8Bom() {
        byte[] content = "你好".getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[3 + content.length];
        data[0] = (byte) 0xEF;
        data[1] = (byte) 0xBB;
        data[2] = (byte) 0xBF;
        System.arraycopy(content, 0, data, 3, content.length);
        assertEquals("你好", EncodingDetector.decode(data));
    }

    @Test
    public void testGbk() throws Exception {
        byte[] data = "你好世界".getBytes("GBK");
        assertEquals("你好世界", EncodingDetector.decode(data));
    }

    @Test
    public void testEmptyBytes() {
        assertEquals("", EncodingDetector.decode(new byte[0]));
        assertEquals("", EncodingDetector.decode(null));
    }
}