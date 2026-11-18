package model;

/**
 * 四个移动方向。
 *
 * 【要写的东西】
 * 1. 四个枚举常量：UP / DOWN / LEFT / RIGHT。
 * 2. 每个方向带一对位移 dx / dy（UP 是 y-1，RIGHT 是 x+1）。
 *    这样移动逻辑可以统一写成"新头 = 旧头 + 方向"，不用写四个 if 分支。
 * 3. isOpposite(Direction other)：判断两个方向是否正好相反。
 *    注意它要能对"任意两个方向"判断，而不是只跟"当前方向"比 ——
 *    校验时你要拿它去比的是"上一步实际用过的方向"。
 *
 * 【不要写的】
 * 不要在这里判越界、判撞墙，那属于 Board。
 * Direction 只描述"往哪走"，不关心"能不能走"。
 *
 * 【方法清单】
 * 已有：dx / dy（public final 字段）、isOpposite(Direction other)
 * - isOpposite 现在是包级访问权限（没有 public），model 包里够用；
 *   但如果以后 view 或 controller 也要调，就编译不过。建议加 public。
 * - 可选简化：isOpposite 可以一行写完，不必 switch ——
 *   判断 other.dx == -dx && other.dy == -dy，以后加方向也不用改这里。
 *
 * 【完成标志】
 * UP.isOpposite(DOWN) 为 true，UP.isOpposite(LEFT) 为 false。
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
