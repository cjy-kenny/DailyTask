package com.example.dailytasks.data

/**
 * 任务重复规则，持久化为整数，保证数据库迁移与旧数据兼容。
 */
object RepeatRule {
    const val NONE = 0
    const val DAILY = 1
    const val WEEKLY = 2
    const val WEEKDAYS = 3

    val all: List<Int> = listOf(NONE, DAILY, WEEKLY, WEEKDAYS)
}
