package loop;

import db.SqliteScoreRepository;
import model.GameModel;
import view.GamePanel;
import view.InputHandler;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.Objects;

/**
 * 入口：建窗口、装配、启动。没有字段，也不写任何游戏规则。
 *
 *   COLS / ROWS       棋盘格子数。属于"装配决定"，通过 new GameModel 传进去
 *   MIN_CELL_SIZE     窗口最小尺寸的下限依据：每格至少 20px，再小就看不清了
 *   dbFile            成绩库落点 user.home/.snake/snake.db，绝对路径、可写、不在仓库里
 *   setup()           在 EDT 上按顺序装配：dbFile → model(含仓库) → panel → loop →
 *                     inputHandler → frame，然后 add / pack / 最小尺寸 / 居中 / 关闭行为
 *                     → setVisible → 请求焦点 → loop.start()
 *   main()            只负责把 setup 丢到 EDT 上
 *
 * 装配层是唯一知道"用哪个实现"的地方：把 new SqliteScoreRepository(dbFile) 换成
 * new InMemoryScoreRepository() 就回到不存档模式，model 和 view 一行都不用改。
 *
 * 经验：
 *   1. main 不在 EDT 上，new JFrame 必须包进 invokeLater。
 *   2. requestFocusInWindow 要放在 setVisible 之后，之前调会被直接丢掉。
 *   3. 这里只放"整局不变"的值（尺寸、窗口）；会变的（分数、状态、速度）都归 model。
 *   4. 数据库路径不能写相对路径，也不能放程序目录：前者会因启动方式不同变成三个库，
 *      后者在 jpackage 装到 Program Files 后不可写。用 user.home 同时避开这两点。
 *   5. getInsets() 要放在 pack() 之后取，pack 之前拿到的是 0，最小尺寸会把标题栏算漏。
 *   6. 仓库必须在 model 之前建好，因为它是构造参数 —— 顺序即依赖。
 */
public class Main{
    private static final int COLS = 27;
    private static final int ROWS = 27;
    private static final int MIN_CELL_SIZE = 20;
    private static void setup(){
        JFrame frame = new JFrame("贪吃蛇");
        Path dbFile = Path.of(System.getProperty("user.home"), ".snake", "snake.db");
        GameModel model = new GameModel(COLS, ROWS, new SqliteScoreRepository(dbFile));
        GamePanel panel = new GamePanel(model);
        GameLoop loop = new GameLoop(model,panel);
        InputHandler inputHandler = new InputHandler(model, panel);
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/icon/snake-96.png")));
        frame.setIconImage(icon.getImage());
        frame.add(panel);
        frame.pack();
        Insets in = frame.getInsets();
        frame.setMinimumSize(new Dimension(
                COLS * MIN_CELL_SIZE + in.left + in.right,ROWS * MIN_CELL_SIZE + in.bottom + in.top));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        // 焦点交给画布：键事件只发给焦点组件，面板不当焦点持有者，按键到不了 InputMap。
        // 必须在 setVisible 之后调，窗口还没显示时这个请求会被直接丢掉。
        panel.requestFocusInWindow();
        loop.start();
    }
    public static void main(String[] args){
        EventQueue.invokeLater(Main::setup);
    }
}
