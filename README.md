太行卫士：农大护田战 - 项目骨架

说明
- 这是一个 Java Swing 五路塔防（类植物大战僵尸）项目：敌人从右向左沿 5 条固定道路推进，玩家通过打字获能量并放置塔牌守护果园。
- 核心循环：输入 -> 获能量 -> 放塔 -> 自动战斗 -> 波次结束三选一增益 -> 下一波（无限循环直到生命归零）。

如何运行（在 Windows PowerShell）：

1. 使用 IDEA 打开该项目，运行 `com.hbau.taihang.Main`。

或 通过命令行：
```powershell
# 进入项目根目录
cd C:\Users\28729\IdeaProjects\TaihangDefender

# 清理并创建输出目录
if (Test-Path out) { Remove-Item out -Recurse -Force }
New-Item -ItemType Directory out | Out-Null

# 编译所有 Java 源到 out（单次 javac 调用，避免依赖顺序问题）
$files = Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& javac -d out $files

# 把 resources 复制到 out 以便打包到 jar
if (Test-Path resources) { Copy-Item -Path resources -Destination out -Recurse }

# 生成可执行 jar（使用 manifest.txt）
jar cfm TaihangDefender.jar manifest.txt -C out .

# 运行 jar
.
java -jar TaihangDefender.jar
```

可选：运行无界面烟囱测试（验证词库加载与匹配逻辑）
```powershell
java -cp out com.hbau.taihang.SmokeTest
```

资源
- 词库位于 `resources/words/`，程序加载相对路径中的三个示例文件（basic/professional/spirit）。

输入与交互（当前版本）
- `1` / `2` / `3`：快速切换塔型（无人机/灌溉/农药）。
- 鼠标左键：在五路网格内点击放塔；按住拖拽并释放可在释放点放塔。
- 鼠标右键：打开塔购买菜单。
- `Esc`：若正在拖拽则取消预览；否则将焦点回到打字输入框。
- `U` 或底部“撤销 (U)”按钮：在短时间窗口内撤销最近一次放塔（支持多级撤销）。
- 波次清空后会进入“增益三选一”阶段：可按 `1/2/3` 或点击卡片选择。
- 放置预览会显示当前选中塔型的攻击范围圆与五路网格位置，便于规划防线。
- 放置成功/失败会有绿色/红色脉冲反馈；首次进入游戏会显示快捷键引导层。
- 正确输入单词会有绿色正反馈，打错会有红色负反馈，并直接作用在输入框附近。
- 左上角 `?` 帮助按钮可随时查看控制说明。
- 窄窗口下底部按钮会自动切为紧凑标签（`机/灌/药`），避免重叠，并可通过 tooltip 查看完整说明。
- 卡片会显示塔型定位、特性说明、攻/射/速参数，不再只是简单按钮。

核心循环（新逻辑）
- 输入正确单词 -> 获得能量。
- 消耗能量放置塔牌（卡池：无人机/灌溉/农药），塔位限制在 5 路固定格子。
- 塔自动攻击路径敌人：
  - 无人机塔：单体高射程。
  - 灌溉塔：命中附带群体减速。
  - 农药塔：命中附带范围溅射。
- 敌人速度已整体下调，给玩家更充足的反应时间。
- 清空当前波后触发 Roguelike 三选一增益：
  - 塔牌升级（全塔伤害永久提升）
  - 词汇加成（每词额外能量）
  - 科技助农临时 buff（限时强化）
- 选择后进入下一波，直到生命耗尽。

IDEA Copilot 提示词模板（推荐）
在新建 `.java` 文件开头粘贴如下注释，再让 Copilot 生成：

```java
// Java Swing 游戏
// 类型: 塔防卡牌 + 打字技能 + Roguelike 无限循环
// 功能:
// 1) 上方显示单词，玩家输入正确获得能量
// 2) 下方塔牌卡池，玩家消耗能量放塔
// 3) 敌人沿路径移动，塔自动攻击
// 4) 每波结束弹出三选一增益
// 5) 无限循环直到生命归零
// 约束:
// - 面向对象，模块化设计（Engine / Panel / Controller / Entities）
// - Game loop 使用 Swing Timer
// - 类可扩展：Enemy, Tower, Bullet, Perk
// - 代码注释简洁，方法职责明确
// 输出: 完整可运行的 Java 类代码
```

