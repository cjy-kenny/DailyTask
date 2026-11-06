package com.example.dailytasks.util

import com.example.dailytasks.data.TaskEntity

enum class DayRelation {
    TODAY,
    TOMORROW,
    YESTERDAY,
    OTHER
}

enum class SortMode {
    TIME,
    PRIORITY,
    CREATED
}

data class TaskGroup(
    val dateKey: String,
    val relation: DayRelation,
    val tasks: List<TaskEntity>
)

fun dateRelation(dateKey: String, nowMillis: Long): DayRelation = when (dateKey) {
    TaskDateUtils.dayKey(nowMillis) -> DayRelation.TODAY
    TaskDateUtils.dayKey(TaskDateUtils.plusDays(nowMillis, 1)) -> DayRelation.TOMORROW
    TaskDateUtils.dayKey(TaskDateUtils.plusDays(nowMillis, -1)) -> DayRelation.YESTERDAY
    else -> DayRelation.OTHER
}

/**
 * 按天分组；组内顺序由 sortMode 决定：
 * 时间优先（默认，同一时间重要任务在前）、优先级优先、最近创建优先。
 * 日期分组本身始终按日期升序。
 */
fun groupTasks(
    tasks: List<TaskEntity>,
    nowMillis: Long,
    sortMode: SortMode = SortMode.TIME
): List<TaskGroup> {
    val comparator = when (sortMode) {
        SortMode.TIME -> compareBy<TaskEntity>({ it.dueAt }, { -it.priority }, { it.createdAt })
        SortMode.PRIORITY -> compareBy<TaskEntity>({ -it.priority }, { it.dueAt }, { it.createdAt })
        SortMode.CREATED -> compareBy<TaskEntity>({ -it.createdAt }, { it.dueAt })
    }
    return tasks
        .sortedWith(comparator)
        .groupBy { TaskDateUtils.dayKey(it.dueAt) }
        .map { (dateKey, items) ->
            TaskGroup(
                dateKey = dateKey,
                relation = dateRelation(dateKey, nowMillis),
                tasks = items
            )
        }
        .sortedBy { it.dateKey }
}
