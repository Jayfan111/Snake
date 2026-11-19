package model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 棋盘：边界、食物、空格。
 *
 * 字段：
 *   private final int width, height   格子数，不是像素；建好后不变
 *   private Cell food                 当前食物；棋盘被占满时为 null（表示通关，不是错误）
 *
 * 方法：
 *   Board(int width, int height)                 只记尺寸，不放食物
 *   int width() / height()                       给 view 算像素尺寸
 *   boolean isInside(Cell c)                     0 <= x < width 且 0 <= y < height
 *   void spawnFood(Collection<Cell> occupied)    从所有空格里随机挑一个；没有空格则置 null
 *   Cell food()                                  当前食物，可能为 null
 *   boolean isFoodAt(Cell c)                     这个格子现在是不是食物；内部先判 null
 *
 * 坐标约定：左上角是 (0,0)，x 向右、y 向下（跟屏幕像素一致，画图不用翻转）。
 *
 * 四条经验：
 *   1. 食物是状态，只允许一个写入点（spawnFood）。如果 view 里自己随机画一个，
 *      界面和判定用的就是两份坐标 —— 表现为"明明吃到了却没加分"，而且很难想到是这里。
 *   2. 先收集空格再随机挑，不要"随机抽一个坐标再看有没有撞上蛇"：蛇快占满时会反复重试，
 *      棋盘满时直接死循环，不报错、只是卡住。
 *   3. nextInt(0) 会抛异常，所以 empty.isEmpty() 必须在取随机数之前判。
 *   4. 参数用 Collection<Cell> 而不是 Snake：Board 只需要知道"哪些格子被占"，
 *      不需要认识蛇。谁被传进来不重要，依赖方向才重要。
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
