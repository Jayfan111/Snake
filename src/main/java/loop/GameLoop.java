package loop;

/**
 * 主循环：掌管时钟，并做跨层调度。
 *
 * 【要写的字段】
 * javax.swing.Timer、GameModel、GamePanel（或者一个 repaint 的回调）。
 *
 * 【要写的方法】
 * 1. start / stop / pause / resume：
 *    暂停用 timer.stop()，不要靠 tick 里 if (paused) return。
 *    另外初始化时把 timer 的 initialDelay 设成和 delay 相同 ——
 *    否则暂停恢复后第一次触发会多等一个 initialDelay，看起来像卡了一下。
 * 2. tick()：Timer 的回调，内容应该只有两行 —— model.step()、panel.repaint()。
 *    没有第三行。
 * 3. 速度控制：吃到食物后 model 会变快，这里读出来再 timer.setDelay(...)，
 *    记得设一个下限（70ms 左右），否则后面快到没法玩。
 *
 * 【为什么用 Timer 而不是自己开线程】
 * Timer 的回调运行在 EDT 上，键盘事件、绘制、tick 全在同一条线程排队，
 * 所以整个项目不需要任何锁。代价是 tick 里绝对不能有耗时操作。
 *
 * 【不要写的】
 * 不要在这里判断游戏规则（撞没撞、吃没吃到）。
 * 不要直接改 model 的字段，只能调它的方法。
 *
 * 【方法清单（名字已定，照着填）】
 * GameLoop(GameModel model, GamePanel panel)   构造里建 Timer，initialDelay = delay
 * void start()                                 启动时钟
 * void stop()                                  停住（GAME_OVER 时调）
 * void pause()                                 RUNNING 时停
 * void resume()                                暂停后恢复
 * void syncSpeed()                             从 model.delayMillis() 同步到 timer.setDelay
 * private void tick()                          Timer 的回调：model.step() + panel.repaint()
 *
 * 【完成标志】
 * 蛇自己会爬；按暂停能停住，恢复时不顿帧。
 */
public class GameLoop {
}