模块化生成建议
- 先生成 `GameEngine`（核心循环 + 波次/增益状态机）
- 再生成 `GamePanel`（渲染 + UI 卡片）
- 再生成 `GameInputController`（键鼠输入）
- 最后补实体类（`Enemy` / `Tower` / `Bullet`）与 `TypingField`

农大融合设计点
- 主题视觉采用“护田、灌溉、病虫防治”语义（无人机塔/灌溉塔/农药塔）。
- 打字资源以农业相关词汇为核心，鼓励“边学词汇边护田”。
- 画面右上角有“农大护田 - 太行卫士”水印，便于课堂演示与课程归属识别。

便携字体（保证在演示机器上不出现方块或缺字）
 - 为了保证在不同电脑上演示时不会因为系统缺少中文字体而看到方块（□□□），程序支持内嵌可移植字体。
 - 如果你希望打包成完全可移植的 jar，请把一个支持中文的字体文件（推荐 Noto Sans CJK SC，Apache 2.0 授权）放到项目的 `resources/fonts/` 目录下，文件名例如 `NotoSansCJKsc-Regular.otf` 或 `NotoSansSC-Regular.ttf`。
 - 打包时确保 `resources/` 被包含进 jar（README 上方已有示例命令），程序启动时会优先加载 `resources/fonts/` 里的字体并注册到运行时，保证字符串在没有系统中文字体的机器上也能正确渲染。

 - 示例：将 `NotoSansCJKsc-Regular.otf` 放入 `resources/fonts/`，然后按 README 的打包步骤生成 jar，即可得到自带字体的可移植包。

自动下载字体（推荐）
 - 为方便起见，仓库中包含了一个 PowerShell 脚本 `scripts/fetch-font.ps1`，它会尝试从官方 GitHub 仓库下载 Noto Sans CJK SC 到 `resources/fonts/`。
 - 在 Windows 演示机器上，运行以下命令来获取字体：

```powershell
# 从仓库根目录运行一次，脚本会把字体保存到 resources/fonts/
.\scripts\fetch-font.ps1
```

 - 下载成功后按常规构建和打包流程将字体包含进 jar：脚本会使 `resources/fonts/` 目录存在并包含字体，随后 README 中的打包步骤会把它打进 jar。

演示前准备（一步到位清单）
1) 将仓库拷贝或克隆到演示电脑。
2) 在仓库根目录打开 PowerShell，然后运行：

```powershell
.\scripts\build-and-package.ps1
```

该脚本会尝试下载字体、编译、复制 resources 并生成 `TaihangDefender.jar`。如果下载字体失败（无网络），脚本仍会生成 jar，但不包含字体；此时请手动把字体放到 `resources/fonts/` 后重跑脚本。

3) 运行生成的 jar：

```powershell
java -jar TaihangDefender.jar
```

快速验收：启动后检查界面文字是否为中文（无方块），并在游戏里测试 1/2/3 切塔、左键放塔、U 撤销。

若出现“方块/乱码”
- 这通常不是编码问题，而是运行机器缺少可用中文字体。
- 推荐优先运行：

```powershell
.\scripts\build-and-package.ps1
```

- 若仍有问题，请手动将 `NotoSansCJKsc-Regular.otf` 放到 `resources/fonts/` 后重新打包运行。
- 程序启动时会弹出“字体提示”警告，帮助快速定位该问题。
下一步建议
- 完善渲染与交互；已将 `WordManager` 改为优先从类路径加载 `resources/words/*.txt`（这样打包进 jar 后也能加载），并保留文件系统回退；
- 实现更丰富的敌人、塔、波次配置；
- 在 `WordManager` 中实现前缀匹配（用于实时提示）与难度分层；
- 打包时请确保 `resources/` 目录被包含在 jar（上面命令会把 resources 复制到 out 并打包）。

