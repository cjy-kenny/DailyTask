package com.example.dailytasks.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    @ColumnInfo(name = "due_at")
    val dueAt: Long,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    @ColumnInfo(name = "note", defaultValue = "''")
    val note: String = "",
    @ColumnInfo(name = "color_index", defaultValue = "0")
    val colorIndex: Int = 0,
    @ColumnInfo(name = "priority", defaultValue = "0")
    val priority: Int = 0,
    @ColumnInfo(name = "reminder_enabled", defaultValue = "1")
    val reminderEnabled: Boolean = true,
    @ColumnInfo(name = "repeat_rule", defaultValue = "0")
    val repeatRule: Int = RepeatRule.NONE,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
