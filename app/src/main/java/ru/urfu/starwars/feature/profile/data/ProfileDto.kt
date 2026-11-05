package ru.urfu.starwars.feature.profile.data

import kotlinx.serialization.Serializable
import ru.urfu.starwars.feature.profile.domain.model.Profile

/** Формат файла profile.json во внутреннем хранилище. */
@Serializable
internal data class ProfileDto(
    val fullName: String = "",
    val position: String = "",
    val avatarUri: String? = null,
    val resumeUrl: String = "",
    val lessonTime: String = "",
)

internal fun ProfileDto.toDomain() = Profile(
    fullName = fullName,
    position = position,
    avatarUri = avatarUri,
    resumeUrl = resumeUrl,
    lessonTime = lessonTime,
)

internal fun Profile.toDto() = ProfileDto(
    fullName = fullName,
    position = position,
    avatarUri = avatarUri,
    resumeUrl = resumeUrl,
    lessonTime = lessonTime,
)
