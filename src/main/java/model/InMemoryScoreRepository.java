package model;

import java.util.ArrayList;
import java.util.List;

/**
 * 成绩只存在内存里的实现，程序一关就没了。
 *
 * 它不是"临时凑数的假货"，有两处真实用途：
 *   1. 测试里传它 —— 测试不需要数据库文件、不需要驱动，也不会因为环境失败；
 *   2. 它就是"不存档模式"：Main 里换成它，游戏回到无记录状态。
 *      数据库文件万一损坏，也可以先降级到它跑起来。
 *
 * 为什么放在 main 而不是 test 目录：上面第 2 条是运行时行为，
 *   测试目录里的类不会打进产物，放那儿就没法在正式版本里用。
 *
 * 结构：一个 private final List<Score> scores 当内部存储，save 直接 add，
 *   top 走"判负 → 拷贝 → 降序排 → Math.min 截断 → List.copyOf"。
 *
 * 实现要点：
 *   save —— 往 list 里加一条，没有别的逻辑
 *   top  —— 四步：limit 为负直接返回空 → 拷贝一份 → 降序排 → 取前 limit 条
 *            截断必须用 Math.min(limit, size)：limit 大于总数时 subList 会抛
 *            IndexOutOfBoundsException，而这个异常在界面上表现为"游戏结束就崩"，
 *            原因却在一百行之外。limit == 0 交给 Math.min 处理就行，subList(0,0) 合法。
 *
 * 经验：
 *   1. 返回给外面的列表必须是新拷贝（List.copyOf）。直接 return 内部 list，
 *      外面一句 add 就能改坏你的数据，而调用点看起来毫无异常。
 *   2. List.sort 是稳定排序：分数相同的元素保持插入顺序，
 *      于是"同分先达到者在前"不用额外写比较器，天然成立。
 *      前提是排序前没打乱顺序 —— 调试用的 shuffle 之类不要留在代码里。
 *   3. 这个类没有任何 IO，所以可以直接在测试里 new，也可以在 EDT 上调用。
 *      SQLite 版就不该这么用，两边的使用边界要分清。
 *   4. 比较器写 (a,b) -> b.points() - a.points() 有溢出风险：分数差超过
 *      Integer.MAX_VALUE 会反向。更稳的是 Integer.compare(b.points(), a.points())。
 *      本项目分数到不了那个量级，所以先用减法也安全，但要知道它是靠这个前提成立的。
 *   5. 排序前不能打乱顺序（调试用的 shuffle 之类别留在代码里），
 *      否则第 2 条那个"稳定排序"的前提就没了，同分次序会随机变。
 */
public class InMemoryScoreRepository implements ScoreRepository{
    private final List<Score> scores = new ArrayList<>();

    @Override
    public void save(String name, int points) {
        scores.add(new Score(name, points));
    }

    @Override
    public List<Score> top(int limit) {
        if(limit < 0) return List.of();
        int n = Math.min(limit,scores.size());
        List<Score> temp = new ArrayList<>(scores);
        temp.sort((a,b) -> b.points() - a.points());
        return List.copyOf(temp.subList(0,n));
    }
}
