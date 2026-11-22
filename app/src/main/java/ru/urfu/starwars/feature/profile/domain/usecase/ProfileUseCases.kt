package ru.urfu.starwars.feature.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.starwars.feature.profile.domain.model.DownloadedFile
import ru.urfu.starwars.feature.profile.domain.model.Profile
import ru.urfu.starwars.feature.profile.domain.model.ReminderSchedule
import ru.urfu.starwars.feature.profile.domain.repository.LessonReminderScheduler
import ru.urfu.starwars.feature.profile.domain.repository.ProfileRepository
import javax.inject.Inject

class ObserveProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    operator fun invoke(): Flow<Profile> = repository.observeProfile()
}

class SaveProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(profile: Profile) = repository.saveProfile(
        profile.copy(
            fullName = profile.fullName.trim(),
            position = profile.position.trim(),
            resumeUrl = profile.resumeUrl.trim(),
            lessonTime = profile.lessonTime.trim(),
        ),
    )
}

/**
 * Ставит напоминание о начале пары на сегодня в указанное время.
 * Если это время сегодня уже прошло — на завтра.
 */
class ScheduleLessonReminderUseCase @Inject constructor(
    private val scheduler: LessonReminderScheduler,
) {
    operator fun invoke(lessonTime: String, ownerName: String): ReminderSchedule {
        val (hour, minute) = requireNotNull(ProfileValidator.parseTime(lessonTime)) {
            "Invalid time: $lessonTime"
        }
        return scheduler.schedule(hour = hour, minute = minute, ownerName = ownerName)
    }
}

class ImportAvatarUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(sourceUri: String): String = repository.importAvatar(sourceUri)
}

class CreateCameraOutputUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    operator fun invoke(): String = repository.createCameraOutputUri()
}

class DownloadResumeUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(url: String): DownloadedFile = repository.downloadResume(url.trim())
}

/** Валидация полей профиля — чистая логика, без Android-зависимостей. */
object ProfileValidator {

    private val urlRegex = Regex("^https?://[^\\s/$.?#].[^\\s]*$", RegexOption.IGNORE_CASE)

    fun validateName(name: String): String? =
        if (name.isBlank()) "Введите ФИО" else null

    private val timeRegex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")

    /** "18:30" -> (18, 30), ерунда -> null */
    fun parseTime(value: String): Pair<Int, Int>? =
        timeRegex.matchEntire(value.trim())?.destructured?.let { (h, m) -> h.toInt() to m.toInt() }

    fun validateLessonTime(value: String): String? = when {
        value.isBlank() -> "Укажите время пары"
        parseTime(value) == null -> "Время должно быть в формате ЧЧ:ММ, например 09:30"
        else -> null
    }

    fun validateResumeUrl(url: String): String? = when {
        url.isBlank() -> null
        !urlRegex.matches(url.trim()) -> "Ссылка должна начинаться с http:// или https://"
        else -> null
    }
}
