package ru.urfu.starwars.feature.profile.presentation.view

import ru.urfu.starwars.feature.profile.domain.model.Profile

/** Готовые к отрисовке данные профиля: экран ничего не вычисляет и не форматирует. */
data class ProfileUi(
    val isEmpty: Boolean,
    val displayName: String,
    val position: String?,
    val avatarUri: String?,
    val avatarDescription: String,
    val canOpenResume: Boolean,
    val resumeHint: String?,
)

internal object ProfileUiMapper {

    fun toUi(profile: Profile): ProfileUi = ProfileUi(
        isEmpty = profile.isEmpty,
        displayName = profile.fullName.ifBlank { "Без имени" },
        position = profile.position.takeIf { it.isNotBlank() },
        avatarUri = profile.avatarUri,
        avatarDescription = if (profile.fullName.isBlank()) "Аватар" else "Аватар ${profile.fullName}",
        canOpenResume = profile.resumeUrl.isNotBlank(),
        resumeHint = if (profile.resumeUrl.isBlank()) "Ссылка на резюме не указана" else null,
    )
}
