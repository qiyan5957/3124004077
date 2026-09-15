# EssayCheck —— 论文查重工具

> 基于中文 2-gram + 余弦相似度的论文查重程序，支持 UTF-8 / GBK / UTF-16 编码自动识别、HTML 标签清洗，附完整 JUnit 单元测试与性能分析报告。

![Java](https://img.shields.io/badge/Java-21-blue)
![JUnit](https://img.shields.io/badge/JUnit-4.13.2-green)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

---

## 项目简介

本工具用于检测两份中文论文的重复率：给定一份**原文**和一份**抄袭版论文**，程序会输出一个 `[0.00, 1.00]` 之间的浮点数表示重复率，结果保留两位小数。

**核心能力**：

- ✅ 中文 2-gram 切分，无需分词，对增删改鲁棒
- ✅ 余弦相似度，值域 [0,1]，不受文本长度差异影响
- ✅ 编码自适应：UTF-8（含 BOM）/ UTF-16 BE / UTF-16 LE / GBK
- ✅ HTML 标签清洗，兼容网页爬取数据
- ✅ 完整异常处理，主程序永不崩溃
- ✅ 28 个 JUnit 单元测试，核心模块覆盖率 80%+

---

## 项目结构

```
EssayCheck/
├── src/
│   └── com/essaycheck/
│       ├── Main.java              # 命令行入口，参数解析与流程串联
│       ├── FileIO.java            # 文件读取与写入，异常统一封装
│       ├── EncodingDetector.java  # 自动识别 UTF-8 / GBK / UTF-16 编码
│       ├── TextCleaner.java       # HTML 标签清洗 + 仅保留中文字符
│       └── Similarity.java        # 核心算法：2-gram + 余弦相似度
├── test/
│   └── com/essaycheck/
│       ├── TextCleanerTest.java
│       ├── EncodingDetectorTest.java
│       ├── SimilarityTest.java
│       ├── FileIOTest.java
│       └── ExceptionTest.java
├── 测试文本/                       # 样例文本
├── MANIFEST.MF
└── README.md
```

---

## 快速开始

### 环境要求

- JDK 17 或以上（推荐 21）
- 可选：IntelliJ IDEA Ultimate（用于查看覆盖率与 Profiler）

### 编译

```bash
cd EssayCheck
javac -encoding UTF-8 -d out src/com/essaycheck/*.java
```

### 打包

```bash
jar cfm main.jar MANIFEST.MF -C out .
```

### 运行

```bash
java -jar main.jar <原文文件> <抄袭版论文文件> <答案文件>
```

**示例**：

```bash
java -jar main.jar 测试文本/orig.txt 测试文本/orig_0.8_add.txt 测试文本/ans.txt
```

运行后 `ans.txt` 中会写入类似 `0.62` 的浮点数，即重复率。

---

## 算法说明

### 核心流程

```
原文/抄袭文本
   ↓
EncodingDetector.decode       —— 编码自适应解码
   ↓
TextCleaner.clean             —— HTML 清洗 + 仅保留中文
   ↓
Similarity.buildNgramVector   —— 2-gram 词频向量
   ↓
Similarity.cosineSimilarity   —— 余弦相似度计算
   ↓
[0, 1] 浮点重复率
```

### 为什么选 2-gram + 余弦相似度？

| 方案 | 优点 | 缺点 |
|------|------|------|
| 单字匹配 | 简单 | 丢失局部语义，容易误判 |
| 整句匹配 | 精确 | 一处小改就整体失效 |
| **2-gram** | **对增删改鲁棒** | 需要额外哈希空间 |
| 余弦相似度 | 不受长度影响 | 需要向量化 |

2-gram + 余弦相似度是中文短文本查重的常用组合，兼顾准确性和鲁棒性。

### 性能优化

- 用 `int` 编码二元组 `(c1 << 16) | c2` 替代 `String.substring`，减少临时对象创建
- `HashMap<Integer,Integer>` 减少哈希开销
- `TextCleaner.clean` 中文过滤由正则改为单次字符遍历
- 相同文本直接返回 1.0

详见博客：[性能改进章节链接]

---

## 单元测试

### 运行测试

**IDEA 中**：右键 `test` 目录 → `Run 'All Tests' with Coverage`

**命令行（JUnit 4）**：

```bash
java -cp "lib/*;out;test-out" org.junit.runner.JUnitCore \
    com.essaycheck.TextCleanerTest \
    com.essaycheck.EncodingDetectorTest \
    com.essaycheck.SimilarityTest \
    com.essaycheck.FileIOTest \
    com.essaycheck.ExceptionTest
```

### 覆盖率

| 类 | 行覆盖率 | 分支覆盖率 |
|----|----------|------------|
| Similarity | 72% | 82% |
| TextCleaner | 91% | 100% |
| EncodingDetector | 90%+ | 60%+ |
| FileIO | 85%+ | 33%+ |
| Main | — | — |
| **整体** | **约 70%+** | **约 70%+** |

> `Main` 作为命令行入口通过集成测试验证，`PerformanceTest` 为性能分析专用类，均不计入覆盖率统计。

---

## 异常处理

| 异常类型 | 触发场景 | 处理方式 |
|----------|----------|----------|
| `IllegalArgumentException` | `cosineSimilarity` 收到 `null` 文本 | 抛出明确异常，提示"待比较的文本不能为 null" |
| `IOException` | 文件不存在 / 读取失败 / 编码异常 | 统一封装，携带路径信息向上抛 |
| `UnsupportedEncodingException` | 字节流无法识别为 UTF-8/UTF-16/GBK | `decodeStrict` 抛异常；`decode` 宽容兜底 |

**设计要点**：严格版 `decodeStrict` 用于测试和诊断，宽容版 `decode` 用于生产路径，兼顾健壮性与可测试性。

---

## 性能分析

使用 IntelliJ IDEA Ultimate 自带的 Profiler（底层集成 Async Profiler）进行 CPU 采样分析。

**优化前后对比**：

| 指标 | 优化前 | 优化后 | 变化 |
|------|--------|--------|------|
| `buildNgramVector` 绝对耗时 | 122 ms | 88 ms | **-27.8%** |
| `buildNgramVector` CPU 占比 | 45.86% | 44.44% | -1.42% |

火焰图与详细分析见博客：[性能分析章节链接]

---

## 参考

- [JUnit 4 官方文档](https://junit.org/junit4/)
- [Java Flight Recorder](https://docs.oracle.com/javacomponents/jmc-5-5/jfr-runtime-guide/about.htm)
- [余弦相似度 - Wikipedia](https://zh.wikipedia.org/wiki/%E4%BD%99%E5%BC%A6%E7%9B%B8%E4%BC%BC%E6%80%A7)

---

##  License

MIT License

---

## 👤 作者

- **学号**：3124004077
- **GitHub**：[@qiyan5957](https://github.com/qiyan5957)
