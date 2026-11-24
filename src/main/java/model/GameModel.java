package model;

/**
 * 一局游戏的全部状态与规则，项目的心脏。能脱离窗口单独跑、单独测。
 *
 *   snake / board     蛇、棋盘（棋盘里含尺寸和食物）
 *   state             当前状态，初值 READY
 *   score             本局分数
 *   delayMillis       当前步长，GameLoop 每 tick 读它去 setDelay
 *
 * 常量：INITIAL_LENGTH=3、INITIAL_DELAY=150、MIN_DELAY=70、DELAY_STEP=5、POINTS_PER_FOOD=10。
 * 棋盘尺寸不在这里，由构造参数给（Main 决定）—— 小棋盘才容易测出撞墙、撞自己。
 *
 *   GameModel(width, height)   先 snake 再 board 再 spawnFood（食物要避开蛇身）
 *   查询    state / score / snake / board / delayMillis
 *   命令    start（READY→RUNNING）、togglePause（RUNNING⇄PAUSED）、
 *          requestDirection（非 RUNNING 直接丢）、reset、step
 *   私有 helper   isOutOfBounds / eat / die / speedUp —— 只给 step 用
 *
 * step() 五步，顺序不能变：
 *   1 状态不是 RUNNING 就返回
 *   2 取方向、算新头（先不写进蛇身）
 *   3 先判新头是不是食物
 *   4 越界或撞身体 → GAME_OVER 返回
 *   5 头入队；吃到就不去尾（加分、加速、换食物），没吃到去尾
 *
 * 经验：
 *   1. 第 3 步必须在第 4 步前，否则不知道尾巴会不会让位：长蛇贴尾转弯会被误判死亡，
 *      已经撞死了还会加分。
 *   2. 这里不存 bestScore、不碰 Timer / awt / 文件，否则它就不能脱离环境做纯逻辑测试 ——
 *      那是它现在最大的价值。
 *   3. 时间不属于 model：step 只被别人叫，它自己不知道"多久"。
 */
public class GameModel {
    private final Snake snake;
    private final Board board;
    private GameState state;
    private int score;
    private int delayMillis;
    private static final int INITIAL_LENGTH = 3;
    private static final int INITIAL_DELAY = 180;
    private static final int MIN_DELAY = 80;
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
    public void start(){
        if(state == GameState.READY) state = GameState.RUNNING;
    }
    public void togglePause() {
        if (state == GameState.RUNNING) state = GameState.PAUSED;
        else if (state == GameState.PAUSED) state = GameState.RUNNING;
    }
    public void requestDirection(Direction d){
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
    public void reset(){
        snake.reset();
        board.spawnFood(snake.body());
        state = GameState.READY;
        score = 0;
        delayMillis = INITIAL_DELAY;
    }
}
