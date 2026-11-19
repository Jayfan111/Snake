package view;

/**
 * 输入：把按键翻译成对 model 的意图。只翻译，不判断。
 *
 * 字段：
 *   private final GameModel model   方向键 → requestDirection
 *   private final GameLoop loop     空格 / R 要启停时钟
 *
 * 方法：
 *   InputHandler(JComponent panel, GameModel model, GameLoop loop)   构造里完成全部绑定
 *   bindArrows(c)      ↑↓←→ 与 WASD → model.requestDirection
 *   bindPause(c)       空格 → loop.pause() / loop.resume()
 *   bindRestart(c)     R → model.reset() + loop.start()
 *   bind(c, keyStroke, action)   通用绑定：往 InputMap 放键，往 ActionMap 放动作
 *
 * 三条经验：
 *   1. 用 InputMap / ActionMap 配 WHEN_IN_FOCUSED_WINDOW，不要用 KeyListener ——
 *      JPanel 没拿到焦点就收不到键，这是新手最常卡的地方，失焦复焦后行为还很难预测。
 *   2. 反向不合法、结束或暂停时不能转向，这些判断都不在这里：规则放 model 才能被测试覆盖，
 *      将来换成触屏手势也不用重写第二遍。
 *   3. 四个方向键可以循环绑定，键位字符串和 Direction 一一对应，不必抄四遍。
 */
public class InputHandler {
}
