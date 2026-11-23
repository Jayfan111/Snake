package loop;

import model.GameModel;
import view.GamePanel;

import javax.swing.*;

/**
 * 主循环：掌管时钟，并做跨层调度。全项目唯一同时认识 model 和 view 的角色。
 *
 * 这个类只做一件事：每隔 N 毫秒推进一次模型，再请界面重画。
 * 它不含游戏规则，也不判断输赢 —— 那些都在 model.step() 里。
 *
 * ---- 先认识 javax.swing.Timer ----
 *
 * Timer 是什么：一个"到点就调用你给的那个方法"的闹钟，由 Swing 自己管理。
 * 它不是一个线程，也不会在后台悄悄跑 —— 每次到点的回调都排在 EDT 上，
 * 和 paintComponent、键盘响应是同一条线程依次执行。所以这里不需要任何锁，
 * 但也意味着 tick 里绝不能有耗时操作：慢多少毫秒，界面就卡多少毫秒。
 *
 * 用它只需要记住四步：
 *   1. 构造    new Timer(间隔毫秒数, ActionListener)
 *              第一个参数是"每隔多久"，第二个是"到点做什么"。
 *   2. 首次延迟 setInitialDelay(毫秒)
 *              从 start() 到第一次触发要等多久。不设就默认等于间隔。
 *   3. 启动    start()
 *              ★ 构造出来的 Timer 是静止的，不 start() 永远不会触发，
 *                而且完全不报错。所以"写了 Timer 画面却一动不动"，
 *                九成是漏了这一句。
 *   4. 停止    stop()
 *              停掉之后还能再 start()，同一个实例可以反复启停。
 *
 * 不需要自己写循环：Timer 默认 repeats = true，到点触发一次、再等一次、
 * 再触发，直到你 stop() —— 循环是它替你转的。
 *
 * 参数形状对不上怎么接：ActionListener 的签名是 actionPerformed(ActionEvent)，带一个参数。
 * 有两条路，选一条：
 *   a) tick 声明成 private void tick(ActionEvent e) —— 方法引用 this::tick 直接可用，
 *      代价是那个 e 永远用不到，IDE 会把它灰掉。
 *   b) tick 保持无参，构造时用 lambda 转手：e -> tick() —— e 声明了但不使用。
 *
 * 坑在这里：方法引用没有"忽略参数"的能力。this::tick 要求 tick 的签名与接口一致，
 * 指向无参方法会报 cannot be applied to given types / 缺少参数 —— 看起来很像是
 * "tick 找不到"，其实只是参数个数不匹配。只有 lambda 能"收了不用"。
 * 两种写法选一种并保持一致，别构造器写 this::tick、tick 却写成无参的。
 *
 * 导入时看清楚：要的是 javax.swing.Timer，不是 java.util.Timer。
 * 后者在后台线程里触发，一旦在里面碰了 Swing 组件，症状是
 * "界面偶尔不刷新、也不报错" —— 本项目从头到尾不需要它。
 *
 * ---- 字段 ----
 *
 *   private final GameModel model   只用它的方法，不碰字段
 *   private final GamePanel panel   只用来 repaint()，这是本类唯一碰 view 的地方
 *   private final Timer timer       上面那个闹钟，在构造器里建好
 *
 * ---- 方法（只有这四个，别再加）----
 *
 *   GameLoop(GameModel model, GamePanel panel)
 *       存两个引用 → 建 timer（间隔取自 model.delayMillis()，到点调用 tick）
 *       → setInitialDelay 成同一个间隔。
 *       建之前先确认 GameModel 构造器已经把 delayMillis 初始化好了：
 *       如果它还是默认的 0，Timer 会连续不停地触发，界面直接卡死且不报错。
 *
 *   void start()
 *       一行，转调 timer.start()。装配完成、窗口可见之后由 Main 调一次。
 *
 *   private void tick()
 *       Timer 的回调，每次到点执行一次，只有三行，顺序不能换：
 *         1) 把 model.delayMillis() 同步给 timer.setDelay     ← 吃食物后变快，下一 tick 生效
 *         2) model.step()                                      ← 推进逻辑
 *         3) panel.repaint()                                   ← 请求重画
 *       第 1 行放最前，因为 setDelay 只影响"下一次之后"的间隔，当次不受影响。
 *       第 2、3 行反了不会报错，但画面会永远落后一帧（先 repaint 画的是旧状态），
 *       症状是蛇慢半拍、按了键要等一下才转向 —— 很难往"顺序"上想，所以记住它。
 *
 * ---- 为什么不要 pause / resume / syncSpeed ----
 *
 * 暂停状态已经存在 model.state() 里，而 step() 的第一步就是
 * "不是 RUNNING 就直接 return"。所以让时钟一直转、step 自己什么都不干，
 * 效果和停掉时钟完全一样，还少了两个方法和一个可能出错的同步点。
 *
 * 反过来写（InputHandler 里既调 model.togglePause() 又调 loop.pause()）
 * 就有了两个真相，漏掉一半就会出现"model 说暂停了，蛇还在走"。
 * 而且本类构造器是包级私有，InputHandler 在 view 包里，跨包根本调不到 pause()。
 * 删掉它，这个麻烦自动消失，InputHandler 也就不需要 loop 这个参数了。
 *
 * 速度变化同理：不需要重启定时器，tick 开头同步一次就够。
 *
 * ---- 四条经验 ----
 *
 *   1. 时钟从开局转到退出，中间不 stop。暂停和结束都由 model 的状态表达，
 *      空转的开销（每 150ms 调一个空方法）可以忽略。
 *   2. initialDelay 显式设成和间隔相同，别依赖"默认值恰好相等"这个巧合；
 *      以后一旦单独改过 setDelay，恢复后就会多等一段，看着像卡了一下。
 *   3. 这里不存 boolean running / paused：状态归 model，再存一份就是两个真相。
 *   4. 用 swing 的 Timer 而不是自己开线程或睡循环：回调天然在 EDT 上，
 *      键盘、绘制、tick 同一条线程排队，全项目零锁。代价是 tick 里既不能
 *      有耗时操作，也绝不能 Thread.sleep —— 节奏已经由间隔决定了，
 *      睡多久界面就冻多久。
 */
public class GameLoop {
    private final GameModel model;
    private final GamePanel panel;
    private final Timer timer;
    GameLoop(GameModel model, GamePanel panel){
        this.model = model;
        this.panel = panel;
        this.timer = new Timer(model.delayMillis(), e ->{
            tick();
        });
        timer.setInitialDelay(model.delayMillis());
    }
    void start(){
        timer.start();
    }
    private void tick(){
        timer.setDelay(model.delayMillis());
        model.step();
        panel.repaint();
    }
}
