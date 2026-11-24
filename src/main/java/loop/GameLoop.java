package loop;

import model.GameModel;
import view.GamePanel;

import javax.swing.*;

/**
 * 主循环：每隔 delayMillis 推进一次 model，再请 view 重画。唯一同时认识 model 和 view 的类。
 *
 *   model / panel   只用它们的公开方法，不碰字段
 *   timer           javax.swing.Timer，构造器里建好
 *
 *   构造     new Timer(model.delayMillis(), e -> tick())，再 setInitialDelay(同一个值)
 *   start()  转调 timer.start()。注意：构造出来的 Timer 是静止的，不 start 永不触发，也不报错
 *   tick()   三行，顺序固定：setDelay（同步速度）→ model.step() → panel.repaint()
 *            反了不会报错，但画面永远慢一帧
 *
 * 经验：
 *   1. 要 javax.swing.Timer，不是 java.util.Timer —— 后者在后台线程触发，界面会偶发不刷新。
 *   2. 方法引用不能忽略参数：tick 无参就必须写 lambda e -> tick()。
 *   3. 不存 paused：状态归 model，step() 自己会挡住，时钟一直转就行。
 *   4. tick 排在 EDT 上，里面不能有耗时操作或 sleep —— 卡多久界面就冻多久。
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
