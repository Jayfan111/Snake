package model;

import db.SqliteScoreRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 成绩仓库的契约测试：同一组断言，对内存版和 SQLite 版各跑一遍。
 *
 * 为什么值得跑两遍：两个实现的行为必须一致，否则换实现时界面会悄悄变样
 * （比如内存版同分先到先排，SQLite 版却反过来）。同一组断言就是"一致"的定义。
 *
 * 两个实现写完之前，这个文件是编译不过的 —— 它写的是"规格"，而当时四个类只是空壳，
 * 编译器的每条报错就是待办清单。现在两边都实现了，跑一遍 42 项全绿。
 * 它的价值在后面：以后改排序、改 limit 语义、换第三种实现，这组断言会先叫。
 *
 * 断言依赖 Score 的 equals —— 这正是它该做成 record 的原因：
 * List.equals 会逐个比元素，普通类不写 equals 就永远不相等。
 *
 * 风格与 GameModelTest 一致：不依赖 JUnit，main 直接跑；断言不立刻抛，
 * 一条用例炸了不影响后面的用例继续跑。
 *
 * 跑法：右键这个文件 → 运行 main；或在 IDEA 里运行 Application 配置指向 model.ScoreRepositoryTest。
 */
public class ScoreRepositoryTest {

    private static int checks = 0;
    private static final List<String> failures = new ArrayList<>();
    private static int dbSeq = 0;

    public static void main(String[] args) {
        System.out.println("== 内存版 ==");
        runContract("内存版", InMemoryScoreRepository::new);

        System.out.println();
        System.out.println("== SQLite 版 ==");
        Path dir = createTempDir();
        try {
            // 每条用例一个新库文件，避免上一条的数据串进下一条
            runContract("SQLite 版", () -> new SqliteScoreRepository(dir.resolve("t" + (dbSeq++) + ".db")));
            run("SQLite 版 / 关掉重开数据还在", () -> casePersistsAcrossInstances(dir));
            run("SQLite 版 / 父目录不存在时自动创建", () -> caseCreatesParentDirs(dir));
        } finally {
            cleanUp(dir);
        }

        if (!failures.isEmpty()) {
            System.out.println();
            System.out.println("失败 " + failures.size() + " 项 / 共 " + checks + " 项检查：");
            for (String f : failures) {
                System.out.println("  ✗ " + f);
            }
            throw new AssertionError("有 " + failures.size() + " 项检查未通过，见上方列表");
        }
        System.out.println();
        System.out.println("全部通过，共 " + checks + " 项检查");
    }

    // ------------------------------------------------------------ 契约（两个实现都要过）

    /** 一组契约：换实现不改断言，这就是"接口存在的意义"的可执行版本。 */
    private static void runContract(String label, Supplier<ScoreRepository> factory) {
        run(label + " / 空仓库返回空列表", () -> caseEmpty(factory.get()));
        run(label + " / 存一条能读回来", () -> caseSaveThenTop(factory.get()));
        run(label + " / 按分数降序", () -> caseOrderByPoints(factory.get()));
        run(label + " / limit 只取前几条", () -> caseLimitCuts(factory.get()));
        run(label + " / 同分先到者在前", () -> caseTieKeepsInsertOrder(factory.get()));
        run(label + " / limit 边界", () -> caseLimitEdge(factory.get()));
        run(label + " / 返回的列表是拷贝", () -> caseTopReturnsCopy(factory.get()));
        run(label + " / 同名不去重", () -> caseSameNameAllowed(factory.get()));
    }

    /** 一条记录都没有时，top 要给空列表，不能是 null。 */
    private static void caseEmpty(ScoreRepository repo) {
        List<Score> top = repo.top(10);
        expect(false, top == null, "top 不能返回 null（调用方会直接 .size()）");
        if (top == null) return;
        expect(0, top.size(), "空仓库 top(10) 应返回 0 条");
    }

    /** 写进去什么，读出来就该是什么。 */
    private static void caseSaveThenTop(ScoreRepository repo) {
        repo.save("Alice", 120);

        List<Score> top = repo.top(10);
        expect(1, top.size(), "存一条后应有 1 条");
        if (top.isEmpty()) return;
        expect(new Score("Alice", 120), top.get(0), "名字和分数要原样读回来");
    }

    /** 分数高的在前，跟写入顺序无关。 */
    private static void caseOrderByPoints(ScoreRepository repo) {
        repo.save("A", 50);
        repo.save("B", 120);
        repo.save("C", 80);

        List<Score> top = repo.top(10);
        expect(3, top.size(), "三条都要在");
        if (top.size() < 3) return;
        expect("B", top.get(0).name(), "第一名：120");
        expect("C", top.get(1).name(), "第二名：80");
        expect("A", top.get(2).name(), "第三名：50");
    }

