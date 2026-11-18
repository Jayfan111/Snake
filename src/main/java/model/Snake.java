package model;

/**
 * 蛇：身体 + 方向。
 *
 * 【要写的字段】
 * 1. 身体：建议用 ArrayDeque<Cell>，头在队首。加头、去尾都是 O(1)。
 * 2. 当前方向。
 * 3. 待处理方向队列 —— 这个类最容易被忽略、也最关键的一点。
 *    玩家在一次移动间隔里可能按两三次键，如果直接改"当前方向"，
 *    前面的按键会被后面的覆盖，表现就是"按了没反应"。
 *    正确做法是排队，每个 tick 消费一个。
 *
 * 【要写的方法】
 * 1. head()：队首，也就是蛇头坐标。
 * 2. request(Direction d)：把方向放进待处理队列。
 *    反向的要丢弃 —— 关键是跟"上一步实际使用的方向"比，而不是跟队列尾部比，
 *    否则连按两次右键就能绕过检查，蛇直接掉头撞自己。
 *    队列长度要有上限（比如 2），否则疯狂按键会把转向攒起来，
 *    松手之后蛇自己继续拐弯。
 * 3. moveTo(Cell newHead)：把新头加进队首。
 * 4. removeTail()：去掉队尾。
 * 5. occupies(Cell c)：这个格子在不在身上（判自撞用）。
 *
 * 【不要写的】
 * 不要判撞墙（Board 管）、不要判吃到（GameModel 管）、
 * 不要判断"多长算赢"。
 *
 * 【方法清单（名字已定，照着填）】
 * Snake(int startX, int startY, int initialLength)   构造，初始朝右
 * Cell head()                                       队首
 * int length()                                      身体长度
 * Direction direction()                             当前方向
 * Direction consumeDirection()                      每个 tick 调一次：取一个待处理方向，
 *                                                   队列空就返回当前方向
 * void request(Direction d)                         入队 + 反向校验 + 队列上限
 * void moveTo(Cell newHead)                         头入队
 * void removeTail()                                 尾出队
 * boolean occupies(Cell c)                          这个格子在不在身上
 * boolean hitsSelf(Cell newHead, boolean willGrow)  判自撞；willGrow 为 false 时
 *                                                   排除尾格（尾巴这一 tick 会移走）
 * List<Cell> body()                                 给 view 从头到尾遍历，返回只读拷贝
 *
 * 【完成标志】
 * 连按两次反向键，只有一次生效。
 */
public class Snake {
}
