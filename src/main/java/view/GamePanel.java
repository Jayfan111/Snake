package view;

import model.Cell;
import model.GameModel;

import javax.swing.*;
import java.awt.*;

import static java.lang.String.valueOf;

/**
 * 画面：读 model，画出来。只读 —— 不判断规则，不调任何写方法。
 * <p>
 *   DEFAULT_CELL_SIZE  每格标称像素，只给 getPreferredSize() 用，运行时不读它
 *   BASE_FONT_SIZE     29px 是"格子正好 24px 时"的原始字号，文字缩放以它为基准
 *   COLOR_*            背景 / 蛇身 / 蛇头 / 食物 / 文字 / 棋盘 / 榜单
 *   model              唯一依赖
 * <p>
 *   构造             设背景、关掉本组件的输入法、让自己可聚焦（否则中文输入法会吞键）
 *   cellSize()       运行时现算 min(宽/列, 高/行)，每格像素
 *   textScale()      文字缩放因子 = cellSize/24，夹在 0.5~2.0，让字号跟着棋盘等比变
 *   originX/Y()      棋盘左上角像素，居中策略只存在于这两个方法里
 *   xToPixel/yToPixel   格子坐标 → 像素，所有绘制都走它们
 *   getPreferredSize 像素尺寸 = board 格子数 * DEFAULT_CELL_SIZE，pack() 靠它
 *   paintComponent   第一行 super，然后 drawBoard → drawFood → drawSnake → drawOverlay
 *   drawBoard        白色底板，顺带把"棋盘没撑满窗口"这件事显性化
 *   drawFood         food 为 null（满盘）时直接返回
 *   drawSnake        先画第 1..n 节，最后单独画蛇头，所以头永远在最上层
 *   drawOverlay      按 state 画：三个非运行态是半透明遮罩 + 文字，RUNNING 只画分数
 *   drawScoreTable   结算界面的历史前三，名字左对齐、分数右对齐，字号跟着 textScale 缩放
 *   drawHud          右上角分数，右对齐靠减 stringWidth
 * <p>
 * 经验：
 *   1. 不 extends JPanel 就永远收不到 paintComponent 调用，而且不报错。
 *   2. 坐标换算只写一处，手算过的地方一改 DEFAULT_CELL_SIZE 就会集体出错。
 *   3. 绘制顺序 = 图层顺序，覆盖层必须最后画。
 *   4. getPreferredSize 不能调 cellSize()：pack 之前 getWidth() 是 0，窗口会塌成一条标题栏。
 *   5. 窗口大小只能改"像素"，不能改 cols/rows —— 否则玩家能靠拉窗口躲开撞墙，model 也会依赖 awt。
 *   6. 换字号之后必须重新 getFontMetrics()，否则 stringWidth 还是旧字号的，居中和右对齐全偏。
 *   7. 被调用的方法若改过 Font/Color，返回前要恢复：整个 paintComponent 共用同一个 Graphics。
 */
