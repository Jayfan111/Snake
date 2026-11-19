package model;

/**
 * 一个格子的坐标：x = 列，y = 行。不可变值对象。
 *
 * 字段（写在 record 头部，自动生成 x() / y()）：
 *   int x   列，向右增长
 *   int y   行，向下增长（跟屏幕像素坐标一致，画图不用翻转）
 *
 * 方法：record 自动生成 x() / y() / equals / hashCode / toString，不用手写。
 *
 * 三条经验：
 *   1. 用 record 而不是普通 class，是为了白拿 equals / hashCode ——
 *      "这个格子是不是食物""蛇头撞到的是不是身体"全靠坐标相等来判断。
 *      普通类忘了重写 equals，就会出现"坐标一样却判不相等"。
 *   2. 不可变：没有 setter，蛇每走一步都是新建一个 Cell，旧的自然被丢弃。
 *      不要给它加改坐标的方法。
 *   3. 绝对不要加 static 字段：static 属于类而不是实例，所有格子会共享同一个坐标，
 *      蛇身画出来是一团，而且不报错、不抛异常 —— 这是最难查的一类 bug。
 */
public record Cell(int x, int y) {

}
