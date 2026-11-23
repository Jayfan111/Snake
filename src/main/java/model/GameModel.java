package model;

/**
 * 一局游戏的全部状态与规则。整个项目的心脏。
 * 它最大的价值是：能在没有窗口的情况下跑起来、被验证。
 *
 * 字段：
 *   private final Snake snake      蛇
 *   private final Board board      棋盘（尺寸与食物都在里面）
 *   private GameState state        当前状态，初值 READY
 *   private int score              本局分数
 *   private int delayMillis        当前步长，GameLoop 读它去 setDelay
 *
 * 常量（一律 private static final，数字自己试，别照抄）：
 *   INITIAL_LENGTH  = 3      开局蛇长
 *   INITIAL_DELAY   = 150    开局每格毫秒
 *   MIN_DELAY       = 70     最快到这儿为止
 *   DELAY_STEP      = 5      每吃一个缩短多少毫秒 → 约 16 个食物到顶速
 *   POINTS_PER_FOOD = 10     每个食物多少分
 *   这里没有 COLS / ROWS：棋盘尺寸由构造参数传入，由 Main 决定。
 *   尺寸写死在 model 里，测试就只能跑 30×30，"蛇走到边界"要循环十几次才撞上；
 *   而 5×4 的小棋盘三行就能验完撞墙、撞自己、吃食物。
 *   常量只写 static 不写 final 的话，它就是个全局可写变量 —— 任何地方都能改，
 *   而且读代码的人分不清它是配置还是会变的状态。
 *
 * 方法：
 *   GameModel(int width, int height)                 尺寸由调用方给
 *                                                   内部顺序：new Snake → new Board → spawnFood
 *                                                   spawnFood 必须放在蛇建好之后，否则食物可能
 *                                                   落在蛇身上（它要靠 snake.body() 算空格）
 *   state() / score() / snake() / board() / delayMillis()   给 view 和 loop 读
 *   void start()                                     READY → RUNNING
 *   void togglePause()                               RUNNING ⇄ PAUSED
 *   void requestDirection(Direction d)               转发给 snake；非 RUNNING 时丢弃
 *   void step()                                      一个 tick 的全部逻辑，见下
 *   void reset()                                     全部字段回到初始状态（不 new 对象）
 *
 * 私有 helper（只给 step() 用，所以不对外开放 —— 一旦 public，view 就能调 die() 改状态）：
 *   isOutOfBounds(Cell c)   越界判定，内部就是 !board.isInside(c)
 *   eat()                   吃到后的连锁：加分、扣 delay、换食物、不去尾
 *   die()                   只做一件事：state = GAME_OVER
 *   speedUp()               只改 delayMillis 并夹住下限；真正生效是 GameLoop.syncSpeed()
 *                           名字叫"加速"其实只是记账，时间不属于 model
 *   拆的目的是让 step() 只表达"做什么"、细节各自去看。反过来，如果一个 helper 只有一行、
 *   别处也不复用、名字也没让代码更好读，那就别拆 —— 那只是多一次跳转。
 *
 * step() 的五步，顺序不能变：
 *   1. 状态不是 RUNNING → 直接 return
 *   2. 取一个方向（consumeDirection），算新头坐标，先不写进蛇身
 *   3. 判新头是不是食物 —— 必须在判死活之前
 *   4. 出界 或 撞身体 → 状态改 GAME_OVER 并 return
 *   5. 落子：头入队；吃到就不去尾（变长 + 加分 + 换食物 + 加速），没吃到就去尾
 *
 * 三条经验：
 *   1. 第 3 步在第 4 步之前，是因为"不知道吃没吃到，就不知道尾巴会不会让位"。
 *      顺序反了会出现两个 bug：长蛇贴着自己尾巴转弯被误判死亡、已经撞死却还加了分。
 *   2. 这里没有 bestScore 字段：最高分要跨局保留、迟早要落盘，一旦 model 开始读写文件，
 *      它就不能脱离环境做纯逻辑测试 —— 那正好是它现在最大的价值。
 *   3. 不持有 Timer、不 import awt / swing、不 repaint。时间不属于 model：
 *      step() 是被别人叫的，它自己不知道"多久"。
 */
public class GameModel {
    private final Snake snake;
    private final Board board;
    private GameState state;
    private int score;
    private int delayMillis;
    private static final int INITIAL_LENGTH = 3;
    private static final int INITIAL_DELAY = 150;
    private static final int MIN_DELAY = 70;
    private static final int DELAY_STEP = 5;
    private static final int POINTS_PER_FOOD = 10;

    public GameModel(int width, int height) {
        this.snake = new Snake(2, 2, INITIAL_LENGTH);
        this.board = new Board(width, height);
        board.spawnFood(snake.body());
        this.state = GameState.READY;
        this.delayMillis = INITIAL_DELAY;
    }
    public GameState state() {
        return state;
    }
    public int score(){
        return score;
    }
    public Snake snake(){
        return snake;
    }
    public Board board(){
        return board;
    }
    public int delayMillis(){
        return delayMillis;
    }
    void start(){
        if(state == GameState.READY) state = GameState.RUNNING;
    }
    void togglePause() {
        if (state == GameState.RUNNING) state = GameState.PAUSED;
        else if (state == GameState.PAUSED) state = GameState.RUNNING;
    }
    void requestDirection(Direction d){
        if(state != GameState.RUNNING) return;
        snake.request(d);
    }
    public void step(){
        if(state != GameState.RUNNING) return;
        Direction d = snake.consumeDirection();
        Cell newHead = new Cell(snake.head().x() + d.dx,snake.head().y() + d.dy);
        boolean ate = board.isFoodAt(newHead);
        if(!board.isInside(newHead) || snake.hitSelf(newHead, ate)){
            die();
            return;
        }
        snake.moveTo(newHead);
        if(!ate) snake.removeTail();
        else{
            eat();
        }
    }
    private void eat(){
        score += POINTS_PER_FOOD;
        speedUp();
        board.spawnFood(snake.body());
    }
    private void die(){
        state = GameState.GAME_OVER;
    }
    private void speedUp(){
        delayMillis = Math.max(delayMillis - DELAY_STEP, MIN_DELAY);
    }
    void reset(){
        snake.reset();
        board.spawnFood(snake.body());
        state = GameState.READY;
        score = 0;
        delayMillis = INITIAL_DELAY;
    }
}
