package model;

/**
 * 一条历史成绩，只读。
 *
 * 为什么只有 name / points，没有数据库的 id：
 *   id 是存储层的细节，唯一用途是同分时决定谁排前面（ORDER BY points DESC, id ASC）。
 *   如果把它写进这个 record，UI 和测试就会开始依赖它，以后换存储（文件、网络接口）就得跟着改。
 *   排行榜只读，不存在"更新某一条"，所以用不上 id。
 *
 * 为什么用 record：
 *   两个字段、不可变、还需要 equals —— record 一次给全。
 *   测试里可以直接比较 record 的相等性，
 *   普通类得手写 equals/hashCode 才能这么写。
 *
 * 使用：new Score(name, points)，参数顺序就是字段声明顺序。
 *   这里两个字段类型不同（String / int），写反会直接编译不过，不用担心静默错位。
 *
 * 经验：
 *   1. record 也不要加 setter。榜单是快照，读到什么就是什么；
 *      一旦可变，"画到一半被别处改了"这类问题就会出现，而且很难复现。
 *   2. 如果以后要显示"第几名"，那是结果集的行号，不是这个类的字段，别往这里加。
 */
public record Score(String name, int points) {
}
