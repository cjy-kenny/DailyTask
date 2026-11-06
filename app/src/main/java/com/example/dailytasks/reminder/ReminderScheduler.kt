package com.example.dailytasks.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dailytasks.MainActivity
import com.example.dailytasks.R
import com.example.dailytasks.data.AppDatabase
import com.example.dailytasks.data.TaskEntity
import com.example.dailytasks.util.TaskDateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 使用 AlarmManager 在任务到期时发送本地通知。
 * 只对未完成、开启提醒且到期时间在未来的任务排期。
 */
object ReminderScheduler {

    const val CHANNEL_ID = "task_reminders"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_TASK_DUE_AT = "extra_task_due_at"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun schedule(context: Context, task: TaskEntity) {
        if (task.isCompleted || !task.reminderEnabled) {
            cancel(context, task.id)
            return
        }
        if (task.dueAt <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return
        ensureChannel(context)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            task.dueAt,
            reminderPendingIntent(context, task.id, task.title, task.dueAt)
        )
    }

    fun cancel(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return
        alarmManager.cancel(
            reminderPendingIntent(context, taskId, title = "", dueAt = 0L)
        )
    }

    private fun reminderPendingIntent(
        context: Context,
        taskId: Long,
        title: String,
        dueAt: Long
    ): PendingIntent {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, title)
            putExtra(EXTRA_TASK_DUE_AT, dueAt)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(ReminderScheduler.EXTRA_TASK_ID, 0L)
        if (taskId == 0L) return
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TASK_TITLE).orEmpty()
        val dueAt = intent.getLongExtra(ReminderScheduler.EXTRA_TASK_DUE_AT, 0L)

        ReminderScheduler.ensureChannel(context)
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val contentIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (dueAt > 0L) {
            context.getString(R.string.notification_due_text, TaskDateUtils.formatTime(dueAt))
        } else {
            context.getString(R.string.notification_due_now)
        }

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title.ifBlank { context.getString(R.string.app_name) })
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        runCatching { manager.notify(taskId.toInt(), notification) }
    }
}

/**
 * 设备重启后 AlarmManager 中的提醒会丢失，这里重新排期所有待提醒任务。
 */
class BootRescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pending = AppDatabase.getInstance(context)
                    .taskDao()
                    .pendingReminders(System.currentTimeMillis())
                ReminderScheduler.ensureChannel(context)
                pending.forEach { ReminderScheduler.schedule(context, it) }
            } catch (_: Exception) {
                // 数据库尚未就绪时忽略，下一次打开应用会补排期
            } finally {
                pendingResult.finish()
            }
        }
    }
}
