package com.essaycheck;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * FileIO 单元测试：
 * 覆盖 readText 的正常读取、UTF-8 BOM 读取、文件不存在异常，
 * 以及 writeText 的正常写入。
 */
public class FileIOTest {

    private Path tempDir;
    private Path tempFile;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("essaycheck_test_");
        tempFile = tempDir.resolve("sample.txt");
    }

    @After
    public void tearDown() throws IOException {
        // 清理临时文件
        if (Files.exists(tempFile)) {
            Files.deleteIfExists(tempFile);
        }
        if (Files.exists(tempDir)) {
            Files.deleteIfExists(tempDir);
        }
    }

    /** 正常路径：读取一个普通 UTF-8 文本 */
    @Test
    public void testReadTextNormal() throws IOException {
        String content = "今天天气真好，适合写代码。";
        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));

        String result = FileIO.readText(tempFile);
        assertEquals(content, result);
    }

    /** 边界路径：读取带 UTF-8 BOM 的文件，BOM 应被剥离 */
    @Test
    public void testReadTextWithBom() throws IOException {
        String content = "你好世界";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[3 + contentBytes.length];
        withBom[0] = (byte) 0xEF;
        withBom[1] = (byte) 0xBB;
        withBom[2] = (byte) 0xBF;
        System.arraycopy(contentBytes, 0, withBom, 3, contentBytes.length);
        Files.write(tempFile, withBom);

        String result = FileIO.readText(tempFile);
        assertEquals(content, result);
    }

    /** 异常路径：文件不存在，应抛 IOException */
    @Test
    public void testReadTextFileNotFound() {
        Path notExist = tempDir.resolve("not_exist.txt");
        try {
            FileIO.readText(notExist);
            fail("应该抛出 IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("文件不存在"));
        }
    }

    /** 异常路径：传入 null 路径，应抛 IOException */
    @Test
    public void testReadTextNullPath() {
        try {
            FileIO.readText(null);
            fail("应该抛出 IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("路径为空"));
        }
    }

    /** 正常路径：写入文本文件，内容应与写入一致 */
    @Test
    public void testWriteTextNormal() throws IOException {
        Path out = tempDir.resolve("out.txt");
        String content = "0.62";

        FileIO.writeText(out, content);

        assertTrue(Files.exists(out));
        String readBack = new String(Files.readAllBytes(out), StandardCharsets.UTF_8);
        assertEquals(content, readBack);

        Files.deleteIfExists(out);
    }

    /** 异常路径：写入 null 路径，应抛 IOException */
    @Test
    public void testWriteTextNullPath() {
        try {
            FileIO.writeText(null, "content");
            fail("应该抛出 IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("路径为空"));
        }
    }
}