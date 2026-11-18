package view;

/**
 * 输入：把按键翻译成对 model 的意图。只翻译，不做判断。
 *
 * 【要写的字段】
 * model、loop，以及被绑定的 panel（键位要挂在它上面）。
 *
 * 【要写的方法】
 * 1. 绑定：用 InputMap / ActionMap 绑 WHEN_IN_FOCUSED_WINDOW。
 *    不要用 KeyListener —— JPanel 拿不到焦点就收不到按键，这是最常见的坑，
 *    而且失焦/复焦之后行为很难预测。
 * 2. 键位：方向键 / WASD 转弯，空格暂停，R 重开。
 * 3. 每个动作只做一件事：
 *    - 方向 -> model.requestDirection(dir)
 *    - 空格 -> loop.pause() / loop.resume()
 *    - R    -> model.reset() + loop.start()
 *
 * 【不要写的】
 * 不要在这里判断"反向不合法"、"游戏结束了不能转向"、"暂停时不能改方向"。
 * 这些规则属于 model —— 校验放在 model 里才能被单元测试覆盖；
 * 而且以后换成触屏滑动手势时，规则不用重写第二遍。
 *
 * 【方法清单（名字已定，照着填）】
 * InputHandler(JComponent panel, GameModel model, GameLoop loop)
 *                                              构造里完成全部绑定
 * private void bindArrows(JComponent c)        ↑↓←→ 和 WASD -> model.requestDirection
 * private void bindPause(JComponent c)         空格 -> loop.pause() / loop.resume()
 * private void bindRestart(JComponent c)       R -> model.reset() + loop.start()
 * private void bind(JComponent c, String keyStroke, Runnable action)
 *                                              通用绑定：往 InputMap 放键，往 ActionMap 放动作
 *
 * 提示：四个方向键可以直接循环绑定，不必写四遍一样的代码 ——
 *      键位字符串和 Direction 一一对应，循环里调 bind 就行。
 *
 * 【完成标志】
 * 这个类里没有任何一处 if 在判断游戏规则。
 */
public class InputHandler {
}
