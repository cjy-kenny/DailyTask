package com.example.dailytasks.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailytasks.data.AppDatabase
import com.example.dailytasks.data.TaskEntity
import com.example.dailytasks.data.TaskRepository
import com.example.dailytasks.reminder.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository(
        AppDatabase.getInstance(application).taskDao()
    )

    val tasks: StateFlow<List<TaskEntity>> = repository.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        // 应用启动时补排期，覆盖强制停止、系统清理闹钟等场景
        viewModelScope.launch {
            ReminderScheduler.ensureChannel(application)
            repository.pendingReminders(System.currentTimeMillis())
                .forEach { ReminderScheduler.schedule(application, it) }
        }
    }

    fun addTask(
        title: String,
        dueAt: Long,
        colorIndex: Int,
        note: String,
        priority: Int,
        reminderEnabled: Boolean,
        repeatRule: Int
    ) {
        viewModelScope.launch {
            val task = repository.addTask(
                title = title,
                dueAt = dueAt,
                colorIndex = colorIndex,
                note = note,
                priority = priority,
                reminderEnabled = reminderEnabled,
                repeatRule = repeatRule
            )
            ReminderScheduler.schedule(getApplication(), task)
        }
    }

    fun updateTask(
        task: TaskEntity,
        title: String,
        dueAt: Long,
        colorIndex: Int,
        note: String,
        priority: Int,
        reminderEnabled: Boolean,
        repeatRule: Int
    ) {
        viewModelScope.launch {
            val updated = repository.updateTask(
                task = task,
                title = title,
                dueAt = dueAt,
                colorIndex = colorIndex,
                note = note,
                priority = priority,
                reminderEnabled = reminderEnabled,
                repeatRule = repeatRule
            )
            ReminderScheduler.cancel(getApplication(), task.id)
            ReminderScheduler.schedule(getApplication(), updated)
        }
    }

    fun setTaskCompleted(task: TaskEntity, completed: Boolean) {
        viewModelScope.launch {
            val updated = repository.setCompleted(task, completed)
            if (completed) {
                ReminderScheduler.cancel(getApplication(), task.id)
                repository.createNextOccurrence(updated)?.let { next ->
                    ReminderScheduler.schedule(getApplication(), next)
                }
            } else {
                ReminderScheduler.schedule(getApplication(), updated)
            }
        }
    }

    fun toggleTask(task: TaskEntity) {
        setTaskCompleted(task, !task.isCompleted)
    }

    fun setTasksCompleted(tasks: List<TaskEntity>, completed: Boolean) {
        tasks.filter { it.isCompleted != completed }.forEach { task ->
            setTaskCompleted(task, completed)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            ReminderScheduler.cancel(getApplication(), task.id)
        }
    }

    fun deleteTasks(tasks: List<TaskEntity>) {
        viewModelScope.launch {
            repository.deleteTasks(tasks)
            tasks.forEach { ReminderScheduler.cancel(getApplication(), it.id) }
        }
    }

    fun restoreTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.restoreTask(task)
            ReminderScheduler.schedule(getApplication(), task)
        }
    }

    fun restoreTasks(tasks: List<TaskEntity>) {
        viewModelScope.launch {
            repository.restoreTasks(tasks)
            tasks.forEach { ReminderScheduler.schedule(getApplication(), it) }
        }
    }
}
