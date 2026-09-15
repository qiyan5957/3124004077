package com.essaycheck;

import org.junit.Test;
import static org.junit.Assert.*;

public class SimilarityTest {

    @Test
    public void testSameText() {
        double score = Similarity.cosineSimilarity("今天天气真好", "今天天气真好");
        assertEquals(1.0, score, 0.01);
    }

    @Test
    public void testCompletelyDifferent() {
        double score = Similarity.cosineSimilarity("苹果香蕉", "宇宙飞船");
        assertTrue(score < 0.3);
    }

    @Test
    public void testPartialOverlap() {
        double score = Similarity.cosineSimilarity("今天天气真好", "今天天气不错");
        assertTrue(score > 0.0 && score < 1.0);
    }
    /** 空字符串应该返回 0.0，不抛异常 */
    @Test
    public void testEmptyStrings() {
        assertEquals(0.0, Similarity.cosineSimilarity("", "你好"), 0.01);
        assertEquals(0.0, Similarity.cosineSimilarity("你好", ""), 0.01);
        assertEquals(0.0, Similarity.cosineSimilarity("", ""), 0.01);
    }

    /** null 输入应该抛 IllegalArgumentException */
    @Test(expected = IllegalArgumentException.class)
    public void testNullThrows() {
        Similarity.cosineSimilarity(null, "你好");
    }

    /** null 作为第二个参数也应该抛异常 */
    @Test(expected = IllegalArgumentException.class)
    public void testNullSecondThrows() {
        Similarity.cosineSimilarity("你好", null);
    }
}