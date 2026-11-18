package model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 棋盘：边界、食物、空格。
 *
 * 【要写的字段】
 * 宽、高（单位是格子数），以及当前食物的坐标。
 *
 * 【要写的方法】
 * 1. isInside(Cell c)：坐标是否在界内。撞墙判定就靠它。
 * 2. 随机放食物：参数要能知道"哪些格子被蛇占了"，否则食物会落在蛇身上。
 *    做法是先把所有没被占的格子收集成一张表，再从表里随机挑一个。
 *    千万不要在 while 里反复抽随机坐标、看是否撞上蛇 —— 蛇快占满棋盘时
 *    会抽非常久，甚至永远抽不出来（死循环）。
 * 3. 吃完之后换一个食物位置。
 *
 * 【不要写的】
 * 不要持有 Snake 的引用去改蛇；不要判断"吃到了"，那是 GameModel 的事。
 * 不要 import 任何 awt / swing —— 这个包必须能脱离界面独立跑。
 *
 * 【方法清单】
 * 构造：Board(int width, int height)
 * int width() / int height()                   —— 给 view 算像素用
 * boolean isInside(Cell c)                     —— 撞墙判定
 * Cell food()                                  —— 当前食物坐标，可能为 null
 * void spawnFood(Collection<Cell> occupied)    —— 重新放食物；棋盘满时置 null
 * boolean isFoodAt(Cell c)                     —— 顺手给 GameModel 用
 *
 * 【坐标约定】
 * 合法范围是 0 <= x < width、0 <= y < height。左上角是 (0,0)，
 * y 轴向下增长 —— 跟屏幕像素坐标一致，所以画图时不需要翻转。
 *
 * 【完成标志】✓ 已达成
 * 蛇占满整个棋盘时 spawnFood 把 food 置 null，而不是卡死或抛异常。
 */
public class Board {
    private final int width;
    private final int height;
    private Cell food;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public Cell food() {
        return food;
    }

    public boolean isInside(Cell c) {
        return c != null && c.x() >= 0 && c.x() < width && c.y() >= 0 && c.y() < height;
    }

    public void spawnFood(Collection<Cell> occupied) {
        Set<Cell> taken = new HashSet<>(occupied);
        List<Cell> empty = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell c = new Cell(x, y);
                if (!taken.contains(c)) {
                    empty.add(c);
                }
            }
        }
        if (empty.isEmpty()) {
            food = null;
            return;
        }
        food = empty.get(ThreadLocalRandom.current().nextInt(empty.size()));
    }

    public boolean isFoodAt(Cell c) {
        return food != null && food.equals(c);
    }
}
