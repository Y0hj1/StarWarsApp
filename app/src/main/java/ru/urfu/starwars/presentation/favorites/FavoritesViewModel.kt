package ru.urfu.starwars.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.urfu.starwars.domain.model.Person
import ru.urfu.starwars.domain.usecase.ObserveFavoritesUseCase
import ru.urfu.starwars.domain.usecase.RemoveFavoriteUseCase
import ru.urfu.starwars.presentation.people.PersonListItemUi
import ru.urfu.starwars.presentation.people.PersonUiMapper
import javax.inject.Inject

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Content(val people: List<PersonListItemUi>) : FavoritesUiState
}

/** Избранное читается из Room — работает без интернета. */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavorites: ObserveFavoritesUseCase,
    private val removeFavorite: RemoveFavoriteUseCase,
) : ViewModel() {

    val state: StateFlow<FavoritesUiState> = observeFavorites()
        .map<List<Person>, FavoritesUiState> { people -> FavoritesUiState.Content(people.map(PersonUiMapper::toListItem)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState.Loading)

    fun onRemove(id: Int) {
        viewModelScope.launch { removeFavorite(id) }
    }
}
