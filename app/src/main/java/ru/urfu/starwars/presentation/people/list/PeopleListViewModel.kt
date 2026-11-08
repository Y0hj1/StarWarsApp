package ru.urfu.starwars.presentation.people.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.starwars.domain.model.Person
import ru.urfu.starwars.domain.usecase.GetPeoplePageUseCase
import ru.urfu.starwars.presentation.common.toUserMessage
import javax.inject.Inject

sealed interface PeopleListUiState {
    data object Loading : PeopleListUiState
    data class Error(val message: String) : PeopleListUiState
    data class Content(
        val people: List<Person>,
        val canLoadMore: Boolean,
        val isLoadingMore: Boolean = false,
        val loadMoreError: String? = null,
        val isRefreshing: Boolean = false,
    ) : PeopleListUiState
}

@HiltViewModel
class PeopleListViewModel @Inject constructor(
    private val getPeoplePage: GetPeoplePageUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<PeopleListUiState>(PeopleListUiState.Loading)
    val state: StateFlow<PeopleListUiState> = _state.asStateFlow()

    private var nextPage = 1
    private var loadJob: Job? = null

    init {
        loadFirstPage()
    }

    /** Первая загрузка и повтор после полноэкранной ошибки. */
    fun loadFirstPage() {
        loadJob?.cancel()
        _state.value = PeopleListUiState.Loading
        loadJob = viewModelScope.launch { fetchFirstPage() }
    }

    /** Pull-to-refresh: список остаётся на экране, пока грузится новый. */
    fun refresh() {
        val current = _state.value as? PeopleListUiState.Content ?: return loadFirstPage()
        loadJob?.cancel()
        _state.value = current.copy(isRefreshing = true, isLoadingMore = false, loadMoreError = null)
        loadJob = viewModelScope.launch { fetchFirstPage() }
    }

    private suspend fun fetchFirstPage() {
        getPeoplePage(page = 1)
            .onSuccess { page ->
                nextPage = 2
                _state.value = PeopleListUiState.Content(
                    people = page.people,
                    canLoadMore = page.hasNextPage,
                )
            }
            .onFailure { error ->
                val current = _state.value
                _state.value = if (current is PeopleListUiState.Content) {
                    current.copy(isRefreshing = false, loadMoreError = error.toUserMessage())
                } else {
                    PeopleListUiState.Error(error.toUserMessage())
                }
            }
    }

    /** Подгрузка следующей страницы при прокрутке к концу списка. */
    fun loadMore() {
        val current = _state.value as? PeopleListUiState.Content ?: return
        if (!current.canLoadMore || current.isLoadingMore || current.isRefreshing) return

        _state.value = current.copy(isLoadingMore = true, loadMoreError = null)
        loadJob = viewModelScope.launch {
            getPeoplePage(page = nextPage)
                .onSuccess { page ->
                    nextPage++
                    _state.update { s ->
                        (s as? PeopleListUiState.Content)?.copy(
                            people = (s.people + page.people).distinctBy { it.id },
                            canLoadMore = page.hasNextPage,
                            isLoadingMore = false,
                        ) ?: s
                    }
                }
                .onFailure { error ->
                    _state.update { s ->
                        (s as? PeopleListUiState.Content)?.copy(
                            isLoadingMore = false,
                            loadMoreError = error.toUserMessage(),
                        ) ?: s
                    }
                }
        }
    }
}
