package ru.urfu.starwars.presentation.people.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.urfu.starwars.data.mock.MockPeopleRepository
import ru.urfu.starwars.presentation.navigation.PersonDetailsRoute
import ru.urfu.starwars.presentation.people.PersonDetailsUi
import ru.urfu.starwars.presentation.people.PersonUiMapper

sealed interface PersonDetailsUiState {
    data object Loading : PersonDetailsUiState
    data class Content(val person: PersonDetailsUi) : PersonDetailsUiState
    data class Error(val message: String) : PersonDetailsUiState
}

/** id персонажа приходит из аргументов навигации через SavedStateHandle. */
class PersonDetailsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val personId = savedStateHandle.toRoute<PersonDetailsRoute>().id

    private val _state = MutableStateFlow<PersonDetailsUiState>(PersonDetailsUiState.Loading)
    val state: StateFlow<PersonDetailsUiState> = _state.asStateFlow()

    init {
        val person = MockPeopleRepository.getPerson(personId)
        _state.value = if (person != null) {
            PersonDetailsUiState.Content(PersonUiMapper.toDetails(person))
        } else {
            PersonDetailsUiState.Error("Персонаж не найден")
        }
    }
}
