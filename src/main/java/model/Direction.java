package model;

/**
 * 四个移动方向，各带一对位移。
 *
 * 常量：UP(0,-1) / DOWN(0,1) / LEFT(-1,0) / RIGHT(1,0)
 *
 * 字段：
 *   public final int dx, dy   位移量。移动逻辑因此能统一写成"新头 = 旧头 + 方向"，
 *                             不用写四个 if 分支。
 *
 * 方法：
 *   boolean isOpposite(Direction other)   两个方向是否正好相反
 *
 * 三条经验：
 *   1. isOpposite 只负责比较"两个方向"，不要在里面掺入"当前正在往哪走"的上下文。
 *      校验时的基准是"待处理队列的队尾，队列空则上一步实际走的方向" ——
 *      这两者都可能不是 this。
 *   2. 它只描述"往哪走"，不关心"能不能走"：越界属于 Board，规则属于 Snake / GameModel。
 *   3. 枚举天然单例且不可变，外部拿到引用也改不了，所以返回、传参都不需要防御性拷贝。
 */
public enum Direction {
    UP(0,-1),
    DOWN(0,1),
    LEFT(-1,0),
    RIGHT(1,0);
    public final int dx, dy;
    Direction(int dx, int dy){
        this.dx = dx;
        this.dy = dy;
    }
    boolean isOpposite(Direction other){
        return switch(this){
            case UP -> other == DOWN;
            case DOWN -> other == UP;
            case LEFT -> other == RIGHT;
            case RIGHT -> other == LEFT;
        };
    }
}
