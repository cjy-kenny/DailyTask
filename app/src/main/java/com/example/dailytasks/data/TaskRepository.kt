package com.example.dailytasks.data

import com.example.dailytasks.util.TaskDateUtils
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val tasks: Flow<List<TaskEntity>> = taskDao.observeAll()

    suspend fun addTask(
        title: String,
        dueAt: Long,
        colorIndex: Int,
        note: String,
        priority: Int,
        reminderEnabled: Boolean,
        repeatRule: Int
    ): TaskEntity {
        val now = System.currentTimeMillis()
        val task = TaskEntity(
            title = title.trim(),
            dueAt = dueAt,
            note = note.trim(),
            colorIndex = colorIndex,
            priority = priority,
            reminderEnabled = reminderEnabled,
            repeatRule = repeatRule,
            createdAt = now,
            updatedAt = now
        )
        val id = taskDao.insert(task)
        return task.copy(id = id)
    }

    suspend fun updateTask(
        task: TaskEntity,
        title: String,
        dueAt: Long,
        colorIndex: Int,
        note: String,
        priority: Int,
        reminderEnabled: Boolean,
        repeatRule: Int
    ): TaskEntity {
        val updated = task.copy(
            title = title.trim(),
            dueAt = dueAt,
            note = note.trim(),
            colorIndex = colorIndex,
            priority = priority,
            reminderEnabled = reminderEnabled,
            repeatRule = repeatRule,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.update(updated)
        return updated
    }

    suspend fun setCompleted(task: TaskEntity, completed: Boolean): TaskEntity {
        val now = System.currentTimeMillis()
        taskDao.setCompletion(
            id = task.id,
            completed = completed,
            updatedAt = now,
            completedAt = if (completed) now else null
        )
        return task.copy(
            isCompleted = completed,
            completedAt = if (completed) now else null,
            updatedAt = now
        )
    }

    /**
     * 完成重复任务后创建下一次任务；相同标题与时间已存在时跳过，避免重复生成。
     */
    suspend fun createNextOccurrence(task: TaskEntity): TaskEntity? {
        if (task.repeatRule == RepeatRule.NONE) return null
        val nextDueAt = TaskDateUtils.nextOccurrence(task.dueAt, task.repeatRule) ?: return null
        if (taskDao.findByTitleAndDueAt(task.title, nextDueAt) != null) return null

        val now = System.currentTimeMillis()
        val next = TaskEntity(
            title = task.title,
            dueAt = nextDueAt,
            note = task.note,
            colorIndex = task.colorIndex,
            priority = task.priority,
            reminderEnabled = task.reminderEnabled,
            repeatRule = task.repeatRule,
            createdAt = now,
            updatedAt = now
        )
        val id = taskDao.insert(next)
        return next.copy(id = id)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.delete(task)
    }

    suspend fun deleteTasks(tasks: List<TaskEntity>) {
        if (tasks.isEmpty()) return
        taskDao.deleteByIds(tasks.map { it.id })
    }

    /**
     * 撤销删除时按原 id 重新插入，保证列表位置和提醒状态不变。
     */
    suspend fun restoreTask(task: TaskEntity) {
        taskDao.insert(task)
    }

    suspend fun restoreTasks(tasks: List<TaskEntity>) {
        tasks.forEach { taskDao.insert(it) }
    }

    suspend fun pendingReminders(fromMillis: Long): List<TaskEntity> =
        taskDao.pendingReminders(fromMillis)
}
