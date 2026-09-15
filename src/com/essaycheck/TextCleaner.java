package com.essaycheck;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextCleaner {
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
    private static final Pattern CHINESE_ONLY =
            Pattern.compile("[\\u4E00-\\u9FFF]");

    public static String clean(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // 为了减少峰值内存，尽量用局部变量并及时替换
        String s = text;
        text = null;

        s = SCRIPT_STYLE.matcher(s).replaceAll(" ");
        s = HTML_COMMENT.matcher(s).replaceAll(" ");
        s = decodeCommonEntities(s);
        s = HTML_TAG.matcher(s).replaceAll(" ");
        s = HTML_ENTITY.matcher(s).replaceAll(" ");

        // 只保留中文字符
        Matcher m = CHINESE_ONLY.matcher(s);
        StringBuilder sb = new StringBuilder(Math.min(s.length(), 1 << 20));
        while (m.find()) {
            sb.append(m.group());
        }

        return sb.toString();
    }

    private static String decodeCommonEntities(String s) {
        // 只替换常见实体，避免大对象反复构建
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
