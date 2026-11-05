package ru.urfu.starwars.feature.profile.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.urfu.starwars.di.IoDispatcher
import ru.urfu.starwars.feature.profile.domain.model.DownloadedFile
import ru.urfu.starwars.feature.profile.domain.model.Profile
import ru.urfu.starwars.feature.profile.domain.repository.ProfileRepository
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Профиль — небольшая структура из нескольких строк, поэтому храним её JSON-файлом
 * во внутреннем хранилище приложения (filesDir/profile/profile.json).
 * Аватарку копируем рядом, чтобы URI не «протух» после перезапуска или удаления фото из галереи.
 */
@Singleton
class ProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ProfileRepository {

    private val profileDir = File(context.filesDir, "profile")
    private val profileFile = File(profileDir, "profile.json")
    private val avatarsDir = File(profileDir, "avatars")

    private val cache = MutableStateFlow<Profile?>(null)
    private val mutex = Mutex()

    private val fileProviderAuthority = "${context.packageName}.fileprovider"

    override fun observeProfile(): Flow<Profile> = flow {
        if (cache.value == null) {
            mutex.withLock {
                if (cache.value == null) cache.value = readProfile()
            }
        }
        emitAll(cache.filterNotNull())
    }.flowOn(ioDispatcher)

    override suspend fun saveProfile(profile: Profile) = withContext(ioDispatcher) {
        mutex.withLock {
            profileDir.mkdirs()
            // Пишем во временный файл и переименовываем, чтобы не получить битый JSON при сбое
            val tmp = File(profileDir, "profile.json.tmp")
            tmp.writeText(json.encodeToString(ProfileDto.serializer(), profile.toDto()))
            if (!tmp.renameTo(profileFile)) {
                profileFile.delete()
                tmp.renameTo(profileFile)
            }
            cache.value = profile
            deleteUnusedAvatars(keep = profile.avatarUri)
        }
    }

    override suspend fun importAvatar(sourceUri: String): String = withContext(ioDispatcher) {
        avatarsDir.mkdirs()
        val target = File(avatarsDir, "avatar_${System.currentTimeMillis()}.jpg")
        val input = context.contentResolver.openInputStream(Uri.parse(sourceUri))
            ?: throw IOException("Не удалось открыть изображение")
        input.use { source -> target.outputStream().use { source.copyTo(it) } }
        Uri.fromFile(target).toString()
    }

    override fun createCameraOutputUri(): String {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, fileProviderAuthority, file).toString()
    }

    override suspend fun downloadResume(url: String): DownloadedFile = withContext(ioDispatcher) {
        val request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Сервер ответил ${response.code}")
            val body = response.body ?: throw IOException("Пустой ответ")

            val extension = MimeTypeMap.getFileExtensionFromUrl(url).orEmpty()
            val mimeType = body.contentType()?.let { "${it.type}/${it.subtype}" }
                ?.takeUnless { it == "application/octet-stream" }
                ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
                ?: "application/pdf"
            val fileExtension = extension.ifBlank {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "pdf"
            }

            // Папка Download в app-specific external storage: разрешения не нужны ни на одной версии Android
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: File(context.filesDir, "downloads")
            dir.mkdirs()
            val file = File(dir, "resume.$fileExtension")
            body.byteStream().use { input -> file.outputStream().use { input.copyTo(it) } }

            DownloadedFile(
                uri = FileProvider.getUriForFile(context, fileProviderAuthority, file).toString(),
                mimeType = mimeType,
            )
        }
    }

    private fun readProfile(): Profile = try {
        if (profileFile.exists()) {
            json.decodeFromString(ProfileDto.serializer(), profileFile.readText()).toDomain()
        } else {
            Profile()
        }
    } catch (e: Exception) {
        Profile()
    }

    private fun deleteUnusedAvatars(keep: String?) {
        val keepName = keep?.let { Uri.parse(it).lastPathSegment }
        avatarsDir.listFiles()?.forEach { file ->
            if (file.name != keepName) file.delete()
        }
    }
}
