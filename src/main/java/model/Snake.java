package model;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;

/**
 * 蛇：身体队列 + 待处理方向。只管"蛇自己"，不管撞墙、不管吃没吃到。
 *
 *   body      ArrayDeque<Cell>，队首是头、队尾是尾
 *   pending   已接受但还没被 tick 消费的方向，最多 MAX_PENDING = 2
 *   lastMove  上一次实际生效的方向
 *
 *   head() / length() / direction()   读状态；length 就是 body.size()，不缓存
 *   consumeDirection()                每 tick 一次：取队首并写回 lastMove，队列空则沿用 lastMove
 *   request(dir)                      入队前校验：反向、超过 2 个、重复的都静默丢弃
 *   moveTo(newHead) / removeTail()    落子的两个原子动作，走不走由 GameModel 决定
 *   hitSelf(newHead, willGrow)        willGrow = false 时尾格让位（本 tick 尾巴会移走）
 *   occupies(Cell)                    这个格子在不在身上
 *   body()                            返回 List.copyOf 快照给 view
 *   reset()                           clear 后重新铺 initialLength 节，始终朝右
 *
 * 经验：
 *   1. body 是 final 只锁引用，内容照样能改，所以 reset 用 clear()，不 new 新队列。
 *   2. 校验基准 = pending 非空看队尾，空则看 lastMove；只比其中一个都会误判合法转向。
 *   3. 反向校验只能在入队那一刻做，取的时候已经无权反悔。
 *   4. body() 必须返回拷贝，否则 view 一句 pollLast() 就能剪掉蛇尾，还不报错。
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
