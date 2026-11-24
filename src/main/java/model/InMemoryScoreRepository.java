package model;

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
 * 结构（自己写，import java.util.ArrayList / java.util.List）：
 *   一个 private final List<Score> scores = new ArrayList<>() 当内部存储，
 *   类声明 implements ScoreRepository，两个方法加 @Override。
 *
 * 实现要点：
 *   save —— 往 list 里加一条，没有别的逻辑
 *   top  —— 四步：limit <= 0 直接返回空 → 拷贝一份 → 降序排 → 取前 limit 条
 *            截断必须用 Math.min(limit, size)：limit 大于总数时 subList 会抛
 *            IndexOutOfBoundsException，而这个异常在界面上表现为"游戏结束就崩"，
 *            原因却在一百行之外。
 *
 * 经验：
 *   1. 返回给外面的列表必须是新拷贝（List.copyOf）。直接 return 内部 list，
 *      外面一句 add 就能改坏你的数据，而调用点看起来毫无异常。
 *   2. List.sort 是稳定排序：分数相同的元素保持插入顺序，
 *      于是"同分先达到者在前"不用额外写比较器，天然成立。
 *      前提是排序前没打乱顺序 —— 调试用的 shuffle 之类不要留在代码里。
 *   3. 这个类没有任何 IO，所以可以直接在测试里 new，也可以在 EDT 上调用。
 *      SQLite 版就不该这么用，两边的使用边界要分清。
 */
public class InMemoryScoreRepository {
}
