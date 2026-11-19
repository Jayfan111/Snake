package model;

/**
 * 一局游戏的全部状态与规则。整个项目的心脏。
 * 它最大的价值是：能在没有窗口的情况下跑起来、被验证。
 *
 * 字段：
 *   private final Snake snake      蛇
 *   private final Board board      棋盘（尺寸与食物都在里面）
 *   private GameState state        当前状态，初值 READY
 *   private int score              本局分数
 *   private int delayMillis        当前步长，GameLoop 读它去 setDelay
 *   手感常量（数字自己试，别照抄）：COLS / ROWS、INITIAL_LENGTH、INITIAL_DELAY、
 *   MIN_DELAY、DELAY_STEP、POINTS_PER_FOOD
 *
 * 方法：
 *   GameModel(int width, int height)                 先建 snake 再建 board，最后 spawnFood
 *   state() / score() / snake() / board() / delayMillis()   给 view 和 loop 读
 *   void start()                                     READY → RUNNING
 *   void togglePause()                               RUNNING ⇄ PAUSED
 *   void requestDirection(Direction d)               转发给 snake；非 RUNNING 时丢弃
 *   void step()                                      一个 tick 的全部逻辑，见下
 *   void reset()                                     全部字段回到初始状态（不 new 对象）
 *   建议私有 helper：isOutOfBounds / eat / die / speedUp —— 否则 step() 会写成六十行
 *
 * step() 的五步，顺序不能变：
 *   1. 状态不是 RUNNING → 直接 return
 *   2. 取一个方向（consumeDirection），算新头坐标，先不写进蛇身
 *   3. 判新头是不是食物 —— 必须在判死活之前
 *   4. 出界 或 撞身体 → 状态改 GAME_OVER 并 return
 *   5. 落子：头入队；吃到就不去尾（变长 + 加分 + 换食物 + 加速），没吃到就去尾
 *
 * 三条经验：
 *   1. 第 3 步在第 4 步之前，是因为"不知道吃没吃到，就不知道尾巴会不会让位"。
 *      顺序反了会出现两个 bug：长蛇贴着自己尾巴转弯被误判死亡、已经撞死却还加了分。
 *   2. 这里没有 bestScore 字段：最高分要跨局保留、迟早要落盘，一旦 model 开始读写文件，
 *      它就不能脱离环境做纯逻辑测试 —— 那正好是它现在最大的价值。
 *   3. 不持有 Timer、不 import awt / swing、不 repaint。时间不属于 model：
 *      step() 是被别人叫的，它自己不知道"多久"。
 */
public class GameModel {
}
