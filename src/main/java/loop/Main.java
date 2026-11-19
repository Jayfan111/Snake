package loop;

/**
 * 启动入口：建窗口、装配对象、启动。整个文件应该一眼读完（30 行以内）。
 *
 * 字段：无。哪天你想在这里加字段（比如"最高分"），说明它开始长成第二个 model。
 *
 * 方法：
 *   public static void main(String[] args)   唯一入口，用 SwingUtilities.invokeLater 包住装配
 *   private static void setup()              可选：把装配细节收在这儿，main 只剩两行
 *
 * 装配顺序（固定）：
 *   new GameModel → new GamePanel(model) → new GameLoop(model, panel)
 *   → new InputHandler(panel, model, loop) → add(panel)、pack()、
 *     setLocationRelativeTo(null)、setVisible(true) → loop.start()
 *
 * 两条经验：
 *   1. main 本身不在 EDT 上，直接 new JFrame 是隐患，必须包进 invokeLater。
 *   2. 这里不写任何游戏规则。它一旦长大就会变成第二个 model，以后没人记得该改哪边。
 */
public class Main {
}
