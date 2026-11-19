package loop;

/**
 * 主循环：掌管时钟，并做跨层调度。全项目唯一同时认识 model 和 view 的角色。
 *
 * 字段：
 *   private final GameModel model   只用它的方法，不碰字段
 *   private final GamePanel panel   只用来 repaint()
 *   private final Timer timer       javax.swing.Timer，构造里建好
 *
 * 方法：
 *   GameLoop(GameModel model, GamePanel panel)   构造里建 Timer，initialDelay = delay
 *   void start() / void stop()                   启动 / 停住（GAME_OVER 时调）
 *   void pause() / void resume()                 RUNNING 时停，暂停后恢复
 *   void syncSpeed()                             从 model.delayMillis() 同步到 timer.setDelay
 *   private void tick()                          Timer 回调：model.step() + panel.repaint()，没有第三行
 *
 * 四条经验：
 *   1. 暂停用 timer.stop()，不要在 tick 里写 if (paused) return —— 后者时钟一直在空转。
 *   2. initialDelay 必须和 delay 设成同一个值，否则暂停恢复后会多等一段，看起来像卡了一下。
 *   3. 不要在这里存 boolean running / paused：状态归 model，再存一份就是两个真相，
 *      迟早出现"model 说暂停了，timer 还在转"。
 *   4. 用 Timer 而不是自己开线程：回调跑在 EDT 上，键盘、绘制、tick 同一条线程排队，
 *      所以全项目不需要任何锁。代价是 tick 里绝对不能有耗时操作。
 */
public class GameLoop {
}
