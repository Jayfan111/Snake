# Snake

包	类	职责	关键成员

model	Cell（record）	格子坐标，值对象	x、y

model	Direction（enum）	四个方向 + 反向判断	dx、dy、isOpposite()

model	Snake	蛇身 + 待处理方向队列	body、request()、moveTo()、occupies()

model	Board	边界、食物、空格计算	isInside()、randomEmptyCell()

model	GameModel	一局游戏的全部状态	state、score、step()、reset()

loop	Main	建窗口、装配、启动	main()

loop	GameLoop	掌管时钟与跨层调度	Timer、start()、stop()、pause()

view	GamePanel	把 model 画成画面	paintComponent()

view	InputHandler	把按键翻译成 model 的意图	bind()