package ru.urfu.starwars.feature.profile.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.starwars.feature.profile.domain.model.Profile
import ru.urfu.starwars.feature.profile.domain.usecase.CreateCameraOutputUseCase
import ru.urfu.starwars.feature.profile.domain.usecase.ImportAvatarUseCase
import ru.urfu.starwars.feature.profile.domain.usecase.ObserveProfileUseCase
import ru.urfu.starwars.feature.profile.domain.usecase.ProfileValidator
import ru.urfu.starwars.feature.profile.domain.usecase.SaveProfileUseCase
import ru.urfu.starwars.feature.profile.domain.usecase.ScheduleLessonReminderUseCase
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class EditProfileUiState(
    val isLoaded: Boolean = false,
    val fullName: String = "",
    val position: String = "",
    val resumeUrl: String = "",
    val avatarUri: String? = null,
    val lessonTime: String = "",
    val isImportingAvatar: Boolean = false,
    val nameError: String? = null,
    val resumeUrlError: String? = null,
    val lessonTimeError: String? = null,
    val isSaving: Boolean = false,
) {
    /** Сохранение недоступно, пока время пары не заполнено корректно. */
    val canSave: Boolean
        get() = isLoaded && !isSaving && !isImportingAvatar &&
            nameError == null && resumeUrlError == null &&
            ProfileValidator.validateLessonTime(lessonTime) == null
}

sealed interface EditProfileEvent {
    data class Saved(val reminderMessage: String) : EditProfileEvent
    data class ShowMessage(val text: String) : EditProfileEvent
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val importAvatar: ImportAvatarUseCase,
    private val createCameraOutput: CreateCameraOutputUseCase,
    private val scheduleLessonReminder: ScheduleLessonReminderUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events: Flow<EditProfileEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val profile = observeProfile().first()
            _state.update {
                it.copy(
                    isLoaded = true,
                    fullName = profile.fullName,
                    position = profile.position,
                    resumeUrl = profile.resumeUrl,
                    avatarUri = profile.avatarUri,
                    lessonTime = profile.lessonTime,
                )
            }
        }
    }

    fun onNameChange(value: String) = _state.update {
        it.copy(fullName = value, nameError = ProfileValidator.validateName(value))
    }

    fun onPositionChange(value: String) = _state.update { it.copy(position = value) }

    fun onResumeUrlChange(value: String) = _state.update {
        it.copy(resumeUrl = value, resumeUrlError = ProfileValidator.validateResumeUrl(value))
    }

    /**
     * Ручной ввод времени. Оставляем только цифры и двоеточие и сами вставляем «:»,
     * чтобы «1830» превращалось в «18:30». Ерунду всё равно поймает валидация.
     */
    fun onLessonTimeChange(value: String) {
        var cleaned = value.filter { it in '0'..'9' || it == ':' }
        if (':' !in cleaned && cleaned.length > 2) {
            cleaned = cleaned.take(2) + ":" + cleaned.drop(2)
        }
        cleaned = cleaned.take(5)
        _state.update {
            it.copy(lessonTime = cleaned, lessonTimeError = ProfileValidator.validateLessonTime(cleaned))
        }
    }

    /** Время выбрано на виджете часов (TimePicker). */
    fun onLessonTimePicked(hour: Int, minute: Int) {
        val value = String.format(Locale.ROOT, "%02d:%02d", hour, minute)
        _state.update { it.copy(lessonTime = value, lessonTimeError = null) }
    }

    /** Фото выбрано в галерее (Photo Picker). */
    fun onGalleryImagePicked(uri: String) = importAndSet(uri)

    /** Готовим файл для камеры. URI запоминаем в SavedStateHandle — переживёт смерть процесса. */
    fun prepareCameraUri(): String =
        createCameraOutput().also { savedStateHandle[KEY_CAMERA_URI] = it }

    fun onCameraResult(success: Boolean) {
        val uri = savedStateHandle.get<String>(KEY_CAMERA_URI) ?: return
        savedStateHandle.remove<String>(KEY_CAMERA_URI)
        if (success) importAndSet(uri)
    }

    private fun importAndSet(sourceUri: String) {
        viewModelScope.launch {
            _state.update { it.copy(isImportingAvatar = true) }
            try {
                val stored = importAvatar(sourceUri)
                _state.update { it.copy(avatarUri = stored, isImportingAvatar = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isImportingAvatar = false) }
                _events.send(EditProfileEvent.ShowMessage("Не удалось загрузить фото"))
            }
        }
    }

    fun onRemoveAvatar() = _state.update { it.copy(avatarUri = null) }

    /** «Готово»: валидируем, сохраняем и возвращаемся в профиль. */
    fun onSave() {
        val s = _state.value
        val nameError = ProfileValidator.validateName(s.fullName)
        val urlError = ProfileValidator.validateResumeUrl(s.resumeUrl)
        val timeError = ProfileValidator.validateLessonTime(s.lessonTime)
        if (nameError != null || urlError != null || timeError != null) {
            _state.update {
                it.copy(nameError = nameError, resumeUrlError = urlError, lessonTimeError = timeError)
            }
            return
        }
        if (!s.canSave) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                saveProfile(
                    Profile(
                        fullName = s.fullName,
                        position = s.position,
                        avatarUri = s.avatarUri,
                        resumeUrl = s.resumeUrl,
                        lessonTime = s.lessonTime,
                    ),
                )
                // Уведомление содержит имя владельца на момент установки напоминания
                val schedule = scheduleLessonReminder(s.lessonTime, ownerName = s.fullName.trim())
                val day = if (schedule.isTomorrow) "завтра" else "сегодня"
                _events.send(EditProfileEvent.Saved("Напоминание о паре: $day в ${s.lessonTime.trim()}"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false) }
                _events.send(EditProfileEvent.ShowMessage("Не удалось сохранить профиль"))
            }
        }
    }

    private companion object {
        const val KEY_CAMERA_URI = "camera_uri"
    }
}
