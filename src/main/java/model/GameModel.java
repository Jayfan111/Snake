package model;

/**
 * 一局游戏的全部状态与规则。整个项目的心脏。
 *
 * 【要写的字段】
 * Snake、Board、分数、GameState。
 *
 * 【要写的方法】
 * 1. step()：一个 tick 调一次。五步的顺序不能错：
 *    a. 状态不是 RUNNING 就直接返回
 *    b. 从待处理队列取一个方向，队列空就沿用当前方向
 *    c. 算新头坐标（先不写进蛇身）
 *    d. 判断新头是不是踩到食物 —— 这一步必须先做
 *    e. 判死活：出界 -> 结束；撞到身体 -> 结束。
 *       撞身体时要注意：如果这一 tick 没吃到食物，最后一格（尾巴）会移走，
 *       所以头进到尾格是合法的，不算撞。
 *    f. 落子：头入队；吃到就不去尾（变长）、加分、换食物、可以加速；
 *       没吃到就去尾。
 * 2. reset()：重开一局，所有字段回到初始状态。
 * 3. getter：给 view 读身体、食物、分数、状态。
 *    只暴露 getter，不要 setter —— view 拿到 model 只该能看。
 * 4. requestDirection(Direction d)：转发给 Snake，
 *    顺便挡掉"非 RUNNING 状态下的转向"这种规则。
 *
 * 【为什么 d 一定要在 e 之前】
 * 不知道吃没吃到，就不知道尾巴会不会让位，也就没法判断头撞到的算不算身体。
 * 顺序反了会有两个 bug：长蛇贴着自己尾巴转弯被误判死亡；
 * 以及已经撞死了却还加了分。
 *
 * 【不要写的】
 * 不要持有 Timer；不要 import awt / swing；不要读写文件；不要调 repaint。
 * 这个类必须能在完全没有窗口的情况下跑起来 —— 这是它最大的价值。
 *
 * 【方法清单（名字已定，照着填）】
 * GameModel(int width, int height)        构造：建 snake 和 board
 * GameState state()                       给 view 决定画不画遮罩
 * int score()                             当前分数
 * Snake snake()                           给 view 读长度、遍历身体
 * Board board()                           给 view 读尺寸、食物位置
 * int delayMillis()                       当前步长，给 GameLoop 去设 Timer
 * void start()                            READY -> RUNNING
 * void togglePause()                      RUNNING <-> PAUSED
 * void requestDirection(Direction d)      转发给 snake；只在 RUNNING 时接受
 * void step()                             核心，就是上面那五步
 * void reset()                            全部字段回到初始状态
 * 建议的私有 helper：isOutOfBounds(Cell)、eat()、die()、speedUp()
 *                    —— step() 才不会被写成 60 行。
 *
 * 【完成标志】
 * 在一个临时 main 里循环调用 step() 并打印蛇的坐标，
 * 能看见它在走、吃到后变长、撞到边界后状态变成 GAME_OVER。
 */
public class GameModel {
}
