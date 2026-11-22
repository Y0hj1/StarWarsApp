package ru.urfu.starwars.feature.profile.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.starwars.feature.profile.domain.model.DownloadedFile
import ru.urfu.starwars.feature.profile.domain.model.Profile

interface ProfileRepository {

    fun observeProfile(): Flow<Profile>

    suspend fun saveProfile(profile: Profile)

    /** Копирует выбранную картинку (из галереи или камеры) во внутреннее хранилище, возвращает её URI. */
    suspend fun importAvatar(sourceUri: String): String

    /** URI временного файла, куда камера запишет снимок. */
    fun createCameraOutputUri(): String

    suspend fun downloadResume(url: String): DownloadedFile
}
