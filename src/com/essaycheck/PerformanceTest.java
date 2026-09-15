package com.essaycheck;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class PerformanceTest {

    public static void main(String[] args) throws Exception {
        // 1. 读原文和抄袭版（用最大的那个文件，165KB）
        String path1 = "D:\\azzq\\软件工程\\论文查重\\EssayCheck\\测试文本\\orig.txt";
        String path2 = "D:\\azzq\\软件工程\\论文查重\\EssayCheck\\测试文本\\orig_0.8_dis_15.txt";

        String origRaw = new String(Files.readAllBytes(Paths.get(path1)), StandardCharsets.UTF_8);
        String copyRaw = new String(Files.readAllBytes(Paths.get(path2)), StandardCharsets.UTF_8);

        // 2. 预处理（只做一次，避免把清洗开销也算进去，或者你也可以故意算进去）
        String orig = TextCleaner.clean(origRaw);
        String copy = TextCleaner.clean(copyRaw);

        // 3. 热个身，让 JIT 先编译几遍
        for (int i = 0; i < 5; i++) {
            Similarity.cosineSimilarity(orig, copy);
        }

        // 4. 循环 200 次，让 Profiler 抓够样本
        long start = System.currentTimeMillis();
        double result = 0;
        for (int i = 0; i < 200; i++) {
            result = Similarity.cosineSimilarity(orig, copy);
        }
        long end = System.currentTimeMillis();

        System.out.println("相似度: " + result);
        System.out.println("200 次耗时: " + (end - start) + " ms");
        System.out.println("单次平均: " + ((end - start) / 200.0) + " ms");
    }
}