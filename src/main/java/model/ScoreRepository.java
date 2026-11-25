package model;

import java.util.List;

/**
 * 成绩存储的抽象层。model 只认识这个接口，不认识数据库。
 *
 * 存在意义（这是加数据库这件事真正的练习点）：
 *   GameModel 现在能脱离窗口、脱离文件单独跑，28 个测试不依赖任何环境。
 *   如果直接在 die() 里写 DriverManager.getConnection(...)，model 就依赖了驱动，
 *   没装数据库的机器上连 new GameModel(...) 都跑不起来，测试全红。
 *   接口把"用什么存"挡在 model 外面：正式运行传 SQLite 实现，测试传内存实现。
 *
 * 方法只有两个（接口不写 public，方法天然是 public abstract）：
 *
 *   save(String name, int points)   保存一局成绩。每局一条，不做去重，
 *                                   同一个人可以有很多条。name 不参与唯一性判断。
 *
 *   List<Score> top(int limit)      取分数最高的前 limit 条。
 *                                   排序约定：分数降序；同分时先达到的那条排前面。
 *                                     SQL 版靠 ORDER BY points DESC, id ASC；
 *                                     内存版靠"稳定排序 + 插入顺序"，
 *                                     这一点要记住，否则两个实现行为会不一致。
 *                                   边界：limit <= 0 返回空列表；
 *                                         总数不足 limit 时返回全部，不补 null。
 *                                   返回新列表，调用方改它不影响仓库内部数据。
 *
 * 铁律：方法签名里绝对不能出现 throws SQLException。
 *   一旦写了，这个文件就得 import java.sql，分层当场失效，而且编译照样通过 ——
 *   属于"错了但不报错"的那类问题，最难发现。
 *   数据库异常由实现类内部捕获，包装成 RuntimeException 再抛。
 *
 * 谁实现：
 *   InMemoryScoreRepository     内存版，测试用，也是"不存档"模式
 *   db.SqliteScoreRepository    落盘版，正式运行用
 *   换存储只改 Main 里装配的那一行，其它代码一行不动。
 */
public interface ScoreRepository {
    void save(String name, int points);
    List<Score> top(int limit);
}
