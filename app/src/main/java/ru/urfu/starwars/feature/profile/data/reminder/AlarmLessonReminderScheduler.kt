package ru.urfu.starwars.feature.profile.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.urfu.starwars.feature.profile.domain.model.ReminderSchedule
import ru.urfu.starwars.feature.profile.domain.repository.LessonReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** Ставит будильник AlarmManager, который в нужное время разбудит [LessonReminderReceiver]. */
@Singleton
class AlarmLessonReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : LessonReminderScheduler {

    private val alarmManager: AlarmManager = requireNotNull(context.getSystemService())

    override fun schedule(hour: Int, minute: Int, ownerName: String): ReminderSchedule {
        val now = LocalDateTime.now()
        var trigger = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))
        val isTomorrow = !trigger.isAfter(now)
        if (isTomorrow) trigger = trigger.plusDays(1)
        val triggerAtMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val pendingIntent = buildPendingIntent(ownerName)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            // Без разрешения на точные будильники система может сдвинуть срабатывание на несколько минут
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
        return ReminderSchedule(triggerAtMillis = triggerAtMillis, isTomorrow = isTomorrow)
    }

    override fun cancel() {
        alarmManager.cancel(buildPendingIntent(ownerName = ""))
    }

    private fun buildPendingIntent(ownerName: String): PendingIntent {
        val intent = Intent(context, LessonReminderReceiver::class.java)
            .setAction(LessonReminderReceiver.ACTION_LESSON_REMINDER)
            .putExtra(LessonReminderReceiver.EXTRA_OWNER_NAME, ownerName)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val REQUEST_CODE = 1001
    }
}
