package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * GameModel 的行为测试（不依赖 JUnit，用 main 跑）。
 *
 * 和 SnakeTest 的关系：那一份测的是"蛇自己"，这一份测的是"一局游戏"——
 * 状态流转、tick 的五步、吃与死的连锁反应。两份都跑，才覆盖到 model 包。
 *
 * 两个设计选择：
 *   1. 断言**不立刻抛**，先把失败收集起来，跑完一次性列出来。
 *      因为 GameModel 是多米诺：初始状态错了，后面每条用例都会跟着红，
 *      立刻抛的话你只能一个个修、一遍遍重跑。
 *   2. 每条用例套在 try/catch 里，一条炸了不影响其余用例继续跑。
 *
 * 摆食物的办法：Board.spawnFood(occupied) 是 public 的，把"除目标格以外的所有格子"
 * 当成已占用传进去，空格就只剩那一格，食物必然落在它上面。
 * 不需要给生产代码开后门，也不需要反射。
 */
public class GameModelTest {

    private static int checks = 0;
    private static final List<String> failures = new ArrayList<>();

    public static void main(String[] args) {
        run("构造与初始状态",   GameModelTest::caseInitialState);
        run("开始与暂停",       GameModelTest::caseStartAndPause);
        run("走一步",           GameModelTest::caseStepMoves);
        run("非 RUNNING 不动",  GameModelTest::caseNotRunningStaysStill);
        run("撞墙结束",         GameModelTest::caseWallEndsGame);
        run("吃到食物",         GameModelTest::caseEatGrowsAndScores);
        run("加速下限",         GameModelTest::caseSpeedFloor);
        run("撞自己结束",       GameModelTest::caseSelfHitEndsGame);
        run("重开一局",         GameModelTest::caseReset);
        run("结束后按空格",     GameModelTest::casePauseDoesNotResurrect);

        if (!failures.isEmpty()) {
            System.out.println();
            System.out.println("失败 " + failures.size() + " 项 / 共 " + checks + " 项检查：");
            for (String f : failures) {
                System.out.println("  ✗ " + f);
            }
            throw new AssertionError("有 " + failures.size() + " 项检查未通过，见上方列表");
        }
        System.out.println("全部通过，共 " + checks + " 项检查");
    }

    // ------------------------------------------------------------ 用例

    /** 新开一局：状态、分数、步长、蛇、食物，五个初始值都要对。 */
    private static void caseInitialState() {
        GameModel m = new GameModel(8, 5);

        expect(8, m.board().width(), "棋盘宽 = 构造参数");
        expect(5, m.board().height(), "棋盘高 = 构造参数");
        expect(GameState.READY, m.state(), "构造后状态应为 READY");
        expect(0, m.score(), "初始分数");
        expect(150, m.delayMillis(), "初始步长应为 INITIAL_DELAY");
        expect(3, m.snake().length(), "初始蛇长");
        expect(new Cell(4, 2), m.snake().head(), "蛇头位置（startX=2、长 3、朝右 → x=4）");
        expect(true, m.board().food() != null, "开局就应该有食物");
        expect(false, m.snake().occupies(m.board().food()), "食物不能落在蛇身上");
    }

    /** 空格键在 RUNNING 与 PAUSED 之间来回。 */
    private static void caseStartAndPause() {
        GameModel m = new GameModel(8, 5);

        m.start();
        expect(GameState.RUNNING, m.state(), "start() 之后进入 RUNNING");

        m.togglePause();
        expect(GameState.PAUSED, m.state(), "RUNNING 时切换 → PAUSED");

        m.togglePause();
        expect(GameState.RUNNING, m.state(), "PAUSED 时再切换 → RUNNING");
    }

    /** 走一步：头前进一格，长度和分数不变。 */
    private static void caseStepMoves() {
        GameModel m = new GameModel(8, 5);
        placeFoodAt(m.board(), new Cell(0, 0));      // 食物挪到远处，免得顺手吃到
        m.start();

        Cell before = m.snake().head();
        m.step();

        expect(new Cell(before.x() + 1, before.y()), m.snake().head(), "向右走一格");
        expect(3, m.snake().length(), "没吃到，长度不变");
        expect(0, m.score(), "没吃到，分数不变");
        expect(GameState.RUNNING, m.state(), "还在进行中");
    }

    /** READY 和 PAUSED 时，时钟来了也不该动；暂停时按的方向应被丢弃。 */
    private static void caseNotRunningStaysStill() {
        GameModel m = new GameModel(8, 5);
        placeFoodAt(m.board(), new Cell(0, 0));
        Cell h = m.snake().head();

        m.step();
        expect(h, m.snake().head(), "READY 状态 step 不动");

        m.start();
        m.togglePause();
        m.requestDirection(Direction.UP);            // 暂停中按上，应被模型丢弃
        m.step();
        expect(h, m.snake().head(), "PAUSED 状态 step 不动");

        m.togglePause();
        m.step();
        expect(new Cell(h.x() + 1, h.y()), m.snake().head(),
                "恢复后仍向右 —— 说明暂停时按的那个上没进队列");
    }

    /** 撞墙：状态转 GAME_OVER，不加分，之后时钟再响也不动。 */
    private static void caseWallEndsGame() {
        GameModel m = new GameModel(5, 3);           // 宽 5，蛇头正好在 x=4
        placeFoodAt(m.board(), new Cell(4, 0));      // 食物挪开，别干扰
        m.start();

        expect(new Cell(4, 2), m.snake().head(), "起手贴右墙");
        m.step();

        expect(GameState.GAME_OVER, m.state(), "向右一步出界 → GAME_OVER");
        expect(0, m.score(), "撞死不加分");

        Cell dead = m.snake().head();
        m.step();
        expect(dead, m.snake().head(), "结束后 step 不再移动");
    }