public class GamePanel extends JPanel {
    private static final int DEFAULT_CELL_SIZE = 24;
    private static final int BASE_FONT_SIZE = 29;
    private static final Color
            COLOR_BG = Color.gray,
            COLOR_SNAKE = Color.green, COLOR_HEAD = new Color(20, 159, 25, 255),
            COLOR_FOOD = Color.red, COLOR_TEXT = Color.black,
            COLOR_BOARD = Color.white,
            COLOR_SCORE = Color.WHITE;   // 榜单专用：黑字压在 110 的遮罩上看不清
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
    private int cellSize(){
        return Math.min(getWidth() / model.board().cols(),getHeight() / model.board().rows());
    }
    // 文字跟着棋盘一起缩放：1.0 = 格子正好 DEFAULT_CELL_SIZE 时的原始比例。
    // 下限 0.5 防极端窗口算出 0 号字体，上限 2.0 防窗口拉太大时标题糊脸。
    private double textScale(){
        return Math.clamp(cellSize() / (double) DEFAULT_CELL_SIZE, 0.5, 2.0);
    }
    private int originX(){
        return (getWidth() - model.board().cols() * cellSize()) / 2;
    }
    private int originY(){
        return 0;
    }
    private int xToPixel(int x){
        return originX() + x * cellSize();
    }
    private int yToPixel(int y){
        return originY() + y * cellSize();
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
        g.fillRect(originX(),originY(),model.board().cols() * cellSize(),model.board().rows() * cellSize());
    }
    private void drawFood(Graphics g){
        Cell food = model.board().food();
        if (food == null) return;
        int x = xToPixel(food.x());
        int y = yToPixel(food.y());
        int cellSize = cellSize();
        g.setColor(COLOR_FOOD);
        g.fillOval(x + cellSize / 24, y + cellSize / 24, cellSize * 11 / 12, cellSize * 11 / 12);
    }
    private void drawSnake(Graphics g){
        var body = model.snake().body();
        int cellSize = cellSize();
        for(int i = 1; i < body.size(); i++) {
            Cell c = body.get(i);
            int x = xToPixel(c.x());
            int y = yToPixel(c.y());
            g.setColor(COLOR_SNAKE);
            g.fillOval(x + cellSize / 24, y + cellSize / 24, cellSize * 11 / 12, cellSize * 11 / 12);
        }
        Cell c = body.getFirst();
        int x = xToPixel(c.x());
        int y = yToPixel(c.y());
        g.setColor(COLOR_HEAD);
        g.fillOval(x + cellSize / 24, y + cellSize / 24, cellSize * 11 / 12, cellSize * 11 / 12);
    }
    private void drawOverlay(Graphics g){
        double s = textScale();
        int cy = getHeight() / 2;
        int hintGap = (int) Math.round(96 * s);
        g.setFont(new Font("微软雅黑",Font.BOLD,Math.max(11, (int) Math.round(BASE_FONT_SIZE * s))));
        FontMetrics fm = g.getFontMetrics();
        switch(model.state()){
            case READY -> {
                g.setColor(new Color(0,0,0, 110));
                g.fillRect(0,0,getWidth(),getHeight());
                String text = "按空格开始";
                g.setColor(COLOR_TEXT);
                g.drawString(text,(getWidth() - fm.stringWidth(text)) / 2,cy + hintGap);
            }
            case RUNNING -> {
                drawHud(g, s);
            }
            case PAUSED -> {
                drawHud(g, s);
                g.setColor(new Color(0,0,0, 110));
                g.fillRect(0,0,getWidth(),getHeight());
                String text = "已暂停";
                g.setColor(COLOR_TEXT);
                g.drawString(text,(getWidth() - fm.stringWidth(text)) / 2,cy + hintGap);
            }
            case GAME_OVER -> {
                g.setColor(new Color(0,0,0, 110));
                g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(COLOR_TEXT);
                String text = "游戏结束";
                g.drawString(text,(getWidth() - fm.stringWidth(text)) / 2,cy - (int) Math.round(60 * s));
                text = "最终得分：" + model.score();
                g.drawString(text,(getWidth() - fm.stringWidth(text)) / 2,cy - (int) Math.round(10 * s));
                drawScoreTable(g, s);
                text = "按R重新开始";
                g.drawString(text,(getWidth() - fm.stringWidth(text)) / 2,cy + (int) Math.round(150 * s));
            }
        }
    }
    private void drawHud(Graphics g, double s){
        g.setColor(COLOR_TEXT);
        String score = valueOf(model.score());
        g.drawString(score,getWidth() - (int) Math.round(60 * s),(int) Math.round(30 * s));
    }
    // 结算界面的历史前三：名字左对齐、分数右对齐，两列整体跟着窗口宽度走。
    // 自己存旧字体和旧颜色、结束时恢复 —— 调用方后面还要用原来的 fm 算 stringWidth，
    // 不恢复的话 "按R重新开始" 会拿新字体绘制、却按旧字体算宽度，文字就偏了。
    private void drawScoreTable(Graphics g, double s){
        var list = model.topScores();
        if (list.isEmpty()) return;   // 库坏了或第一次运行，没有榜就不占位置
        Font oldFont = g.getFont();
        Color oldColor = g.getColor();
        g.setFont(new Font("微软雅黑",Font.PLAIN,Math.max(10,(int) Math.round(16 * s))));
        FontMetrics fm = g.getFontMetrics();
        int rowGap = (int) Math.round(26 * s);          // 行距，必须和字号一起缩放
        int tableWidth = (int) Math.round(220 * s);     // 两列的总宽度
        int left = getWidth() / 2 - tableWidth / 2;     // 左列左边界
        int right = left + tableWidth;                  // 右列右边界
        int top = getHeight() / 2 + (int) Math.round(40 * s);
        g.setColor(COLOR_SCORE);
        for(int i = 0; i < list.size(); i++){
            var sc = list.get(i);
            int y = top + i * rowGap;
            g.drawString((i + 1) + ".  " + sc.name(),left,y);
            String points = valueOf(sc.points());
            // drawString 的 x 是文字左边缘，想要右边缘落在 right 上，就得减掉自身宽度
            g.drawString(points,right - fm.stringWidth(points),y);
        }
        g.setFont(oldFont);
        g.setColor(oldColor);
    }
}
