package model;

/**
 * 一个格子的坐标（x = 列，y = 行），不可变值对象。
 *
 * 【这个类还要写什么】
 * 基本不用再写了，你已经把声明改对了。要理解的是它为什么长这样：
 *
 * 1. 用 record 而不是普通 class，是为了自动获得 equals / hashCode。
 *    "这一格是不是食物"、"头撞到的格子是不是身体"全靠坐标相等来判断。
 *    用普通类忘了重写 equals，就会出现"坐标一样却判不相等"的鬼故事。
 *
 * 2. 不可变 —— 字段隐含 final，也没有 setter。蛇每走一步都是"新建一个 Cell"，
 *    旧的自然被丢弃。所以不要给它加修改坐标的方法。
 *
 * 3. 绝对不要加 static 字段。static 属于类而不是实例，所有格子会共享同一个坐标，
 *    蛇身会变成一团，而且不抛异常、不报错，纯逻辑 bug，极难查。
 *
 * 4. 可以加的小工具（可选）：toString 方便打印调试、isAdjacent(Cell other)
 *    判断两个格子是否相邻。别急着加，等真的需要再说。
 *
 * 【方法清单】
 * 已有（record 自动生成，不用写）：x()、y()、equals、hashCode、toString
 * 可选，真的需要时再加：
 *   Cell plus(Direction d)         返回"往 d 走一格"的新坐标。
 *                                  有了它，算新头就是 head.plus(dir)，一行搞定。
 *   boolean isAdjacentTo(Cell o)   曼哈顿距离为 1（本版本其实用不到）
 *
 * 【完成标志】
 * new Cell(3, 4).equals(new Cell(3, 4)) 返回 true。
 */
public record Cell(int x, int y) {

}