    /** 吃到食物：变长、加分、加速、换食物，且新食物不落在蛇身上。 */
    private static void caseEatGrowsAndScores() {
        GameModel m = new GameModel(8, 5);
        m.start();
        placeFoodAt(m.board(), new Cell(5, 2));      // 正前方

        m.step();

        expect(new Cell(5, 2), m.snake().head(), "头进到原来食物那一格");
        expect(4, m.snake().length(), "变长一格");
        expect(10, m.score(), "加 POINTS_PER_FOOD");
        expect(145, m.delayMillis(), "加速 DELAY_STEP（150 - 5）");

        Cell food = m.board().food();
        expect(true, food != null, "吃完还有下一个食物");
        expect(false, m.snake().occupies(food), "新食物不能刷在蛇身上");
    }

    /** 一路吃 20 个：前 16 个线性变快，之后卡在 MIN_DELAY 不再下降。 */
    private static void caseSpeedFloor() {
        GameModel m = new GameModel(30, 5);
        m.start();

        int[] d = new int[20];
        for (int i = 0; i < d.length; i++) {
            Cell head = m.snake().head();
            placeFoodAt(m.board(), new Cell(head.x() + 1, head.y()));
            m.step();
            d[i] = m.delayMillis();
        }

        expect(145, d[0], "第 1 个食物之后的步长");
        expect(70, d[15], "第 16 个食物之后降到 MIN_DELAY");
        expect(70, d[16], "第 17 个：不低于下限");
        expect(70, d[19], "第 20 个：仍不低于下限");
        expect(GameState.RUNNING, m.state(), "一直在吃，不该死");
        expect(200, m.score(), "20 个食物 → 200 分");
    }

    /**
     * 撞自己：吃到长 5 后走一个 U 型，第三步回到身体中段。
     * 顺便验证"尾巴让位"不是万能的 —— 让位只对尾格有效，中段照样撞。
     */
    private static void caseSelfHitEndsGame() {
        GameModel m = new GameModel(8, 8);
        m.start();

        placeFoodAt(m.board(), new Cell(5, 2));
        m.step();                                    // 吃到第 1 个，长 4，头 (5,2)
        placeFoodAt(m.board(), new Cell(6, 2));
        m.step();                                    // 吃到第 2 个，长 5，头 (6,2)
        placeFoodAt(m.board(), new Cell(0, 0));      // 食物挪开，别干扰转弯

        m.requestDirection(Direction.UP);
        m.step();                                    // 头 (6,1)
        m.requestDirection(Direction.LEFT);
        m.step();                                    // 头 (5,1)
        expect(GameState.RUNNING, m.state(), "转两次弯还没死");

        m.requestDirection(Direction.DOWN);
        m.step();                                    // 头 (5,2) —— 身体中段

        expect(GameState.GAME_OVER, m.state(), "撞进自己身体 → GAME_OVER");
        expect(20, m.score(), "死前吃到的分要保留");
    }

    /** 重开：状态、分数、步长、蛇、食物全部回到开局。 */
    private static void caseReset() {
        GameModel m = new GameModel(5, 3);
        m.start();
        m.step();
        expect(GameState.GAME_OVER, m.state(), "先撞死一局");

        m.reset();

        expect(GameState.READY, m.state(), "reset 之后回到 READY");
        expect(0, m.score(), "分数清零");
        expect(150, m.delayMillis(), "步长复位（上一局的极速不能带过来）");
        expect(3, m.snake().length(), "蛇回到初始长度");
        expect(new Cell(4, 2), m.snake().head(), "蛇头回到起始位置");
        expect(false, m.snake().occupies(m.board().food()), "新食物不在蛇身上");
    }

    /** 结束画面上按空格，不应该把蛇"救活"。 */
    private static void casePauseDoesNotResurrect() {
        GameModel m = new GameModel(5, 3);
        m.start();
        m.step();
        expect(GameState.GAME_OVER, m.state(), "先撞死");

        m.togglePause();
        expect(GameState.GAME_OVER, m.state(), "GAME_OVER 按空格仍应是 GAME_OVER");
    }

    // ------------------------------------------------------------ 工具

    /** 把食物精确放到 target：把"除 target 外的所有格子"当作已占用传进去。 */
    private static void placeFoodAt(Board board, Cell target) {
        List<Cell> occupied = new ArrayList<>();
        for (int y = 0; y < board.height(); y++) {
            for (int x = 0; x < board.width(); x++) {
                Cell c = new Cell(x, y);
                if (!c.equals(target)) occupied.add(c);
            }
        }
        board.spawnFood(occupied);
    }

    private static void run(String name, Runnable c) {
        int before = checks;
        try {
            c.run();
        } catch (RuntimeException | AssertionError e) {
            failures.add(name + " —— 抛出异常：" + e);
        }
        if (checks == before) {
            failures.add(name + " —— 一项检查都没跑到（可能很早就抛异常了）");
        }
    }

    /** 相等就通过；不等只记录，不打断后面的用例。 */
    private static void expect(Object expected, Object actual, String what) {
        if (Objects.equals(expected, actual)) {
            checks++;
            return;
        }
        checks++;
        failures.add(what + " —— 期望：" + expected + "，实际：" + actual);
    }
}
