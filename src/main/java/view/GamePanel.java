package view;

/**
 * 画面：把 model 的状态画出来。
 *
 * 【先改一行声明】
 * 这个类要继承 JPanel（现在还没继承，你自己补上 extends JPanel）。
 *
 * 【要写的字段】
 * 一个 cellSize（每格多少像素）、model 的引用（只读用）。
 *
 * 【要写的方法】
 * 1. paintComponent(Graphics g)：第一行必须先 super.paintComponent(g) 再画。
 *    绘制顺序：背景 -> 网格线 -> 食物 -> 蛇身（从头到尾）-> 蛇头（换一种颜色）
 *    -> 覆盖层（暂停 / 结束的半透明遮罩 + 文字）。
 * 2. getPreferredSize()：返回窗口理想尺寸，这样 Main 里的 pack() 才有意义。
 * 3. 坐标换算：格子坐标转像素，写成一个私有小方法，别在每处手算
 *    ——后面改格子大小的时候你会感谢自己。
 *
 * 【不要写的】
 * 一行都不许调 model 的 setter 或改状态的方法 —— 拿 model 只为读。
 * 也不要在这里判断"撞墙了没有"、"该不该结束"，那是 model 的职责。
 * 这个类只做一件事：读，然后画。
 *
 * 【方法清单（名字已定，照着填）】
 * GamePanel(GameModel model)                     构造：存 model，设背景色、panel 尺寸来源
 * @Override protected void paintComponent(Graphics g)
 * @Override public Dimension getPreferredSize()   宽 = board.width() * cellSize + 边距
 * private void drawGrid(Graphics2D g)
 * private void drawFood(Graphics2D g)
 * private void drawSnake(Graphics2D g)
 * private void drawOverlay(Graphics2D g)          暂停 / 结束时的遮罩与文字
 * private int toPixel(int cell)                   格子坐标 -> 像素，画的时候统一用它
 *
 * 提示：paintComponent 里第一句转型一次 Graphics2D，后面四个 draw 都接收 Graphics2D，
 * 这样开抗锯齿、设圆角只写一遍。
 *
 * 【完成标志】
 * 把整个 view 包删掉，model 依然能编译、能测试。
 */
public class GamePanel {
}
