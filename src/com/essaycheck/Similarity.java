package com.essaycheck;

import java.util.HashMap;
import java.util.Map;

public final class Similarity {

    private Similarity() {
    }

    private static final int N = 2;

    public static double cosineSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            throw new IllegalArgumentException("待比较的文本不能为 null");
        }
        if (text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }


        // 优化 1：完全相同直接返回 1.0，省掉后面的所有计算
        if (text1.equals(text2)) {
            return 1.0;
        }

        Map<Integer, Integer> v1 = buildNgramVector(text1);
        Map<Integer, Integer> v2 = buildNgramVector(text2);

        if (v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }

        // 优化 2：遍历较小的 Map，减少哈希查找次数
        Map<Integer, Integer> smaller = v1.size() <= v2.size() ? v1 : v2;
        Map<Integer, Integer> larger = (smaller == v1) ? v2 : v1;

        double dot = 0.0;
        for (Map.Entry<Integer, Integer> e : smaller.entrySet()) {
            Integer other = larger.get(e.getKey());
            if (other != null) {
                dot += (double) e.getValue() * other;
            }
        }

        double norm1 = 0.0;
        for (int c : v1.values()) {
            norm1 += (double) c * c;
        }
        double norm2 = 0.0;
        for (int c : v2.values()) {
            norm2 += (double) c * c;
        }

        double denom = Math.sqrt(norm1) * Math.sqrt(norm2);
        if (denom == 0.0) {
            return 0.0;
        }

        double sim = dot / denom;
        if (sim < 0.0) {
            sim = 0.0;
        }
        if (sim > 1.0) {
            sim = 1.0;
        }
        return sim;
    }

    /**
     * 优化 3：用 int 编码二元组 (c1 << 16) | c2 作为 key
     * 替代原来的 String.substring + HashMap<String,Integer>
     * 避免创建大量临时 String 对象，并减少哈希表的存储开销。
     */
    private static Map<Integer, Integer> buildNgramVector(String text) {
        int len = text.length();
        // 优化 4：按长度预估容量，减少 rehash
        int cap = Math.max(16, (int) (len * 1.4));
        Map<Integer, Integer> vector = new HashMap<>(cap);

        if (len < N) {
            if (len > 0) {
                // 短文本仍用字符串的唯一编码，保证不会与其他内容冲突
                vector.put(text.hashCode(), 1);
            }
            return vector;
        }

        for (int i = 0; i <= len - N; i++) {
            int key = (text.charAt(i) << 16) | text.charAt(i + 1);
            Integer old = vector.get(key);
            if (old == null) {
                vector.put(key, 1);
            } else {
                vector.put(key, old + 1);
            }
        }
        return vector;
    }
}