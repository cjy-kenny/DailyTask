package com.example.dailytasks.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY due_at ASC, created_at ASC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query(
        """
        UPDATE tasks
        SET is_completed = CASE WHEN is_completed = 1 THEN 0 ELSE 1 END,
            updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun toggleCompletion(id: Long, updatedAt: Long)

    @Query(
        """
        UPDATE tasks
        SET is_completed = :completed,
            completed_at = :completedAt,
            updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun setCompletion(id: Long, completed: Boolean, updatedAt: Long, completedAt: Long?)

    @Query(
        """
        SELECT * FROM tasks
        WHERE is_completed = 0 AND reminder_enabled = 1 AND due_at > :fromMillis
        ORDER BY due_at ASC
        """
    )
    suspend fun pendingReminders(fromMillis: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE title = :title AND due_at = :dueAt LIMIT 1")
    suspend fun findByTitleAndDueAt(title: String, dueAt: Long): TaskEntity?

    @Query("DELETE FROM tasks WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Delete
    suspend fun delete(task: TaskEntity)
}
