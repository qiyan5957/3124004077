package com.essaycheck;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.UnsupportedEncodingException;

public class FileIO {
    private FileIO() {
    }
    /*读取文件字节并自动识别编码为字符串*/
    public static String readText(Path path) throws IOException {
        if (path == null) {
            throw new IOException("路径为空");
        }
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + path);
        }
        byte[] bytes = Files.readAllBytes(path);
        try {
            return EncodingDetector.decodeStrict(bytes);
        } catch (UnsupportedEncodingException e) {
            throw new IOException("文件编码无法识别: " + path, e);
        }
    }
    /*用 UTF-8 写入答案*/
    public static void writeText(Path path, String content) throws IOException {
        if (path == null) {
            throw new IOException("路径为空");
        }
        byte[] data = content.getBytes(StandardCharsets.UTF_8);
        Files.write(path, data);
    }
}
