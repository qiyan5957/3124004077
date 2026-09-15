package com.essaycheck;

import org.junit.Test;
import static org.junit.Assert.*;

public class TextCleanerTest {

    @Test
    public void testPureChinese() {
        assertEquals("今天是星期天", TextCleaner.clean("今天是星期天"));
    }

    @Test
    public void testHtmlTags() {
        assertEquals("你好世界", TextCleaner.clean("<div>你好</div><p>世界</p>"));
    }

    @Test
    public void testHtmlEntities() {
        // &nbsp; 被替换成空格，中文字符才被保留
        assertEquals("你好", TextCleaner.clean("&nbsp;你好&nbsp;"));
    }

    @Test
    public void testScriptAndStyle() {
        assertEquals("正文", TextCleaner.clean("<script>alert(1)</script>正文"));
    }

    @Test
    public void testEnglishAndNumbers() {
        assertEquals("世界", TextCleaner.clean("Hello 世界 123"));
    }

    @Test
    public void testEmptyAndNull() {
        assertEquals("", TextCleaner.clean(""));
        assertEquals("", TextCleaner.clean(null));
    }
}