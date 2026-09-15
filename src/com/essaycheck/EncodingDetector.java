package com.essaycheck;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * 编码探测器。
 * 支持 UTF-8 / UTF-16 BE / UTF-16 LE / GBK 识别。
 */
public final class EncodingDetector {

    /** GBK 字符集。*/
    private static final Charset GBK = Charset.forName("GBK");

    private static final int BYTE_MASK = 0xFF;
    private static final int UTF8_BOM_FIRST = 0xEF;
    private static final int UTF8_BOM_SECOND = 0xBB;
    private static final int UTF8_BOM_THIRD = 0xBF;
    private static final int UTF16_BOM_START = 0xFE;
    private static final int UTF16_LE_BOM_END = 0xFF;
    private static final int BOM_UTF8_LEN = 3;
    private static final int BOM_UTF16_LEN = 2;

    private EncodingDetector() {
    }

    /**
     * 宽容版解码：任何情况都返回一个字符串，绝不抛异常。
     * 主程序使用此版本，保证极端情况下也不会崩溃。
     *
     */
    public static String decode(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        try {
            return decodeStrict(bytes);
        } catch (UnsupportedEncodingException e) {
            // 严格模式都无法识别，兜底用 UTF-8 + 替换字符
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    //无法识别编码时抛出 UnsupportedEncodingException。

    public static String decodeStrict(final byte[] bytes) throws UnsupportedEncodingException {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        // 1. BOM 优先
        if (bytes.length >= BOM_UTF8_LEN
                && (bytes[0] & BYTE_MASK) == UTF8_BOM_FIRST
                && (bytes[1] & BYTE_MASK) == UTF8_BOM_SECOND
                && (bytes[2] & BYTE_MASK) == UTF8_BOM_THIRD) {
            return new String(bytes, BOM_UTF8_LEN, bytes.length - BOM_UTF8_LEN, StandardCharsets.UTF_8);
        }
        if (bytes.length >= BOM_UTF16_LEN
                && (bytes[0] & BYTE_MASK) == UTF16_BOM_START
                && (bytes[1] & BYTE_MASK) == UTF16_LE_BOM_END) {
            return new String(bytes, BOM_UTF16_LEN, bytes.length - BOM_UTF16_LEN, StandardCharsets.UTF_16BE);
        }
        if (bytes.length >= BOM_UTF16_LEN
                && (bytes[0] & BYTE_MASK) == UTF16_LE_BOM_END
                && (bytes[1] & BYTE_MASK) == UTF16_BOM_START) {
            return new String(bytes, BOM_UTF16_LEN, bytes.length - BOM_UTF16_LEN, StandardCharsets.UTF_16LE);
        }

        // 2. 严格试 UTF-8
        try {
            return strictDecode(bytes, StandardCharsets.UTF_8);
        } catch (CharacterCodingException ignored) {
            // 继续
        }

        // 3. 严格试 GBK
        try {
            return strictDecode(bytes, GBK);
        } catch (CharacterCodingException ignored) {
            // 继续
        }

        // 4. 都失败 → 抛出异常
        throw new UnsupportedEncodingException("无法识别的文件编码：不是合法的 UTF-8 / UTF-16 / GBK 字节流");
    }

    private static String strictDecode(final byte[] bytes, final Charset charset)
            throws CharacterCodingException {
        final CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        return decoder.decode(ByteBuffer.wrap(bytes)).toString();
    }
}