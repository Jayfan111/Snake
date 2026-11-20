package model;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;

/**
 * 蛇：身体队列 + 待处理方向。
 *
 * 字段：
 *   private final ArrayDeque<Cell> body         蛇身，队首是头、队尾是尾
 *   private final ArrayDeque<Direction> pending  已接受、还没被 tick 消费的方向
 *   private Direction lastMove                  上一次实际生效的方向
 *   private static final int MAX_PENDING = 2    待处理上限，约等于一次 U 型掉头
 *
 * 方法：
 *   Snake(int startX, int startY, int initialLength)  造一条水平朝右的蛇（头在最大 x 上）
 *   Cell head() / int length() / Direction direction()  读状态；length 就是 body.size()
 *   Direction consumeDirection()   每 tick 一次：取队首方向并写回 lastMove，队列空则沿用
 *   void request(Direction d)      入队前做反向校验与容量校验；可能无声地拒绝
 *   void moveTo(Cell newHead) / void removeTail()  落子的两个原子动作，吃没吃到由 GameModel 决定
 *   boolean occupies(Cell c)       这个格子在不在身上
 *   boolean hitSelf(Cell newHead, boolean willGrow)  自撞判定；willGrow 为 false 时尾格让位
 *   List<Cell> body()              给 view 的只读快照
 *
 * 五条经验：
 *   1. body 是 final，但内容仍然可变：final 锁的是"永远指向同一个队列"，
 *      不锁 offerFirst / pollLast / clear。所以 reset() 用 clear() 再重新塞，不换新队列。
 *   2. 类里没有 length 字段：长度是 body.size() 的派生值。缓存它只会多出
 *      吃食物 / 去尾 / reset 三处必须同步的写入点，漏一处就是"吃了食物没变长"。
 *   3. pending 和 lastMove 必须分开，校验基准是两项合起来：
 *      pending 非空看队尾，为空看上一步实际方向。只比 lastMove 会误杀合法转向
 *      （向右时快速按"上、左"）；只比队尾则队列空时没有基准，等于没校验。
 *   4. 反向校验只能在入队时做，因为只有那一刻才知道该不该收；取的时候已经无权反悔。
 *   5. body() 返回拷贝而不是内部队列，否则 view 一句 pollLast() 就能剪掉蛇尾，还不报错。
 */
public class Snake {
    private final ArrayDeque<Cell> body = new ArrayDeque<>();
    private final ArrayDeque<Direction> pending = new ArrayDeque<>();
    private Direction lastMove;
    private static final int MAX_PENDING = 2;
    private final int startX, startY, initialLength;
    Snake(int startX, int startY, int initialLength ){
        this.startX = startX;
        this.startY = startY;
        this.initialLength = initialLength;
        reset();
    }
    Cell head(){
        return body.peekFirst();
    }
    int length(){
        return body.size();
    }
    Direction direction(){
        return lastMove;
    }

    /**会消费掉一个, 队列空时回落到 lastMove.*/
    Direction consumeDirection(){
        Direction d = pending.isEmpty() ? lastMove : pending.pollFirst();
        this.lastMove = d;
        return d;
    }
    void request(Direction d){
        Direction ref = pending.isEmpty() ? lastMove : pending.getLast();
        if(d.isOpposite(ref)) return;
        if(pending.size() >= MAX_PENDING) return;
        if(pending.contains(d)) return;
        pending.addLast(d);
    }
    void moveTo(Cell newHead){
        body.addFirst(newHead);
    }
    void removeTail(){
        body.pollLast();
    }
    boolean occupies(Cell c){
        return body.contains(c);
    }
    boolean hitSelf(Cell newHead, boolean willGrow){
        boolean res = body.contains(newHead);
        if(!willGrow && Objects.equals(body.peekLast(), newHead)) res = false;
        return res;
    }
    public List<Cell> body(){
        return List.copyOf(body);
    }
    void reset(){
        body.clear();
        pending.clear();
        lastMove = Direction.RIGHT;
        for(int i = 0; i < initialLength; i++){
            this.body.addFirst(new Cell(startX + i, startY));
        }
    }
}
