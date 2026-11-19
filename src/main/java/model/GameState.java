package model;

/**
 * 一局的四个状态。
 *
 * 常量：READY（还没开始）/ RUNNING / PAUSED / GAME_OVER
 * 方法：没有。也**不要**往里加"能不能动""该不该结束"这类判断 —— 那是 GameModel 的规则。
 *
 * 为什么用枚举，而不是 int 0/1/2 或几个 boolean：
 *   后者允许"既暂停又结束"这种非法组合被写出来，而且读代码时要回去查数字含义。
 *
 * 谁能改状态（改之前先确认自己是不是那个角色）：
 *   READY → RUNNING        Main 启动，或玩家第一次按键
 *   RUNNING ⇄ PAUSED       InputHandler（同时 stop / start Timer）
 *   RUNNING → GAME_OVER    只有 GameModel.step()
 *   GAME_OVER → READY      InputHandler 调 model.reset()
 *
 * 状态属于规则，所以放 model 包，而不是放 loop 里当调度标志。
 */
public enum GameState {
    READY,
    RUNNING,
    PAUSED,
    GAME_OVER;
}
