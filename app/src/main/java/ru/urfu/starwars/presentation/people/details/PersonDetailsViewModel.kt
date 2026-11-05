package ru.urfu.starwars.presentation.people.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import ru.urfu.starwars.domain.model.Person
import ru.urfu.starwars.domain.usecase.ObservePersonUseCase
import ru.urfu.starwars.presentation.common.toUserMessage
import ru.urfu.starwars.presentation.navigation.PersonDetailsRoute
import javax.inject.Inject

sealed interface PersonDetailsUiState {
    data object Loading : PersonDetailsUiState
    data class Content(val person: Person) : PersonDetailsUiState
    data class Error(val message: String) : PersonDetailsUiState
}

@HiltViewModel
class PersonDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observePerson: ObservePersonUseCase,
) : ViewModel() {

    private val personId = savedStateHandle.toRoute<PersonDetailsRoute>().id

    private val _state = MutableStateFlow<PersonDetailsUiState>(PersonDetailsUiState.Loading)
    val state: StateFlow<PersonDetailsUiState> = _state.asStateFlow()

    private var job: Job? = null

    init {
        load()
    }

    fun load() {
        job?.cancel()
        job = observePerson(personId)
            .onStart { _state.value = PersonDetailsUiState.Loading }
            .onEach { _state.value = PersonDetailsUiState.Content(it) }
            .catch { _state.value = PersonDetailsUiState.Error(it.toUserMessage()) }
            .launchIn(viewModelScope)
    }
}
