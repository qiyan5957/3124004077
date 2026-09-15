package com.essaycheck;

import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ExceptionTest {

    /** 场景 1：空参数 → IllegalArgumentException */
    @Test(expected = IllegalArgumentException.class)
    public void testNullTextThrows() {
        Similarity.cosineSimilarity(null, "你好");
    }

    /** 场景 2：文件不存在 → IOException */
    @Test
    public void testFileNotFound() {
        try {
            FileIO.readText(Paths.get("不存在的文件_" + System.currentTimeMillis() + ".txt"));
            fail("应该抛出 IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("文件不存在"));
        }
    }

    /** 场景 3：非法编码字节流 → UnsupportedEncodingException */
    @Test
    public void testInvalidEncoding() {
        // 0xFF 开头在 UTF-8 / GBK 里都是非法字节序列
        byte[] invalid = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        try {
            EncodingDetector.decodeStrict(invalid);
            fail("应该抛出 UnsupportedEncodingException");
        } catch (UnsupportedEncodingException e) {
            assertTrue(e.getMessage().contains("无法识别"));
        }
    }

    /** 场景 4：宽容版不抛异常，返回兜底字符串 */
    @Test
    public void testLenientDecodeNeverThrows() {
        byte[] invalid = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        String result = EncodingDetector.decode(invalid);   // 不应抛异常
        assertNotNull(result);
    }
}