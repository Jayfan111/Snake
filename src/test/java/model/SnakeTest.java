package model;

import java.util.List;
import java.util.Objects;

/**
 * Snake 的行为测试（不依赖 JUnit，用 main 跑）。
 *
 * 结构说明：
 *   1. 一个 case 方法 = 一个用例，方法名就是它验证的事情；
 *   2. 每个用例内部**新建一条蛇**，用例之间不共享任何状态；
 *   3. main 里顺序调用，任何一条失败都立刻抛异常并停在那里；
 *   4. 通过不打印，失败带出"期望 / 实际"，跑完只看一行总数。
 */
public class SnakeTest {

    private static int checks = 0;

    public static void main(String[] args) {
        caseConstructor();
        caseIntentVsFact();
        caseUTurn();
        caseReverseRejected();
        caseSelfHit();
        caseBodySnapshot();
        System.out.println("全部通过，共 " + checks + " 项检查");
    }

    // ------------------------------------------------------------ 用例

    /** 构造出来的蛇：长 3、朝右、头在最大 x 上、身体水平连续。 */
    private static void caseConstructor() {
        System.out.println("· 构造与形状");
        Snake s = new Snake(5, 7, 3);

        expect(3, s.length(), "长度");
        expect(Direction.RIGHT, s.direction(), "初始方向");
        expect(new Cell(7, 7), s.head(), "蛇头在最大 x 上");

        List<Cell> body = s.body();
        expect(3, body.size(), "body() 的长度与 length() 一致");
        for (int i = 0; i < body.size(); i++) {
            Cell c = body.get(i);
            expect(7, c.y(), "第 " + i + " 格的 y（整条蛇在同一行）");
            expect(7 - i, c.x(), "第 " + i + " 格的 x（头在最大 x，逐格递减）");
        }
    }

    /** 按键只记录意图；方向要等被消费之后才改变。 */
    private static void caseIntentVsFact() {
        System.out.println("· 意图与事实");
        Snake s = new Snake(5, 7, 3);

        s.request(Direction.DOWN);
        expect(Direction.RIGHT, s.direction(), "按下之后、消费之前，方向还没变");

        expect(Direction.DOWN, s.consumeDirection(), "消费得到的转向");
        expect(Direction.DOWN, s.direction(), "消费之后方向才变");

        expect(Direction.DOWN, s.consumeDirection(), "队列空时沿用当前方向");
    }

    /** 一次 tick 间隔里连按两下，两下都要生效——这是队列存在的理由。 */
    private static void caseUTurn() {
        System.out.println("· U 型掉头");
        Snake s = new Snake(5, 7, 3);

        s.request(Direction.DOWN);
        s.request(Direction.LEFT);

        expect(Direction.DOWN, s.consumeDirection(), "第一次消费");
        expect(Direction.LEFT, s.consumeDirection(), "第二次消费（相对 DOWN 是合法转向）");
    }

    /** 向右行进时直接按左，应被丢弃。 */
    private static void caseReverseRejected() {
        System.out.println("· 反向被拒绝");
        Snake s = new Snake(5, 7, 3);

        s.request(Direction.LEFT);
        expect(Direction.RIGHT, s.consumeDirection(), "反向请求被丢弃，仍走原方向");
    }

    /** 自撞的四种情况，以及"判定不应改变蛇"。 */
    private static void caseSelfHit() {
        System.out.println("· 自撞判定");
        Snake s = new Snake(5, 7, 3);            // 身体：(7,7) (6,7) (5,7)
        int len = s.length();

        expect(true,  s.hitSelf(new Cell(6, 7), false), "新头落在身体中段");
        expect(false, s.hitSelf(new Cell(5, 7), false), "新头落在尾格，这一 tick 尾巴会移走");
        expect(true,  s.hitSelf(new Cell(5, 7), true),  "新头落在尾格，但这一 tick 会变长");
        expect(false, s.hitSelf(new Cell(7, 9), false), "新头落在空格");

        expect(len, s.length(), "四次判定之后长度不变");
    }

    /** body() 是快照：拿到之后蛇继续走，旧列表不受影响；列表本身也不可写。 */
    private static void caseBodySnapshot() {
        System.out.println("· body() 快照");
        Snake s = new Snake(5, 7, 3);

        List<Cell> before = s.body();
        s.moveTo(new Cell(8, 7));
        s.removeTail();

        expect(3, s.length(), "走一步之后长度不变（头进尾出）");
        expect(3, before.size(), "之前拿到的列表长度不变");
        expect(new Cell(8, 7), s.head(), "头换成新格子");
        expect(true,  s.occupies(new Cell(6, 7)), "中段还在身上");
        expect(false, s.occupies(new Cell(5, 7)), "旧尾巴已经离开身体");

        boolean rejected = false;
        try {
            before.add(new Cell(0, 0));
        } catch (UnsupportedOperationException e) {
            rejected = true;
        }
        expect(true, rejected, "body() 返回的列表不可修改");
    }

    // ------------------------------------------------------------ 工具

    /** 相等就通过；不相等抛异常，栈会精确指到出问题的那一行调用处。 */
    private static void expect(Object expected, Object actual, String what) {
        if (Objects.equals(expected, actual)) {
            checks++;
            return;
        }
        throw new AssertionError(what + " —— 期望：" + expected + "，实际：" + actual);
    }
}
