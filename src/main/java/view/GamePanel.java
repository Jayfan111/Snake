package view;

import model.Cell;
import model.GameModel;

import javax.swing.*;
import java.awt.*;

import static java.lang.String.valueOf;

/**
 * 画面：读 model，画出来。只读 —— 不判断规则，不调任何写方法。
 *
 *   CELL_SIZE        每格像素，改它等于缩放画面
 *   COLOR_*          背景 / 网格 / 蛇身 / 蛇头 / 食物 / 文字
 *   model            唯一依赖
 *
 *   构造             设背景、关掉本组件的输入法、让自己可聚焦（否则中文输入法会吞键）
 *   getPreferredSize 像素尺寸 = board 格子数 * CELL_SIZE，pack() 靠它
 *   paintComponent   第一行 super，然后 drawFood → drawSnake → drawOverlay
 *   toPixel(cell)    格子坐标 → 像素，所有绘制都走它
 *   drawFood         food 为 null（满盘）时直接返回
 *   drawSnake        先画第 1..n 节，最后单独画蛇头，所以头永远在最上层
 *   drawOverlay      按 state 画：三个非运行态是半透明遮罩 + 文字，RUNNING 只画分数
 *
 * 经验：
 *   1. 不 extends JPanel 就永远收不到 paintComponent 调用，而且不报错。
 *   2. 坐标换算只写一处，手算过的地方一改 CELL_SIZE 就会集体出错。
 *   3. 绘制顺序 = 图层顺序，覆盖层必须最后画。
 */
public class GamePanel extends JPanel {
    private static final int DEFAULT_CELL_SIZE = 24;
    private static final Color
            COLOR_BG = Color.gray, COLOR_GRID = Color.white,
            COLOR_SNAKE = Color.green, COLOR_HEAD = new Color(20, 159, 25, 255),
            COLOR_FOOD = Color.red, COLOR_TEXT = Color.black,
            COLOR_BOARD = Color.white;
    private final GameModel model;

    public GamePanel(GameModel model){
        this.model = model;
        setBackground(COLOR_BG);
        // 关输入法：默认按键会先交给输入法判断要不要消费，中文状态下方向键选候选词、
        // 空格上屏，InputMap 一条都收不到。下面 setFocusable 是它的前提 ——
        // 键事件只发给焦点组件，面板拿不到焦点，这行就没有作用对象。
        enableInputMethods(false);
        setFocusable(true);
    }
    private int cellsize(){
        return Math.min(getWidth() / model.board().cols(),getHeight() / model.board().rows());
    }
    private int originX(){
        return (getWidth() - model.board().cols() * cellsize()) / 2;
    }
    private int originY(){
        return 0;
    }
    private int xToPixel(int x){
        return originX() + x * cellsize();
    }
    private int yToPixel(int y){
        return originY() + y * cellsize();
    }
    @Override
    public Dimension getPreferredSize(){
        return new Dimension(model.board().cols() * DEFAULT_CELL_SIZE,model.board().rows() * DEFAULT_CELL_SIZE);
    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        drawBoard(g);
        drawFood(g);
        drawSnake(g);
        drawOverlay(g);
    }
    private void drawBoard(Graphics g){
        g.setColor(COLOR_BOARD);
        g.fillRect(originX(),originY(),model.board().cols() * cellsize(),model.board().rows() * cellsize());
    }
    private void drawFood(Graphics g){
        Cell food = model.board().food();
        if (food == null) return;
        int x = xToPixel(food.x());
        int y = yToPixel(food.y());
        int cellsize = cellsize();
        g.setColor(COLOR_FOOD);
        g.fillOval(x + cellsize / 24, y + cellsize / 24, cellsize * 11 / 12, cellsize * 11 / 12);
    }
    private void drawSnake(Graphics g){
        var body = model.snake().body();
        int cellsize = cellsize();
        for(int i = 1; i < body.size(); i++) {
            Cell c = body.get(i);
            int x = xToPixel(c.x());
            int y = yToPixel(c.y());
            g.setColor(COLOR_SNAKE);
            g.fillOval(x + cellsize / 24, y + cellsize / 24, cellsize * 11 / 12, cellsize * 11 / 12);
        }
        Cell c = body.getFirst();
        int x = xToPixel(c.x());
        int y = yToPixel(c.y());
        g.setColor(COLOR_HEAD);
        g.fillOval(x + cellsize / 24, y + cellsize / 24, cellsize * 11 / 12, cellsize * 11 / 12);
    }
    private void drawOverlay(Graphics g){
        g.setFont(new Font("微软雅黑",Font.BOLD,29));
        FontMetrics fm = g.getFontMetrics();
        switch(model.state()){
            case READY -> {
                g.setColor(new Color(0,0,0, 110));
                g.fillRect(0,0,getWidth(),getHeight());
                String text = "按空格开始";
                g.setColor(COLOR_TEXT);
                g.drawString(text,(getWidth() - fm.stringWidth(text)) / 2,getHeight() / 2 + 96);
            }
            case RUNNING -> {
                drawHud(g);
            }
            case PAUSED -> {
                drawHud(g);
                g.setColor(new Color(0,0,0, 110));
                g.fillRect(0,0,getWidth(),getHeight());
                String text = "已暂停";
                g.setColor(COLOR_TEXT);
                g.drawString(text,(getWidth() - fm.stringWidth(text)) / 2,getHeight() / 2 + 96);
            }
            case GAME_OVER -> {
                g.setColor(new Color(0,0,0, 110));
                g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(COLOR_TEXT);
                String text = "游戏结束";
                g.drawString(text,(getWidth() - fm.stringWidth(text)) / 2,getHeight() / 2);
                text = "最终得分：" + valueOf(model.score());
                g.drawString(text,(getWidth() - fm.stringWidth(text)) / 2,getHeight() / 2 + 48);
                text = "按R重新开始";
                g.drawString(text,(getWidth() - fm.stringWidth(text)) / 2,getHeight() / 2 + 96);
            }
        }
    }
    private void drawHud(Graphics g){
        g.setColor(COLOR_TEXT);
        String score = valueOf(model.score());
        g.drawString(score,getWidth() - 60,30);
    }
}
