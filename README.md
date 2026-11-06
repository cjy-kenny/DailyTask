# 每日任务清单

一个使用 Kotlin + Jetpack Compose + Room 开发的本地每日任务管理 App（MVP）。

## 功能

- 首页按日期分组展示任务，分组标题为「今天」「明天」「昨天」或视野内的具体日期
- 右上角可在「清单视图」与「日历视图」之间切换；日历按月展示，日期格子里用任务颜色的圆点标记当天的任务
- 点击日历中的日期，下方显示该天的任务；支持切换月份、回到今天，并可直接为选中日期添加任务
- 清单视图顶部提供本周日期条，点击任意一天按日期筛选，再次点击或「显示全部」取消筛选
- 顶部搜索按钮可搜索任务名称与备注，并可切换「全部 / 未完成 / 已完成」状态筛选
- 任务卡片显示任务名称、日期时间、备注、颜色标识、优先级与完成状态
- 可为任务标记「重要」，重要任务在卡片上显示红色标签，并在同一时间下优先排序
- 已完成任务使用灰色文字、删除线和勾选图标区分
- 点击右下角 `+` 打开新增任务弹窗，任务名必填，可设置 8 种任务颜色、备注、优先级与到时提醒
- 在日历视图中新增任务时，默认日期为当前选中的日期；在清单视图按日期筛选时同样默认使用该日期
- 点击任务卡片编辑名称、备注、颜色、优先级、提醒和日期时间
- 顶部统计按钮打开「统计与回顾」：今日完成环形进度、近 7 天完成柱状图与连续完成天数
- 支持「每天 / 每周 / 工作日」重复任务，完成后自动生成下一次任务并重新排期提醒
- 任务卡片显示重复规则标签；清单内可按时间、优先级或创建时间排序
- 长按任务卡片或更多菜单中的「多选模式」进入批量选择，可批量完成或删除（删除支持撤销）
- 清单与日历视图切换带淡入滑动过渡，任务增删带列表动画
- 任务卡片右上角更多菜单中的「删除」会二次确认；长按卡片进入多选模式
- 向右滑动任务卡片标记完成；向左滑动直接删除，并通过底部 Snackbar 提供「撤销」
- 点击圆形完成按钮切换完成状态，再次点击可取消
- 支持任务到时本地通知（Android 13+ 首次启动会请求通知权限），重启设备后自动重新排期
- 外观设置支持「跟随系统 / 浅色 / 深色」三种模式，以及 6 种主题色，设置保存在本机并立即生效
- 无任务时显示「今天还没有任务，点击 + 添加任务」
- 数据使用 Room 保存在本地 SQLite，关闭 App 后仍保留

## 技术栈

| 项目 | 版本 |
| --- | --- |
| Kotlin | 2.0.20 |
| Jetpack Compose | BOM 2024.09.02（Material 3） |
| Room | 2.6.1（KSP） |
| Android Gradle Plugin | 8.5.2 |
| Gradle | 8.9 |
| compileSdk / targetSdk | 34 |
| minSdk | 24 |
| JDK | 17 或 21 |

## 项目结构

```
app/src/main/java/com/example/dailytasks/
├── MainActivity.kt              # 入口 Activity，请求通知权限，启用 edge-to-edge
├── data/
│   ├── TaskEntity.kt            # id、title、dueAt、isCompleted、note、colorIndex、priority、reminderEnabled、repeatRule、completedAt
│   ├── TaskDao.kt               # Flow 查询、增删改、完成状态、待提醒任务
│   ├── AppDatabase.kt           # Room 单例，v4 迁移（1→2、2→3、3→4）
│   └── TaskRepository.kt        # 增删改、重复任务生成、批量与撤销
├── reminder/
│   └── ReminderScheduler.kt     # AlarmManager 排期、通知接收器、开机重排
├── ui/
│   ├── DailyTaskApp.kt          # 读取外观设置并套用主题
│   ├── TaskViewModel.kt         # StateFlow + viewModelScope + 提醒排期
│   ├── MainScreen.kt            # 清单/日历、搜索筛选、周日期条、滑动操作、任务卡片
│   ├── TaskEditorDialog.kt      # 新增/编辑弹窗、颜色、备注、优先级、重复规则、提醒开关、DatePicker、TimePicker
│   ├── SettingsDialog.kt        # 外观模式与主题色设置
│   ├── StatsDialog.kt           # 统计与回顾：环形进度、近 7 天柱状图、连续天数
│   └── theme/
│       ├── AppearancePreferences.kt # 主题偏好持久化
│       ├── TaskColors.kt        # 8 种任务颜色及索引映射
│       ├── Color.kt / Theme.kt / Type.kt
└── util/
    ├── TaskDateUtils.kt         # 日期格式化、时区换算、月历与本周格子生成
    └── TaskGrouping.kt          # 按天分组、今天/明天/昨天关系、优先级排序
```

## 构建

环境要求：JDK 17+、Android SDK Platform 34、Build-Tools 34.0.0。

```powershell
# 使用项目 Wrapper（首次会下载 Gradle 8.9）
./gradlew assembleDebug
./gradlew assembleRelease

# 或使用本机已安装的 Gradle 8.9
gradle assembleDebug
```

如果 SDK 路径不同，请修改 `local.properties`：

```properties
sdk.dir=C\:\\Android\\Sdk
```

构建产物：

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`
- 交付副本位于 `dist/`

## 安装

```powershell
adb install -r dist/每日任务清单-1.3.apk
```

> Release 包当前使用 Android 调试证书签名，仅用于本地体验与 MVP 交付；正式上架前应替换为自己的发布签名。

## 测试

```powershell
./gradlew testDebugUnitTest
```

共 10 个 JVM 测试，全部通过：

- `TaskDateUtilsTest`：日期格式化、DatePicker UTC/本地时区换算、跨月日期关系、按日期分组与排序、月历与本周格子生成、日期键与时间合并、重复规则推算、优先级排序
- `TaskPersistenceTest`（Robolectric + Room）：写入、完成切换、关闭并重开数据库后数据与排序仍存在、删除
- `MainScreenUiTest`（Robolectric + Compose UI）：空状态、新增弹窗、日期/时间选择器、空标题校验、新增、完成、编辑、删除确认、颜色与备注、日历视图展示及月份切换

## MVP 实现说明

- 首页展示全部任务（不区分是否今天），按日期时间升序分组；同一时间下重要任务优先
- 长按任务卡片进入多选模式；更多菜单中的「删除」会弹出确认框；左滑删除则立即删除并提供撤销
- 完成按钮使用独立的点击区域，点击任务卡片其他区域进入编辑
- 空状态文案按要求固定为「今天还没有任务，点击 + 添加任务」
- 任务颜色以索引形式保存在 Room，数据库版本升级到 v4，并提供 1→2、2→3、3→4 迁移（新增 `note`、`color_index`、`priority`、`reminder_enabled`、`repeat_rule`、`completed_at` 字段）
- 重复任务在完成后按规则生成下一次任务；同标题同时间已存在时不会重复生成
- 统计页基于任务的完成时间（`completed_at`）计算今日环形进度、近 7 天完成量与连续完成天数
- 日历为自绘月历（周一开始，6×7 补齐相邻月份日期），不依赖第三方日历库；日期格最多显示 3 个任务颜色点
- 提醒使用 `AlarmManager` 的非精确闹钟（无需特殊权限），系统省电策略下可能有分钟级延迟；只对未完成、开启提醒且到期时间在未来的任务排期
- 未加入登录、云同步、多清单、多人协作等非 MVP 功能
- 外观模式支持跟随系统、浅色、深色，并可在设置中切换主题色
