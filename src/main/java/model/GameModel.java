package model;

import java.util.List;

/**
 * 一局游戏的全部状态与规则，项目的心脏。能脱离窗口单独跑、单独测。
 * <p>
 *   snake / board     蛇、棋盘（棋盘里含尺寸和食物）
 *   state             当前状态，初值 READY
 *   score             本局分数
 *   delayMillis       当前步长，GameLoop 每 tick 读它去 setDelay
 *   repo              成绩仓库，接口类型 —— 所以这个包里看不到 java.sql
 *   playerName        本局用哪个名字存档，由 Main 决定，这里只当一个字符串用
 *   topScores         历史前三的快照，die() 里查一次存下来，供面板每帧读
 * <p>
 * 常量：INITIAL_LENGTH=3、INITIAL_DELAY=180、MIN_DELAY=80、DELAY_STEP=5、POINTS_PER_FOOD=10。
 * 棋盘尺寸不在这里，由构造参数给（Main 决定）—— 小棋盘才容易测出撞墙、撞自己。
 * <p>
 *   构造(2 参)   委派给 4 参，仓库用内存版、名字用 "Player"。保留它是为了让
 *                28 个测试一行都不用改 —— "不传就是无存档"也就是默认行为。
 *   构造(3 参)   同上，只换仓库
 *   构造(4 参)   全量：蛇 → 棋盘 → 食物 → 初始状态 → 两个外部依赖
 *   查询    state / score / snake / board / delayMillis / topScores
 *   命令    start（READY→RUNNING）、togglePause（RUNNING⇄PAUSED）、
 *          requestDirection（非 RUNNING 直接丢）、reset、step
 *   私有 helper   eat / die / speedUp —— 只给 step 用
 * <p>
 * step() 五步，顺序不能变：
 *   1 状态不是 RUNNING 就返回
 *   2 取方向、算新头（先不写进蛇身）
 *   3 先判新头是不是食物
 *   4 越界或撞身体 → GAME_OVER 返回
 *   5 头入队；吃到就不去尾（加分、加速、换食物），没吃到去尾
 * <p>
 * 经验：
 *   1. 第 3 步必须在第 4 步前，否则不知道尾巴会不会让位：长蛇贴尾转弯会被误判死亡，
 *      已经撞死了还会加分。
 *   2. 这里不存 bestScore、不碰 Timer / awt / 文件，否则它就不能脱离环境做纯逻辑测试 ——
 *      那是它现在最大的价值。落盘走 ScoreRepository 接口，java.sql 只出现在 db 包。
 *   3. 时间不属于 model：step 只被别人叫，它自己不知道"多久"。
 *   4. 榜必须"查一次、存起来"。若在 paintComponent 里调 repo.top()，GAME_OVER 状态下
 *      每秒重绘约 60 次，就是每秒 60 条 SELECT 打在一个文件上，界面会持续卡。
 *   5. 查询要排在 save 之后，否则本局成绩不会出现在榜上 —— 这是 topScores 的顺序依赖。
 *   6. die() 里 catch RuntimeException：库坏了也只该是"没有榜"，不该让游戏崩在这一步。
 */
public class GameModel {
    private final Snake snake;
    private final Board board;
    private final ScoreRepository repo;
    private List<Score> topScores = List.of();
    private String playerName;
    private GameState state;
    private int score;
    private int delayMillis;
    private static final int INITIAL_LENGTH = 3;
    private static final int INITIAL_DELAY = 180;
    private static final int MIN_DELAY = 80;
    private static final int DELAY_STEP = 5;
    private static final int POINTS_PER_FOOD = 10;

    public GameModel(int cols, int rows){
        this(cols, rows, new InMemoryScoreRepository(), "Player");
    }
    public GameModel(int cols, int rows, ScoreRepository repo) {
        this(cols, rows, repo, "Player");
    }
    public GameModel(int cols, int rows, ScoreRepository repo, String playerName) {
        this.snake = new Snake(2, 2, INITIAL_LENGTH);
        this.board = new Board(cols, rows);
        board.spawnFood(snake.body());
        this.state = GameState.READY;
        this.delayMillis = INITIAL_DELAY;
        this.repo = repo;
        this.playerName = playerName;
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
    public List<Score> topScores(){
        return topScores;
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
        try {
            repo.save(playerName, score);
            topScores = repo.top(3);
        } catch (RuntimeException e){
            topScores = List.of();
        }
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
