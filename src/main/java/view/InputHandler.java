package view;

import model.Direction;
import model.GameModel;
import model.GameState;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


/**
 * 输入：把按键翻译成对 model 的意图。只翻译，不判断。
 *
 * 这个类不做任何规则判断 —— 反向能不能转、暂停时能不能动、结束了还理不理，
 * 全在 model 里。这里只负责一件事：哪个键，对应哪一句话。
 *
 * ---- 键绑定的机制：两张表，靠名字对上 ----
 *
 * Swing 不用 KeyListener，而是两张表：
 *
 *   InputMap    KeyStroke → 名字        "按了 ↑，就执行名叫 moveUp 的动作"
 *   ActionMap   名字 → Action           "moveUp 这个动作具体要做什么"
 *
 * 所以每绑一个键都得写两行，而且两行必须用同一个名字（本类统一用 String）：
 *
 *   inputMap.put(keyStroke, 名字);
 *   actionMap.put(名字, action);
 *
 * 名字是唯一的连接点，写错一个字母，那个键就静默失灵 —— 不报错、不提示、
 * 不抛异常。同一个名字可以挂多个键（↑ 和 W 都指向 "moveUp"），
 * 这就是 WASD 不必写第二套动作的原因。
 *
 * ★ 条件参数必须传：getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)。
 *   无参的 getInputMap() 等价于 WHEN_FOCUSED —— 必须这个组件自己持有焦点才响应。
 *   窗口一打开，焦点通常不在画布上，于是所有按键全部无反应，且没有任何报错。
 *   这是本类最容易踩、也最难查的坑：现象和"键名写错"一模一样。
 *
 * ---- 字段 ----
 *
 *   private final GameModel model   唯一依赖。方向键、空格、R 都只是对它发一条命令
 *
 * 不需要 GameLoop。暂停、继续、重来都只是改 model 的状态，step() 自己会挡住，
 * 时钟一直转就行；而且 GameLoop 的命令方法都是包级私有，view 包根本调不到。
 * （因此构造器那个 loop 参数和顶部 import loop.GameLoop 都可以删掉。）
 *
 * ---- 方法 ----
 *
 *   public InputHandler(JComponent panel, GameModel model)
 *       ★ 必须 public：Main 在 loop 包，跨包 new，包级私有编译不过。
 *       构造里依次调用 bindArrows / bindPause / bindRestart，把 panel 传下去即可，
 *       不必存成字段 —— 绑完这些键，之后没有任何地方还需要这个组件。
 *       所有绑定都在这里一次做完，别指望别处再补。
 *
 *   private void bind(JComponent c, KeyStroke ks, String name, Runnable body)
 *       通用绑定。内部 new 一个 AbstractAction 把 body 包起来，再往两张表各放一次。
 *
 *       为什么要 Action 这一层：InputMap 里的 value 只是个"查找键"，
 *       真正的动作放在 ActionMap 里，而 ActionMap 严格要求 Action 类型 ——
 *       所以一段 lambda 必须先被包装成 Action，AbstractAction 就是这个适配器。
 *       封装在这里的价值：AbstractAction 的样板只出现一次，以后加一个键
 *       只需要写"名字 + 一句话"。
 *
 *   private void bindArrows(JComponent c)
 *       ↑ ↓ ← → 加 WASD 共八个键，指向四个动作。同一方向的两个键绑同一个名字，
 *       所以只需要写四个 body，不是八个。
 *
 *   private void bindPause(JComponent c)
 *       空格 → model.togglePause()。不用自己判断"现在是运行还是暂停" ——
 *       togglePause 内部已经处理了，READY / GAME_OVER 状态下它什么都不做。
 *
 *   private void bindRestart(JComponent c)
 *       R → reset() 之后还必须 start()：reset 把状态退回 READY，
 *       不 start 就停在"按空格开始"的画面，看起来像这个键没生效。
 *
 * ---- 键位怎么写 ----
 *
 *   KeyStroke.getKeyStroke("UP")                  方向键，VK 常量名，全大写
 *   KeyStroke.getKeyStroke("SPACE")                空格，同样大写
 *   KeyStroke.getKeyStroke(KeyEvent.VK_R, 0)       字母键用这个最稳，需 import KeyEvent
 *
 * 字符串形式必须是 VK_ 常量名。大小写写错时它解析不出来会返回 null，
 * 然后 put(null, ...) 静默失效 —— 又一个"不报错但不工作"的位置。
 *
 * ---- 三条经验 ----
 *
 *   1. 用 InputMap / ActionMap 而不是 KeyListener：后者要求组件持有焦点，
 *      窗口一开得先点一下画布才能用，失焦复焦之后行为还很难预测。
 *   2. 这里出现的 if 只应该有一种：判断"按了哪个键"。任何形如"反向不能转"
 *      "死了不能动"的判断都必须留在 model —— 规则放 model 才能被测试覆盖，
 *      将来换成触屏手势也不用重写一遍。
 *   3. 每个键的动作都应该是一句话。如果某个 body 里开始出现三五行，
 *      说明那段逻辑该收回 model 变成一个方法，而不是在输入层展开。
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
