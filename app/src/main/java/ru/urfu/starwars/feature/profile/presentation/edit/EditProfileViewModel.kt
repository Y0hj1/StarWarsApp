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
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class EditProfileUiState(
    val isLoaded: Boolean = false,
    val fullName: String = "",
    val position: String = "",
    val resumeUrl: String = "",
    val avatarUri: String? = null,
    val isImportingAvatar: Boolean = false,
    val nameError: String? = null,
    val resumeUrlError: String? = null,
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = isLoaded && !isSaving && !isImportingAvatar && nameError == null && resumeUrlError == null
}

sealed interface EditProfileEvent {
    data object Saved : EditProfileEvent
    data class ShowMessage(val text: String) : EditProfileEvent
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val importAvatar: ImportAvatarUseCase,
    private val createCameraOutput: CreateCameraOutputUseCase,
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
        if (nameError != null || urlError != null) {
            _state.update { it.copy(nameError = nameError, resumeUrlError = urlError) }
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
                    ),
                )
                _events.send(EditProfileEvent.Saved)
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
