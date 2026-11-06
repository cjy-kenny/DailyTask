package com.example.dailytasks.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class MonthCell(
    val dayMillis: Long,
    val dateKey: String,
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean
)

/**
 * 以设备本地时区为基准的日期时间工具。
 * DatePicker 的选中值是 UTC 零点毫秒数，因此需要在这里与本地日期时间相互转换。
 */
object TaskDateUtils {

    private const val PATTERN_DATE = "yyyy-MM-dd"
    private const val PATTERN_TIME = "HH:mm"
    private const val PATTERN_DATE_TIME = "yyyy-MM-dd HH:mm"
    private const val PATTERN_TODAY = "yyyy年M月d日 E"
    private const val PATTERN_MONTH = "yyyy年M月"
    private const val PATTERN_DAY_WEEKDAY = "M月d日 E"

    private val chineseLocale: Locale = Locale.CHINA

    fun dayKey(millis: Long): String = format(millis, PATTERN_DATE)

    fun formatDate(millis: Long): String = format(millis, PATTERN_DATE)

    fun formatTime(millis: Long): String = format(millis, PATTERN_TIME)

    fun formatDateTime(millis: Long): String = format(millis, PATTERN_DATE_TIME)

    fun formatToday(millis: Long): String = format(millis, PATTERN_TODAY)

    fun formatMonth(millis: Long): String = format(millis, PATTERN_MONTH)

    fun formatDayWithWeekday(millis: Long): String = format(millis, PATTERN_DAY_WEEKDAY)

    fun firstDayOfMonth(millis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun addMonths(millis: Long, delta: Int): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = firstDayOfMonth(millis)
            add(Calendar.MONTH, delta)
        }
        return firstDayOfMonth(calendar.timeInMillis)
    }

    /**
     * 生成周一到周日排列的整月格子，首尾用相邻月份日期补齐，保证每周 7 格。
     */
    fun buildMonthCells(monthMillis: Long): List<MonthCell> {
        val firstDay = Calendar.getInstance().apply {
            timeInMillis = firstDayOfMonth(monthMillis)
        }
        val leading = (firstDay.get(Calendar.DAY_OF_WEEK) + 5) % 7
        val daysInMonth = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)
        val totalCells = (leading + daysInMonth + 6) / 7 * 7

        return (0 until totalCells).map { index ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = firstDayOfMonth(monthMillis)
                add(Calendar.DAY_OF_YEAR, index - leading)
            }
            val localMillis = Calendar.getInstance().apply {
                clear()
                set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    0,
                    0,
                    0
                )
            }.timeInMillis
            MonthCell(
                dayMillis = localMillis,
                dateKey = dayKey(localMillis),
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1,
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
                isCurrentMonth = index in leading until leading + daysInMonth
            )
        }
    }

    /**
     * 生成当前周（周一到周日）的 7 个日期格子。
     */
    fun buildWeekCells(millis: Long): List<MonthCell> {
        val offset = (Calendar.getInstance().apply { timeInMillis = millis }
            .get(Calendar.DAY_OF_WEEK) + 5) % 7
        val mondayLocal = Calendar.getInstance().apply {
            timeInMillis = millis
            add(Calendar.DAY_OF_YEAR, -offset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return (0..6).map { index ->
            val localMillis = Calendar.getInstance().apply {
                timeInMillis = mondayLocal
                add(Calendar.DAY_OF_YEAR, index)
            }.timeInMillis
            val calendar = Calendar.getInstance().apply { timeInMillis = localMillis }
            MonthCell(
                dayMillis = localMillis,
                dateKey = dayKey(localMillis),
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1,
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
                isCurrentMonth = true
            )
        }
    }

    fun atDateKeyWithTime(dayKey: String, hour: Int, minute: Int): Long {
        val parts = dayKey.split("-")
        if (parts.size != 3) return System.currentTimeMillis()
        return Calendar.getInstance().apply {
            clear()
            set(
                parts[0].toIntOrNull() ?: 1970,
                (parts[1].toIntOrNull() ?: 1) - 1,
                parts[2].toIntOrNull() ?: 1,
                hour,
                minute,
                0
            )
        }.timeInMillis
    }

    fun plusDays(millis: Long, days: Int): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.timeInMillis
    }

    /**
     * 按重复规则推算下一次任务时间；不重复时返回 null。
     */
    fun nextOccurrence(millis: Long, repeatRule: Int): Long? = when (repeatRule) {
        com.example.dailytasks.data.RepeatRule.DAILY -> plusDays(millis, 1)
        com.example.dailytasks.data.RepeatRule.WEEKLY -> plusDays(millis, 7)
        com.example.dailytasks.data.RepeatRule.WEEKDAYS -> {
            var next = plusDays(millis, 1)
            while (true) {
                val dayOfWeek = Calendar.getInstance()
                    .apply { timeInMillis = next }
                    .get(Calendar.DAY_OF_WEEK)
                if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) break
                next = plusDays(next, 1)
            }
            next
        }
        else -> null
    }

    /**
     * 以周一为 0 的星期索引，用于周历与统计标签。
     */
    fun weekdayIndexMondayBased(millis: Long): Int =
        (Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.DAY_OF_WEEK) + 5) % 7

    fun hourOf(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.HOUR_OF_DAY)

    fun minuteOf(millis: Long): Int =
        Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.MINUTE)

    /**
     * 本地时间 -> DatePicker 需要的 UTC 零点毫秒数。
     */
    fun toDatePickerMillis(localMillis: Long): Long {
        val local = Calendar.getInstance().apply { timeInMillis = localMillis }
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(
                local.get(Calendar.YEAR),
                local.get(Calendar.MONTH),
                local.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }
        return utc.timeInMillis
    }

    /**
     * DatePicker 的 UTC 选中值与已选时间合并成本地时间。
     */
    fun combineDateAndTime(datePickerMillis: Long, hour: Int, minute: Int): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = datePickerMillis
        }
        val local = Calendar.getInstance().apply {
            clear()
            set(
                utc.get(Calendar.YEAR),
                utc.get(Calendar.MONTH),
                utc.get(Calendar.DAY_OF_MONTH),
                hour,
                minute,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }
        return local.timeInMillis
    }

    private fun format(millis: Long, pattern: String): String =
        SimpleDateFormat(pattern, chineseLocale).format(Date(millis))
}
