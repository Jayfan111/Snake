package model;

/**
 * 一局的四个状态：READY / RUNNING / PAUSED / GAME_OVER。
 *
 * 只能通过 GameModel 的方法改：start()、togglePause()、reset()，以及 step() 里的 die()。
 * 外部（view / loop）永远只是调用这些方法，不直接碰字段。
 *
 * 经验：用枚举而不是 int 0/1/2 或几个 boolean —— 后者允许"既暂停又结束"这种
 *       非法组合被写出来，读代码时还要回去查数字含义。
 */
public enum GameState {
    READY,
    RUNNING,
    PAUSED,
    GAME_OVER;
}
