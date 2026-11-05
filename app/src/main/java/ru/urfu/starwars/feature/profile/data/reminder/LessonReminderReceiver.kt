package ru.urfu.starwars.feature.profile.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ru.urfu.starwars.R

/** Получает сигнал от AlarmManager и показывает пуш-уведомление о начале пары. */
class LessonReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_LESSON_REMINDER) return
        val ownerName = intent.getStringExtra(EXTRA_OWNER_NAME).orEmpty().ifBlank { "Студент" }

        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Открываем приложение по нажатию. Берём launch-интент пакета, чтобы не зависеть от MainActivity
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_lesson)
            .setContentTitle("Пара начинается!")
            .setContentText("$ownerName, начинается ваша любимая пара по мобильной разработке")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$ownerName, начинается ваша любимая пара по мобильной разработке. Да пребудет с вами Сила!"),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания о парах",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = "Уведомление о начале любимой пары" }
        ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_LESSON_REMINDER = "ru.urfu.starwars.action.LESSON_REMINDER"
        const val EXTRA_OWNER_NAME = "owner_name"
        private const val CHANNEL_ID = "lesson_reminders"
        private const val NOTIFICATION_ID = 42
    }
}
