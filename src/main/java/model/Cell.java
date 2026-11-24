package model;

/**
 * 格子坐标：x = 列，y = 行。record，自动得到 equals / hashCode / toString。
 *
 *   x()   列，向右增大
 *   y()   行，向下增大（和屏幕像素同向，画图不用翻转）
 *
 * 经验：坐标相等是全部判定的地基（是不是食物、撞没撞到自己），所以必须是 record
 *       或手写 equals；不要加 setter，也不要加 static 字段 —— static 属于类，
 *       所有格子会共享同一个坐标，画出来是一团却不报错。
 */
public record Cell(int x, int y) {

}
