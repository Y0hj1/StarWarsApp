package ru.urfu.starwars.feature.profile.presentation.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.urfu.starwars.feature.profile.domain.usecase.DownloadResumeUseCase
import ru.urfu.starwars.feature.profile.domain.usecase.ObserveProfileUseCase
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class ProfileUiState(
    val profile: ProfileUi? = null,
    val isDownloadingResume: Boolean = false,
)

sealed interface ProfileEvent {
    data class OpenFile(val uri: String, val mimeType: String) : ProfileEvent
    data class ShowMessage(val text: String) : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    private val downloadResume: DownloadResumeUseCase,
) : ViewModel() {

    private val isDownloading = MutableStateFlow(false)

    /** Доменный профиль: из него берём ссылку на резюме. */
    private val profile = observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val state: StateFlow<ProfileUiState> =
        combine(profile, isDownloading) { current, downloading ->
            ProfileUiState(
                profile = current?.let(ProfileUiMapper::toUi),
                isDownloadingResume = downloading,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    /** «Резюме»: скачиваем файл и просим систему открыть его в просмотрщике. */
    fun onResumeClick() {
        val url = profile.value?.resumeUrl.orEmpty()
        if (url.isBlank() || isDownloading.value) return
        viewModelScope.launch {
            isDownloading.value = true
            try {
                val file = downloadResume(url)
                _events.send(ProfileEvent.OpenFile(file.uri, file.mimeType))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(ProfileEvent.ShowMessage("Не удалось скачать резюме: ${e.message}"))
            } finally {
                isDownloading.value = false
            }
        }
    }

    fun onOpenFileFailed() {
        viewModelScope.launch {
            _events.send(ProfileEvent.ShowMessage("Нет приложения, чтобы открыть этот файл"))
        }
    }
}
