package loop;

/**
 * 启动入口：建窗口、装配对象、启动。
 *
 * 【要写的步骤，顺序固定】
 * 1. 用 SwingUtilities.invokeLater 包住，保证界面创建都在 EDT 上。
 *    （main 方法本身不在 EDT 上，直接 new JFrame 是隐患。）
 * 2. new GameModel()；new GamePanel(model)。
 * 3. new GameLoop(model, panel)，把 Timer 的 initialDelay 和 delay 设成同一个值。
 * 4. new InputHandler(panel, model, loop) 完成键位绑定。
 * 5. frame.add(panel)、pack()、setLocationRelativeTo(null)、setVisible(true)。
 * 6. loop.start()。
 *
 * 【不要写的】
 * 不要有任何游戏规则或输赢判断。这个类应该短到一眼能读完 ——
 * 它一旦长大就会变成第二个 model，而且以后没人记得该改哪边。
 *
 * 【方法清单（名字已定，照着填）】
 * public static void main(String[] args)     唯一入口，里面用 invokeLater 包住装配
 * private static void setup()                可选：把 2~6 步都收在这里，
 *                                            main 就只剩两行
 *
 * 【完成标志】
 * 整个文件在一个屏幕内读完（30 行以内），并且能跑出一局完整的游戏。
 */
public class Main {
}
