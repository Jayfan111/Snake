package model;

/**
 * 一局游戏的四个状态。
 *
 * 【要写的】
 * 四个常量：READY（还没开始）、RUNNING、PAUSED、GAME_OVER。
 *
 * 这个枚举里不要写任何逻辑。它的价值在于：把"现在处于什么阶段"变成一个有名字的类型，
 * 而不是用 int 0/1/2 或者几个 boolean 拼出来 —— 后者的问题是
 * "既暂停又结束"这种非法组合也写得出来。
 *
 * 【谁能改状态，必须分清】
 * - READY -> RUNNING      ：Main 启动，或玩家第一次按键
 * - RUNNING <-> PAUSED    ：InputHandler（同时要 stop / start Timer）
 * - RUNNING -> GAME_OVER  ：只有 GameModel.step() 有权
 * - GAME_OVER -> READY    ：InputHandler 调用 model.reset()
 *
 * 状态属于规则，所以放在 model 包里，而不是放 loop 里当"调度标志"。
 *
 * 【方法清单】
 * 没有方法。四个常量就够了。
 *
 * 不要在枚举里写"现在能不能动""该不该结束"这类判断 —— 那是 GameModel 的规则。
 * 一旦枚举开始有行为，状态机就会分裂成两个地方，改一处忘一处。
 *
 * 【完成标志】
 * 四个常量齐全，除此之外这个文件没有任何其他内容。
 */
public enum GameState {
    READY,
    RUNNING,
    PAUSED,
    GAME_OVER;
}
