package com.example.dailytasks

import com.example.dailytasks.data.RepeatRule
import com.example.dailytasks.data.TaskEntity
import com.example.dailytasks.util.DayRelation
import com.example.dailytasks.util.SortMode
import com.example.dailytasks.util.TaskDateUtils
import com.example.dailytasks.util.dateRelation
import com.example.dailytasks.util.groupTasks
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class TaskDateUtilsTest {

    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    private fun millisOf(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 0,
        minute: Int = 0
    ): Long = Calendar.getInstance().apply {
        clear()
        set(year, month, day, hour, minute, 0)
    }.timeInMillis

    @Test
    fun formatsDateAndTime() {
        val millis = millisOf(2026, Calendar.NOVEMBER, 1, 14, 30)
        assertEquals("2026-11-01", TaskDateUtils.formatDate(millis))
        assertEquals("2026-11-01", TaskDateUtils.dayKey(millis))
        assertEquals("14:30", TaskDateUtils.formatTime(millis))
        assertEquals("2026-11-01 14:30", TaskDateUtils.formatDateTime(millis))
        assertEquals("2026年11月1日 周日", TaskDateUtils.formatToday(millis))
    }

    @Test
    fun datePickerConversionRoundTrip() {
        val original = millisOf(2026, Calendar.NOVEMBER, 1, 14, 30)
        val datePickerMillis = TaskDateUtils.toDatePickerMillis(original)

        val combined = TaskDateUtils.combineDateAndTime(
            datePickerMillis = datePickerMillis,
            hour = 9,
            minute = 15
        )

        assertEquals(9, TaskDateUtils.hourOf(combined))
        assertEquals(15, TaskDateUtils.minuteOf(combined))
        assertEquals("2026-11-01 09:15", TaskDateUtils.formatDateTime(combined))
    }

    @Test
    fun datesAcrossMonthBoundaryKeepCalendarRelations() {
        val now = millisOf(2026, Calendar.NOVEMBER, 1, 10, 0)

        assertEquals(
            DayRelation.TODAY,
            dateRelation(TaskDateUtils.dayKey(now), now)
        )
        assertEquals(
            DayRelation.TOMORROW,
            dateRelation("2026-11-02", now)
        )
        assertEquals(
            DayRelation.YESTERDAY,
            dateRelation("2026-10-31", now)
        )
        assertEquals(
            DayRelation.OTHER,
            dateRelation("2026-11-05", now)
        )
    }

    @Test
    fun tasksAreGroupedByDayAndSortedByDateTime() {
        val now = millisOf(2026, Calendar.NOVEMBER, 1, 10, 0)
        val yesterdayMorning = millisOf(2026, Calendar.OCTOBER, 31, 8, 0)
        val todayEarly = millisOf(2026, Calendar.NOVEMBER, 1, 8, 0)
        val todayLate = millisOf(2026, Calendar.NOVEMBER, 1, 21, 0)
        val tomorrow = millisOf(2026, Calendar.NOVEMBER, 2, 7, 30)

        val tasks = listOf(
            task(id = 1, dueAt = tomorrow),
            task(id = 2, dueAt = todayLate),
            task(id = 3, dueAt = yesterdayMorning),
            task(id = 4, dueAt = todayEarly, isCompleted = true)
        )

        val groups = groupTasks(tasks, now)

        assertEquals(3, groups.size)
        assertEquals(listOf("2026-10-31", "2026-11-01", "2026-11-02"), groups.map { it.dateKey })
        assertEquals(listOf(DayRelation.YESTERDAY, DayRelation.TODAY, DayRelation.TOMORROW), groups.map { it.relation })
        assertEquals(listOf(3L), groups[0].tasks.map { it.id })
        assertEquals(listOf(4L, 2L), groups[1].tasks.map { it.id })
        assertEquals(listOf(1L), groups[2].tasks.map { it.id })
        assertEquals(true, groups[1].tasks.first().isCompleted)
    }

    @Test
    fun monthGridStartsWithMondayAndContainsEveryDay() {
        val november = millisOf(2026, Calendar.NOVEMBER, 1, 0, 0)
        val cells = TaskDateUtils.buildMonthCells(november)

        assertEquals(0, cells.size % 7)
        assertEquals("2026-10-26", cells.first().dateKey)
        assertEquals(6, cells.indexOfFirst { it.dateKey == "2026-11-01" })
        assertEquals(30, cells.count { it.isCurrentMonth })
        assertEquals(30, cells.count { it.dateKey.startsWith("2026-11") })

        val december = TaskDateUtils.buildMonthCells(
            TaskDateUtils.addMonths(november, 1)
        )
        assertEquals("2026年12月", TaskDateUtils.formatMonth(TaskDateUtils.addMonths(november, 1)))
        assertEquals(31, december.count { it.isCurrentMonth })
    }

    @Test
    fun dateKeyWithTimeKeepsLocalDayAndTime() {
        val millis = TaskDateUtils.atDateKeyWithTime("2026-11-01", 14, 30)
        assertEquals("2026-11-01 14:30", TaskDateUtils.formatDateTime(millis))
    }

    @Test
    fun repeatRulesAdvanceToNextValidDate() {
        val friday = millisOf(2026, Calendar.NOVEMBER, 6, 9, 30)

        assertEquals(null, TaskDateUtils.nextOccurrence(friday, RepeatRule.NONE))
        assertEquals(
            "2026-11-07 09:30",
            TaskDateUtils.formatDateTime(TaskDateUtils.nextOccurrence(friday, RepeatRule.DAILY)!!)
        )
        assertEquals(
            "2026-11-13 09:30",
            TaskDateUtils.formatDateTime(TaskDateUtils.nextOccurrence(friday, RepeatRule.WEEKLY)!!)
        )
        assertEquals(
            "2026-11-09 09:30",
            TaskDateUtils.formatDateTime(TaskDateUtils.nextOccurrence(friday, RepeatRule.WEEKDAYS)!!)
        )
    }

    @Test
    fun prioritySortPlacesImportantTasksFirst() {
        val now = millisOf(2026, Calendar.NOVEMBER, 1, 10, 0)
        val morning = millisOf(2026, Calendar.NOVEMBER, 1, 8, 0)
        val afternoon = millisOf(2026, Calendar.NOVEMBER, 1, 15, 0)

        val tasks = listOf(
            task(id = 1, dueAt = morning),
            task(id = 2, dueAt = afternoon, priority = 1),
            task(id = 3, dueAt = afternoon)
        )

        val groups = groupTasks(tasks, now, SortMode.PRIORITY)
        assertEquals(listOf(2L, 1L, 3L), groups.first().tasks.map { it.id })

        val byTime = groupTasks(tasks, now, SortMode.TIME)
        assertEquals(listOf(1L, 2L, 3L), byTime.first().tasks.map { it.id })
    }

    private fun task(
        id: Long,
        dueAt: Long,
        isCompleted: Boolean = false,
        priority: Int = 0
    ) = TaskEntity(
        id = id,
        title = "任务 $id",
        dueAt = dueAt,
        priority = priority,
        isCompleted = isCompleted,
        createdAt = id,
        updatedAt = id
    )
}
