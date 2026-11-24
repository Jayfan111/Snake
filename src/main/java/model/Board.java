package model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 棋盘：边界、食物、空格。
 *
 *   cols / rows            格子数（不是像素），建好后不变
 *   food                      当前食物；棋盘被占满时为 null（表示通关，不是错误）
 *
 *   cols() / rows()        给 view 算像素尺寸
 *   isInside(Cell)            0 <= x < cols 且 0 <= y < rows
 *   spawnFood(occupied)       从所有空格里随机挑一个，没空格就置 null。food 的唯一写入点
 *   isFoodAt(Cell)            先判 null 再比坐标
 *
 * 经验：
 *   1. 食物只能有一个写入点。view 若自己随机画一个，界面和判定就是两份坐标 ——
 *      表现为"明明吃到了却没加分"，很难想到是这里。
 *   2. 先收集空格再随机取，别"取随机坐标再看撞没撞上蛇"：满盘会死循环。
 *   3. nextInt(0) 抛异常，所以空集合判断必须在取随机数之前。
 */
public class Board {
    private final int cols;
    private final int rows;
    private Cell food;

    public Board(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
    }

    public int cols() {
        return cols;
    }

    public int rows() {
        return rows;
    }

    public Cell food() {
        return food;
    }

    boolean isInside(Cell c) {
        return c != null && c.x() >= 0 && c.x() < cols && c.y() >= 0 && c.y() < rows;
    }

    void spawnFood(Collection<Cell> occupied) {
        Set<Cell> taken = new HashSet<>(occupied);
        List<Cell> empty = new ArrayList<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
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

    boolean isFoodAt(Cell c) {
        return food != null && food.equals(c);
    }
}
