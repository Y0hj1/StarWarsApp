package ru.urfu.starwars.feature.profile.domain.model

data class Profile(
    val fullName: String = "",
    val position: String = "",
    /** URI аватарки на устройстве (копия во внутреннем хранилище приложения). */
    val avatarUri: String? = null,
    /** Ссылка на резюме/портфолио в сети, например pdf. */
    val resumeUrl: String = "",
) {
    val isEmpty: Boolean
        get() = fullName.isBlank() && position.isBlank() && avatarUri == null && resumeUrl.isBlank()
}

/** Скачанный файл: content:// URI, который можно отдать просмотрщику, и его MIME-тип. */
data class DownloadedFile(
    val uri: String,
    val mimeType: String,
)
