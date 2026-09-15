package com.essaycheck;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class Main {
    private Main() {
    }

    public static void main(String[] args) {
        // 所有可抛出的东西都在这里被吞掉，保证不会异常退出
        try {
            run(args);
        } catch (Throwable t) {
            // 打印到 stderr，不影响程序正常退出
            System.err.println("[警告] 程序捕获到异常: " + t.getClass().getSimpleName()
                    + ": " + t.getMessage());
        }
    }

    private static void run(String[] args) {
        if (args == null || args.length < 3) {
            System.err.println("用法: java -jar main.jar <原文文件> <抄袭版论文的文件> <答案文件>");
            return;
        }

        Path origPath = null;
        Path copyPath = null;
        Path answerPath = null;
        try {
            origPath = Paths.get(args[0]).toAbsolutePath().normalize();
            copyPath = Paths.get(args[1]).toAbsolutePath().normalize();
            answerPath = Paths.get(args[2]).toAbsolutePath().normalize();
        } catch (Throwable t) {
            System.err.println("[警告] 命令行参数不是合法路径: " + t.getMessage());
            return;
        }

        double similarity = 0.0;

        try {
            // 只读命令行传入的两个文件
            String origText = FileIO.readText(origPath);
            String copyText = FileIO.readText(copyPath);

            // 先做 HTML 洗入 + 只保留中文，再做 n-gram 统计，峰值内存更低
            String cleanOrig = TextCleaner.clean(origText);
            String cleanCopy = TextCleaner.clean(copyText);

            // 及时释放原始字符串引用，便于 GC
            origText = null;
            copyText = null;

            similarity = Similarity.cosineSimilarity(cleanOrig, cleanCopy);

            cleanOrig = null;
            cleanCopy = null;

        } catch (Throwable t) {
            // 读文件/计算失败时，重复率按 0 处理，仍然要写答案文件
            System.err.println("[警告] 计算失败，重复率按 0 处理: " + t.getMessage());
            similarity = 0.0;
        }

        // 格式化为两位小数：0.00 ~ 1.00
        if (Double.isNaN(similarity) || Double.isInfinite(similarity)) {
            similarity = 0.0;
        }
        if (similarity < 0.0) {
            similarity = 0.0;
        }
        if (similarity > 1.0) {
            similarity = 1.0;
        }

        String answer = String.format(Locale.US, "%.2f", similarity);

        // 只写命令行传入的答案文件
        try {
            FileIO.writeText(answerPath, answer);
        } catch (Throwable t) {
            System.err.println("[警告] 答案文件写入失败: " + t.getMessage());
        }
    }
}
