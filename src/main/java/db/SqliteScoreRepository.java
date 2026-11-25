package db;

import model.Score;
import model.ScoreRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 把成绩写进 SQLite 文件的实现。整个项目里唯一 import java.sql 的地方。
 *
 * 依赖方向：db 依赖 model（implements model.ScoreRepository），model 不认识 db。
 *   所以随时可以自查：model 包里 grep java.sql 必须是零命中。
 *
 * 构造函数收 Path 而不是 String：路径由调用方（Main）决定，这里只负责
 * "目录要存在"和"表要存在"两件事。
 *
 * 构造函数做三件事，顺序由依赖决定：拼连接串 → 建父目录 → 执行建表语句。
 *   拼 url 必须最先（后面 getConnection 要用它）；建目录必须在建表前，否则根本连不上。
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
 * 路径由 Main 用 Path.of(user.home, ".snake", "snake.db") 拼，落在用户目录而不是程序目录 ——
 * jpackage 装到 Program Files 后那里不可写，放程序目录会在打包版上直接起不来。
 * 也不要写相对路径：IDEA、双击 exe、跑测试的"当前目录"是三个地方，会变成三个互不相干的库。
 *
 * 两个方法要点：
 *   save —— INSERT INTO score(name, points) VALUES(?, ?)
 *           必须用 PreparedStatement 占位符，不要字符串拼接。
 *           拼接的问题不只是注入：名字里带一个单引号就直接语法错误。
 *   top  —— SELECT name, points FROM score ORDER BY points DESC, id ASC LIMIT ?
 *           id 不进 SELECT 的结果，它只在 ORDER BY 里起作用，所以 Score 里没有它。
 *           LIMIT 传超了也只返回实际条数，不会像内存版 subList 那样越界 —— 两边行为一致。
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
public class SqliteScoreRepository implements ScoreRepository {
    private final String url;

    public SqliteScoreRepository(Path dbFile){
        this.url = "jdbc:sqlite:" + dbFile.toAbsolutePath().toString().replace('\\','/');
        try{
            Files.createDirectories(dbFile.toAbsolutePath().getParent());
            try(Connection c = DriverManager.getConnection(url);
                PreparedStatement ps = c.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS score(" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "name TEXT NOT NULL," +
                                "points INTEGER NOT NULL," +
                                "created TEXT NOT NULL DEFAULT (datetime('now','localtime')))")){
                ps.executeUpdate();
            }
        } catch (IOException | SQLException e){
            throw new RuntimeException("初始化成绩库失败: " + url,e);
        }
    }

    @Override
    public void save(String name, int points) {
        try(Connection c = DriverManager.getConnection(url);
            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO score(name,points) VALUES (?,?)")){
                ps.setString(1,name);
                ps.setInt(2,points);
                ps.executeUpdate();
        }
        catch (SQLException e){
            throw(new RuntimeException("保存成绩失败", e));
        }
    }

    @Override
    public List<Score> top(int limit) {
        if(limit < 0) return List.of();
        List<Score> out = new ArrayList<>();
        try(Connection c = DriverManager.getConnection(url);
            PreparedStatement ps = c.prepareStatement(
                    "SELECT name, points FROM score ORDER BY points DESC, id ASC LIMIT ?")){
            ps.setInt(1,limit);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()) out.add(new Score(rs.getString("name"),rs.getInt("points")));
            }
        }catch(SQLException e){
            throw(new RuntimeException("读取排行榜失败", e));
        }
        return out;
    }
}