    /** limit 是"最多取几条"，不是"必须几条"。 */
    private static void caseLimitCuts(ScoreRepository repo) {
        repo.save("A", 50);
        repo.save("B", 120);
        repo.save("C", 80);

        List<Score> top = repo.top(2);
        expect(2, top.size(), "limit=2 只取两条");
        if (top.size() < 2) return;
        expect("B", top.get(0).name(), "取的是最高的两条之一");
        expect("C", top.get(1).name(), "取的是最高的两条之二");
    }

    /**
     * 同分时先达到的排前面。
     * 内存版靠 List.sort 的稳定性，SQLite 版靠 ORDER BY ... , id ASC ——
     * 两种做法不同，结果必须一致，这一条就是用来钉住它的。
     */
    private static void caseTieKeepsInsertOrder(ScoreRepository repo) {
        repo.save("Tom", 100);
        repo.save("Ann", 100);

        List<Score> top = repo.top(10);
        expect(2, top.size(), "同分也要各占一条");
        if (top.size() < 2) return;
        expect("Tom", top.get(0).name(), "同分：先达到的排前面");
        expect("Ann", top.get(1).name(), "同分：后达到的排后面");
    }

    /** 0、负数、超过总数，三种边界都不能抛异常。 */
    private static void caseLimitEdge(ScoreRepository repo) {
        repo.save("A", 10);

        expect(0, repo.top(0).size(), "limit=0 → 空列表");
        expect(0, repo.top(-1).size(), "limit<0 → 空列表，别抛 IndexOutOfBounds");
        expect(1, repo.top(100).size(), "limit 大于总数 → 返回全部，不补 null");
    }

    /** 调用方拿到的必须是拷贝；不可变列表也算安全。 */
    private static void caseTopReturnsCopy(ScoreRepository repo) {
        repo.save("A", 10);
        repo.save("B", 20);

        List<Score> first = repo.top(10);
        try {
            first.clear();
        } catch (UnsupportedOperationException ignored) {
            // 返回不可变列表同样安全，这本身就是一种保护
        }
        expect(2, repo.top(10).size(), "改返回值不该影响仓库内部数据");
    }

    /** 同一个人玩两局，就是两条记录，不做去重。 */
    private static void caseSameNameAllowed(ScoreRepository repo) {
        repo.save("Tom", 10);
        repo.save("Tom", 20);

        expect(2, repo.top(10).size(), "同名两局 → 两条，不要合并成一条");
    }

    // ------------------------------------------------------------ SQLite 专属

    /** 模拟"关掉程序再打开"：新建一个实例指向同一个文件。 */
    private static void casePersistsAcrossInstances(Path dir) {
        Path db = dir.resolve("persist.db");

        SqliteScoreRepository first = new SqliteScoreRepository(db);
        first.save("Alice", 120);
        first.save("Bob", 80);

        SqliteScoreRepository second = new SqliteScoreRepository(db);   // 相当于重开程序
        List<Score> top = second.top(10);

        expect(2, top.size(), "新实例应读到上一次写的两条");
        if (top.size() < 2) return;
        expect("Alice", top.get(0).name(), "最高的还是 Alice");
        expect(true, Files.exists(db), "数据库文件应该真的落在磁盘上");
    }

    /** SQLite 不会替你建父目录，所以构造函数里要先 createDirectories。 */
    private static void caseCreatesParentDirs(Path dir) {
        Path nested = dir.resolve("a").resolve("b").resolve("c").resolve("snake.db");

        new SqliteScoreRepository(nested);          // 不该抛异常
        expect(true, Files.exists(nested), "父目录不存在时，构造函数应自动建出来");
    }

    // ------------------------------------------------------------ 工具

    private static Path createTempDir() {
        try {
            Path dir = Files.createTempDirectory("snake-scores");
            System.out.println("（临时库目录：" + dir + "）");
            return dir;
        } catch (IOException e) {
            throw new UncheckedIOException("无法创建临时目录", e);
        }
    }

    /** 清理失败不影响测试结论，只提示一句，别让它盖住真正的失败原因。 */
    private static void cleanUp(Path dir) {
        try (var files = Files.list(dir)) {
            for (Path p : files.toList()) {
                Files.deleteIfExists(p);
            }
            Files.deleteIfExists(dir);
        } catch (IOException e) {
            System.out.println("（临时目录没删掉，手动清理即可：" + dir + "）");
        }
    }

    private static void run(String name, Runnable c) {
        int before = checks;
        try {
            c.run();
        } catch (RuntimeException | AssertionError e) {
            failures.add(name + " —— 抛出异常：" + e);
        }
        if (checks == before) {
            failures.add(name + " —— 一项检查都没跑到（可能很早就抛异常了）");
        }
    }

    /** 相等就通过；不等只记录，不打断后面的用例。 */
    private static void expect(Object expected, Object actual, String what) {
        checks++;
        if (Objects.equals(expected, actual)) {
            return;
        }
        failures.add(what + " —— 期望：" + expected + "，实际：" + actual);
    }
}
