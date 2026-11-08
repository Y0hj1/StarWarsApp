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
import ru.urfu.starwars.domain.model.PeopleFilter
import ru.urfu.starwars.domain.model.Person
import ru.urfu.starwars.domain.usecase.GetPeoplePageUseCase
import ru.urfu.starwars.domain.usecase.ObserveFilterUseCase
import ru.urfu.starwars.presentation.common.FilterBadgeCache
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
    private val observeFilter: ObserveFilterUseCase,
    private val badgeCache: FilterBadgeCache,
) : ViewModel() {

    private val _state = MutableStateFlow<PeopleListUiState>(PeopleListUiState.Loading)
    val state: StateFlow<PeopleListUiState> = _state.asStateFlow()

    /** Бейдж на кнопке фильтра: читаем общий кэш. */
    val showFilterBadge: StateFlow<Boolean> = badgeCache.showBadge

    private var filter: PeopleFilter? = null
    private var nextPage = 1
    private var loadJob: Job? = null

    init {
        // Фильтр читается из DataStore; при каждом изменении (кнопка «Готово») список перезагружается
        viewModelScope.launch {
            observeFilter().collect { newFilter ->
                badgeCache.setFiltersApplied(!newFilter.isDefault)
                if (newFilter != filter) {
                    filter = newFilter
                    loadFirstPage()
                }
            }
        }
    }

    /** Первая загрузка и повтор после полноэкранной ошибки. */
    fun loadFirstPage() {
        val currentFilter = filter ?: return
        loadJob?.cancel()
        _state.value = PeopleListUiState.Loading
        loadJob = viewModelScope.launch { fetchFirstPage(currentFilter) }
    }

    /** Pull-to-refresh: список остаётся на экране, пока грузится новый. */
    fun refresh() {
        val currentFilter = filter ?: return
        val current = _state.value as? PeopleListUiState.Content ?: return loadFirstPage()
        loadJob?.cancel()
        _state.value = current.copy(isRefreshing = true, isLoadingMore = false, loadMoreError = null)
        loadJob = viewModelScope.launch { fetchFirstPage(currentFilter) }
    }

    private suspend fun fetchFirstPage(currentFilter: PeopleFilter) {
        nextPage = 1
        fetchUntilFilled(currentFilter, alreadyLoaded = emptyList())
            .onSuccess { (people, hasNext) ->
                _state.value = PeopleListUiState.Content(people = people, canLoadMore = hasNext)
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
        val currentFilter = filter ?: return
        val current = _state.value as? PeopleListUiState.Content ?: return
        if (!current.canLoadMore || current.isLoadingMore || current.isRefreshing) return

        _state.value = current.copy(isLoadingMore = true, loadMoreError = null)
        loadJob = viewModelScope.launch {
            fetchUntilFilled(currentFilter, alreadyLoaded = current.people)
                .onSuccess { (people, hasNext) ->
                    _state.update { s ->
                        (s as? PeopleListUiState.Content)?.copy(
                            people = people,
                            canLoadMore = hasNext,
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

    /**
     * Фильтры по полу и кораблям применяются на клиенте, поэтому после фильтрации
     * страница может оказаться почти пустой. Догружаем страницы, пока не наберём
     * хотя бы [MIN_NEW_ITEMS] новых элементов или пока страницы не закончатся.
     */
    private suspend fun fetchUntilFilled(
        currentFilter: PeopleFilter,
        alreadyLoaded: List<Person>,
    ): Result<Pair<List<Person>, Boolean>> {
        var people = alreadyLoaded
        var hasNext: Boolean
        var added = 0
        do {
            val page = getPeoplePage(page = nextPage, filter = currentFilter)
                .getOrElse { return Result.failure(it) }
            nextPage++
            val before = people.size
            people = (people + page.people).distinctBy { it.id }
            added += people.size - before
            hasNext = page.hasNextPage
        } while (hasNext && added < MIN_NEW_ITEMS)
        return Result.success(people to hasNext)
    }

    private companion object {
        const val MIN_NEW_ITEMS = 8
    }
}
