package ru.urfu.starwars.presentation.people.list

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.urfu.starwars.data.mock.MockPeopleRepository
import ru.urfu.starwars.domain.model.Person

sealed interface PeopleListUiState {
    data object Loading : PeopleListUiState
    data class Content(val people: List<Person>) : PeopleListUiState
    data class Error(val message: String) : PeopleListUiState
}

class PeopleListViewModel : ViewModel() {

    private val _state = MutableStateFlow<PeopleListUiState>(PeopleListUiState.Loading)
    val state: StateFlow<PeopleListUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = PeopleListUiState.Content(MockPeopleRepository.getPeople())
    }
}
