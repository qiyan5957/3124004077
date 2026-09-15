package com.essaycheck;

import java.util.regex.Pattern;

public final class TextCleaner {

    private TextCleaner() {
    }

    private static final Pattern SCRIPT_STYLE =
            Pattern.compile("(?is)<script\\b[^>]*>.*?</script>|<style\\b[^>]*>.*?</style>");
    private static final Pattern HTML_COMMENT =
            Pattern.compile("(?s)<!--.*?-->");
    private static final Pattern HTML_TAG =
            Pattern.compile("(?s)<[^>]*>");
    private static final Pattern HTML_ENTITY =
            Pattern.compile("&[a-zA-Z#0-9]+;");

    public static String clean(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String s = text;
        text = null;

        // 大块无关内容先干掉，剩下的少量正则操作影响有限
        s = SCRIPT_STYLE.matcher(s).replaceAll(" ");
        s = HTML_COMMENT.matcher(s).replaceAll(" ");
        s = decodeCommonEntities(s);
        s = HTML_TAG.matcher(s).replaceAll(" ");
        s = HTML_ENTITY.matcher(s).replaceAll(" ");

        // 优化核心：原先使用 Matcher.find() 循环取中文，改为直接遍历 char 数组
        int len = s.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c >= '\u4E00' && c <= '\u9FFF') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String decodeCommonEntities(String s) {
        if (s.indexOf('&') < 0) {
            return s;
        }
        return s.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'");
    }
}