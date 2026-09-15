package com.essaycheck;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class  EncodingDetector {
    private EncodingDetector() {
    }

    private static final Charset GBK = Charset.forName("GBK");

    public static String decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        // 1. BOM 优先
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xEF
                && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        if (bytes.length >= 2
                && (bytes[0] & 0xFF) == 0xFE
                && (bytes[1] & 0xFF) == 0xFF) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        }
        if (bytes.length >= 2
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xFE) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        }

        // 2. 严格试 UTF-8
        try {
            return strictDecode(bytes, StandardCharsets.UTF_8);
        } catch (CharacterCodingException ignored) {
            // 继续尝试其它编码
        }

        // 3. 严格试 GBK
        try {
            return strictDecode(bytes, GBK);
        } catch (CharacterCodingException ignored) {
            // 继续
        }

        //  4. 兜底
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String strictDecode(byte[] bytes, Charset charset)
            throws CharacterCodingException {
        CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        return decoder.decode(ByteBuffer.wrap(bytes)).toString();
    }
}
