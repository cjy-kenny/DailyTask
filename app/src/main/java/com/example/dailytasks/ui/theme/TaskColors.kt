package com.example.dailytasks.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.dailytasks.R

data class TaskColorOption(
    val color: Color,
    val labelRes: Int
)

/**
 * 任务可选颜色。索引保存在数据库中，调整顺序时需要同步修改默认索引与迁移逻辑。
 */
val TaskColorOptions: List<TaskColorOption> = listOf(
    TaskColorOption(Color(0xFF2D6CDF), R.string.color_blue),
    TaskColorOption(Color(0xFF2E9E6B), R.string.color_green),
    TaskColorOption(Color(0xFFE08A2E), R.string.color_orange),
    TaskColorOption(Color(0xFFD9534F), R.string.color_red),
    TaskColorOption(Color(0xFF8B5CF6), R.string.color_purple),
    TaskColorOption(Color(0xFF0E9AA7), R.string.color_teal),
    TaskColorOption(Color(0xFFD46A9F), R.string.color_pink),
    TaskColorOption(Color(0xFF6B7280), R.string.color_gray)
)

fun taskColor(colorIndex: Int): Color =
    TaskColorOptions[colorIndex.coerceIn(0, TaskColorOptions.lastIndex)].color
