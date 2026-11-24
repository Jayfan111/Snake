package db;

/**
 * 把成绩写进 SQLite 文件的实现。整个项目里唯一 import java.sql 的地方。
 *
 * 依赖方向：db 依赖 model（implements model.ScoreRepository），model 不认识 db。
 *   所以随时可以自查：model 包里 grep java.sql 必须是零命中。
 *
 * 构造函数收 Path 而不是 String：路径由调用方（Main）决定，这里只负责
 * "目录要存在"和"表要存在"两件事。
 *
 * 构造函数要做三件事，顺序不能反：建父目录 → 拼连接串 → 执行建表语句。
 * 这一步是样板、不是逻辑，但藏了两个坑：
 *   1. SQLite 不会替你创建父目录。~/.snake/ 不存在时报的是
 *      "unable to open database file"，一个字都没提目录，能卡很久。
 *      用 Files.createDirectories(dbFile.toAbsolutePath().getParent())。
 *   2. 不需要 Class.forName("org.sqlite.JDBC")。JDBC 4.0 起 classpath 里的驱动自动加载；
 *      真找不到驱动时抛的是 No suitable driver，那是 jar 没进 classpath，不是忘了 forName。
 *
 * 建表语句（IF NOT EXISTS 让重复启动也安全，不需要额外判断）：
 *
 *   CREATE TABLE IF NOT EXISTS score (
 *     id      INTEGER PRIMARY KEY AUTOINCREMENT,
 *     name    TEXT    NOT NULL,
 *     points  INTEGER NOT NULL,
 *     created TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
 *   );
 *
 * 连接串形如 jdbc:sqlite:C:/Users/86187/.snake/snake.db（正斜杠）。
 *
 * 两个方法要点：
 *   save —— INSERT INTO score(name, points) VALUES(?, ?)
 *           必须用 PreparedStatement 占位符，不要字符串拼接。
 *           拼接的问题不只是注入：名字里带一个单引号就直接语法错误。
 *   top  —— SELECT name, points FROM score ORDER BY points DESC, id ASC LIMIT ?
 *           id 不进 SELECT 的结果，它只在 ORDER BY 里起作用，所以 Score 里没有它。
 *
 * 连接管理：
 *   每个方法开头 try (Connection c = DriverManager.getConnection(url)) 开一个新连接，用完自动关。
 *   SQLite 开连接很便宜，桌面单机这样最省心。
 *   不要做成全局单例 Connection —— 它不是线程安全的，而且忘了关会锁住文件。
 *
 * 线程：
 *   这类调用不要放在绘制路径上。界面卡顿先测量（System.nanoTime 包住 save），
 *   真有 20ms 以上再上 SwingWorker；SQLite 单机插入通常只要 1~3ms。
 */
public class SqliteScoreRepository {
}
