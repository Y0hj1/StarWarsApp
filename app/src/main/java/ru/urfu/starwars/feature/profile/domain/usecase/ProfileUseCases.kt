package ru.urfu.starwars.feature.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.starwars.feature.profile.domain.model.DownloadedFile
import ru.urfu.starwars.feature.profile.domain.model.Profile
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
        ),
    )
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

    fun validateResumeUrl(url: String): String? = when {
        url.isBlank() -> null
        !urlRegex.matches(url.trim()) -> "Ссылка должна начинаться с http:// или https://"
        else -> null
    }
}
