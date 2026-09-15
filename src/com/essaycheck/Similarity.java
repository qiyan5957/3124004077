package com.essaycheck;

import java.util.HashMap;
import java.util.Map;

public class Similarity {
    private Similarity() {
    }

    private static final int N = 2;

    public static double cosineSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null
                || text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }

        Map<String, Integer> v1 = buildNgramVector(text1);
        Map<String, Integer> v2 = buildNgramVector(text2);

        if (v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }

        // 遍历较小的向量，减少哈希查找次数
        Map<String, Integer> smaller = v1.size() <= v2.size() ? v1 : v2;
        Map<String, Integer> larger = (smaller == v1) ? v2 : v1;

        double dot = 0.0;
        for (Map.Entry<String, Integer> e : smaller.entrySet()) {
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

    private static Map<String, Integer> buildNgramVector(String text) {
        Map<String, Integer> vector = new HashMap<>();
        int len = text.length();

        if (len < N) {
            if (len > 0) {
                vector.put(text, 1);
            }
            return vector;
        }

        for (int i = 0; i <= len - N; i++) {
            String gram = text.substring(i, i + N);
            Integer old = vector.get(gram);
            if (old == null) {
                vector.put(gram, 1);
            } else {
                vector.put(gram, old + 1);
            }
        }
        return vector;
    }
}
