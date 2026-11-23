package loop;

import model.GameModel;
import view.GamePanel;

import javax.swing.*;
import java.awt.*;

/**
 * 启动入口：建窗口、装配对象、启动。整个文件应该一眼读完（30 行以内）。
 *
 * 常量（棋盘尺寸属于"装配决定"，不属于游戏规则，所以放这儿）：
 *   private static final int COLS = 30      宽，单位是格子数
 *   private static final int ROWS = 30      高
 *   通过 new GameModel(COLS, ROWS) 传进去。model 自己不写死尺寸 ——
 *   写死了测试就只能跑 30×30 的棋盘，"蛇走到边界"要循环十几次才撞上。
 *
 * 字段：无。常量不算状态（static final 不随一局游戏变化）。
 * 哪天你想在这里加实例字段（比如"最高分"），说明它开始长成第二个 model。
 *
 * 方法：
 *   public static void main(String[] args)   唯一入口，用 SwingUtilities.invokeLater 包住装配
 *   private static void setup()              可选：把装配细节收在这儿，main 只剩两行
 *
 * 装配顺序（固定）：
 *   new GameModel(COLS, ROWS) → new GamePanel(model) → new GameLoop(model, panel)
 *   → new InputHandler(panel, model, loop) → add(panel)、pack()、
 *     setLocationRelativeTo(null)、setVisible(true) → loop.start()
 *
 * 三条经验：
 *   1. main 本身不在 EDT 上，直接 new JFrame 是隐患，必须包进 invokeLater。
 *   2. 这里不写任何游戏规则。它一旦长大就会变成第二个 model，以后没人记得该改哪边。
 *   3. 尺寸、初始速度这类"开局前就定好、整局不变"的值归这里；
 *      会随一局变化的值（分数、当前步长、状态）归 model。分界线就是"会不会变"。
 */
public class Main{
    private static final int COLS = 27;
    private static final int ROWS = 27;
    private static void setup(){
        JFrame frame = new JFrame("贪吃蛇");
        GameModel model = new GameModel(COLS,ROWS);
        GamePanel panel = new GamePanel(model);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GameLoop loop = new GameLoop(model,panel);
        frame.setVisible(true);
        loop.start();
    }
    public static void main(String[] args){
        EventQueue.invokeLater(Main::setup);
    }
}
