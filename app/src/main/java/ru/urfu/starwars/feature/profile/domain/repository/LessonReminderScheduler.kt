package ru.urfu.starwars.feature.profile.domain.repository

import ru.urfu.starwars.feature.profile.domain.model.ReminderSchedule

interface LessonReminderScheduler {
    fun schedule(hour: Int, minute: Int, ownerName: String): ReminderSchedule
    fun cancel()
}
