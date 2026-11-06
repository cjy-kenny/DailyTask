package com.example.dailytasks

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.dailytasks.data.AppDatabase
import com.example.dailytasks.data.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TaskPersistenceTest {

    private lateinit var context: Context
    private var database: AppDatabase? = null

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"))
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(DATABASE_NAME)
    }

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun tasksCompletionAndOrderSurviveDatabaseReopen() = runBlocking {
        database = openDatabase()
        val dao = database!!.taskDao()

        val firstId = dao.insert(task(title = "早起", dueAt = millisOf(2026, 11, 1, 8)))
        dao.insert(task(title = "写周报", dueAt = millisOf(2026, 11, 1, 20)))
        dao.insert(task(title = "阅读", dueAt = millisOf(2026, 11, 2, 9)))

        dao.toggleCompletion(firstId, updatedAt = 1_000L)
        assertEquals(3, dao.observeAll().first().size)
        assertTrue(dao.observeAll().first().first().isCompleted)

        database!!.close()
        database = openDatabase()

        val reopenedDao = database!!.taskDao()
        val persisted = reopenedDao.observeAll().first()

        assertEquals(listOf("早起", "写周报", "阅读"), persisted.map { it.title })
        assertEquals(listOf(true, false, false), persisted.map { it.isCompleted })

        reopenedDao.toggleCompletion(firstId, updatedAt = 2_000L)
        assertFalse(reopenedDao.observeAll().first().first().isCompleted)

        reopenedDao.delete(persisted.first())
        assertEquals(2, reopenedDao.observeAll().first().size)
    }

    private fun openDatabase(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries()
            .build()

    private fun task(title: String, dueAt: Long): TaskEntity = TaskEntity(
        title = title,
        dueAt = dueAt,
        createdAt = dueAt,
        updatedAt = dueAt
    )

    private fun millisOf(year: Int, month: Int, day: Int, hour: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, 0, 0)
        }.timeInMillis

    companion object {
        private const val DATABASE_NAME = "daily-task-persistence-test.db"
    }
}
