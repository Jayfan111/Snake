package model;

/**
 * 四个方向，各带一对位移。移动逻辑统一写成 新头 = 旧头 + 方向，不用四个 if。
 *
 *   dx, dy               UP(0,-1) / DOWN(0,1) / LEFT(-1,0) / RIGHT(1,0)
 *   isOpposite(other)    两个方向是否正相反，不掺"当前正在往哪走"的上下文
 *
 * 经验：枚举天然单例且不可变，返回和传参都不用防御性拷贝；
 *       它只描述"往哪走"，"能不能走"属于 Board / Snake。
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
