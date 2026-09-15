package com.essaycheck;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
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
    /** UTF-16 BE（带 BOM） */
    @Test
    public void testUtf16BeBom() {
        byte[] content = "你好".getBytes(StandardCharsets.UTF_16BE);
        byte[] data = new byte[2 + content.length];
        data[0] = (byte) 0xFE;
        data[1] = (byte) 0xFF;
        System.arraycopy(content, 0, data, 2, content.length);
        assertEquals("你好", EncodingDetector.decode(data));
    }

    /** UTF-16 LE（带 BOM） */
    @Test
    public void testUtf16LeBom() {
        byte[] content = "你好".getBytes(StandardCharsets.UTF_16LE);
        byte[] data = new byte[2 + content.length];
        data[0] = (byte) 0xFF;
        data[1] = (byte) 0xFE;
        System.arraycopy(content, 0, data, 2, content.length);
        assertEquals("你好", EncodingDetector.decode(data));
    }

    /** 严格解码：无法识别编码时应抛 UnsupportedEncodingException */
    @Test
    public void testDecodeStrictInvalid() {
        // 0xFF 开头在 UTF-8 / GBK 中都是非法字节序列
        byte[] invalid = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        try {
            EncodingDetector.decodeStrict(invalid);
            fail("应该抛出 UnsupportedEncodingException");
        } catch (UnsupportedEncodingException e) {
            assertTrue(e.getMessage().contains("无法识别"));
        }
    }

    /** 宽容解码：非法字节流也不抛异常，返回兜底字符串 */
    @Test
    public void testDecodeLenientNeverThrows() {
        byte[] invalid = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        String result = EncodingDetector.decode(invalid);
        assertNotNull(result);
    }
}