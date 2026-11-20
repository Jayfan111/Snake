package view;

import model.GameModel;

import javax.swing.*;
import java.awt.*;

/**
 * 画面：把 model 的状态画出来。只做两件事 —— 读、然后画。
 *
 * 字段：
 *   private final GameModel model              只读，一个写方法都不许调
 *   private static final int CELL_SIZE = 24    每格像素；改这一个数就能缩放画面
 *   颜色常量（static final，不随对象变化）：COLOR_BG / COLOR_GRID / COLOR_SNAKE /
 *   COLOR_HEAD / COLOR_FOOD / COLOR_TEXT
 *
 * 方法：
 *   GamePanel(GameModel model)          存 model、设背景色
 *   paintComponent(Graphics g)          第一行必须 super.paintComponent(g)；
 *                                       顺序：背景 → 网格 → 食物 → 蛇身 → 蛇头 → 覆盖层
 *   getPreferredSize()                  宽高 = 格子数 * CELL_SIZE + 边距，Main 的 pack() 靠它
 *   drawGrid / drawFood / drawSnake / drawOverlay
 *   toPixel(int cell)                   格子坐标 → 像素，画的时候统一走它
 *
 * 三条经验：
 *   1. 一行都不许调 model 的写方法，也不许判断"撞墙了没有"。检测到结束就切场景是 loop 的事，
 *      这里出现一个判断规则的 if，分层就废了。
 *   2. 别忘了 extends JPanel —— 不继承的话 paintComponent 永远不会被调用，也不报错。
 *   3. 坐标换算只写一处。以后改 CELL_SIZE 或加边距时，手算过的那些地方会一起错。
 */
public class GamePanel extends JPanel {
    private static final int CELL_SIZE = 24;
    private static final Color
            COLOR_BG = Color.white, COLOR_GRID = Color.white,
            COLOR_SNAKE = Color.green, COLOR_HEAD = Color.blue,
            COLOR_FOOD = Color.yellow, COLOR_EXIT = Color.black;
    private final GameModel model;

    GamePanel(GameModel model){
        this.model = model;
        setBackground(COLOR_BG);
    }

    private int toPixel(int cell){
        return cell * CELL_SIZE;
    }
    @Override
    public Dimension getPreferredSize(){
        return new Dimension()
    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

    }
    private void drawFood(Graphics g){
        g.drawRect(model.board().food().x(),model.board().food().y(),
                CELL_SIZE,CELL_SIZE);
        g.setColor(COLOR_FOOD);
        g.fillRect(model.board().food().x(),model.board().food().y(),
                CELL_SIZE,CELL_SIZE);
    }
    private void drawSnake(){

    }
}
