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
 * boolean isInside(Cell c)                     —— 已有，但下界漏了，见下
 * Cell food()                                  —— 当前食物坐标，可能为 null
 * void spawnFood(Collection<Cell> occupied)    —— 重新放食物；棋盘满时置 null
 * boolean isFoodAt(Cell c)                     —— 顺手给 GameModel 用
 *
 * 【现在这个 isInside 的两个问题】
 * 1. 只判了上界，没判下界：x = -1 会返回 true。负坐标在贪吃蛇里是真实存在的
 *    （蛇头往上走一步就是 y-1），漏判的后果是"穿墙了却不报错"，
 *    而且这条 bug 只有在贴着左墙或上墙的时候才出现。
 * 2. 用了 <= 而不是 <：坐标范围是 0..width-1，x == width 已经在界外了。
 *    这是最典型的 off-by-one，表现为"贴着右墙还能多走一格才死"。
 *
 * 【完成标志】
 * 蛇占满整个棋盘时，随机放食物返回"没有位置了"，而不是卡死。
 */
public class Board {
    int width,height;
    Cell food;
    Board(int width, int height){
        this.height = height;
        this.width = width;
    }
    int width(){
        return this.width;
    }
    Cell food(){
        return this.food;
    }
    boolean isInside(Cell c){
        return c != null && c.x() < width && c.x() >= 0 && c.y() < height && c.y() >= 0;
    }
    void spawnFood(Collection<Cell> occupied){
        Set<Cell> taken = new HashSet<>(occupied);
        List<Cell> empty = new ArrayList<>();
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                Cell c = new Cell(x, y);
                if(!taken.contains(c)) empty.add(c);
            }
        }
        if(empty.isEmpty()){
            food = null;
            return;
        }
        food = empty.get(ThreadLocalRandom.current().nextInt(empty.size()));
    }
    boolean isFoodAt(Cell c){
        return c !=null && food.equals(c);
    }
}
