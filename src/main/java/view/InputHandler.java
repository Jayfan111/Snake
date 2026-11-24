package view;

import model.Direction;
import model.GameModel;
import model.GameState;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


/**
 * 输入：把按键翻译成对 model 的一句话。只翻译，不判断。
 *
 *   model   唯一依赖；转向、暂停、重开都只是对它发一条命令
 *
 * 机制：两张表靠一个名字对上 —— InputMap 存 "KeyStroke → 名字"，
 * ActionMap 存 "名字 → Action"。名字写错就静默失灵；同一个名字可以挂多个键
 * （↑ 和 W 都指向 moveUp），所以 WASD 不需要第二套动作。
 *
 *   public InputHandler(GameModel, JComponent)   必须 public（Main 在 loop 包），
 *                                                构造里一次把三组键绑完
 *   bind(c, ks, name, body)                      通用绑定，AbstractAction 的样板只出现一次
 *   bindArrows                                   ↑↓←→ + WASD 共 8 个键 → 4 个动作名
 *   bindStartPause                               空格：READY→start，RUNNING/PAUSED→togglePause，
 *                                                GAME_OVER 无反应
 *   bindRestart                                  R → reset，退回 READY（要再按空格才开始）
 *
 * 绑定时的两个必写项：getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW) —— 无参版等于
 * WHEN_FOCUSED，窗口一开按键就全无反应；KeyStroke 的字符串名必须全大写（"UP"/"SPACE"），
 * 写错会返回 null，put(null, …) 静默失效。
 *
 * 经验：
 *   1. 用 InputMap / ActionMap 而不是 KeyListener：后者要求组件先拿到焦点，
 *      失焦复焦之后的行为还很难预测。
 *   2. 这里只允许"按了哪个键"这一种判断；"反向不能转""死了不能动"都属于 model。
 *   3. 每个键的动作应该只有一句话，写成三五行的 if 就说明该收回 model 变成一个方法。
 */
public class InputHandler {
    private final GameModel model;
    public InputHandler(GameModel model, JComponent panel){
        this.model = model;
        bindArrows(panel);
        bindStartPause(panel);
        bindRestart(panel);
    }
    private void bind(JComponent c,KeyStroke ks, String name, Runnable body){
        Action action = new AbstractAction(name){

            @Override
            public void actionPerformed(ActionEvent e) {
                body.run();
            }
        };
        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, name);
        c.getActionMap().put(name, action);
    }
    private void bindArrows(JComponent c){
        bind(c,KeyStroke.getKeyStroke("UP"),"moveUp",() -> model.requestDirection(Direction.UP));
        bind(c,KeyStroke.getKeyStroke(KeyEvent.VK_W,0),"moveUp",() -> model.requestDirection(Direction.UP));
        bind(c,KeyStroke.getKeyStroke("DOWN"),"moveDown",() -> model.requestDirection(Direction.DOWN));
        bind(c,KeyStroke.getKeyStroke(KeyEvent.VK_S,0),"moveDown",() -> model.requestDirection(Direction.DOWN));
        bind(c,KeyStroke.getKeyStroke("LEFT"),"moveLeft",() -> model.requestDirection(Direction.LEFT));
        bind(c,KeyStroke.getKeyStroke(KeyEvent.VK_A,0),"moveLeft",() -> model.requestDirection(Direction.LEFT));
        bind(c,KeyStroke.getKeyStroke("RIGHT"),"moveRight",() -> model.requestDirection(Direction.RIGHT));
        bind(c,KeyStroke.getKeyStroke(KeyEvent.VK_D,0),"moveRight",() -> model.requestDirection(Direction.RIGHT));
    }
    private void bindStartPause(JComponent c){
        bind(c, KeyStroke.getKeyStroke("SPACE"),"pause", () -> {
            if(model.state() == GameState.READY)
                model.start();
            else if(model.state() == GameState.RUNNING || model.state() == GameState.PAUSED)
                model.togglePause();
        });
    }
    private void bindRestart(JComponent c){
        bind(c,KeyStroke.getKeyStroke(KeyEvent.VK_R,0),"restart", model::reset);
    }
}
