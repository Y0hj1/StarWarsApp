package ru.urfu.starwars.presentation.filters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.urfu.starwars.domain.model.GenderFilter
import ru.urfu.starwars.domain.model.PeopleFilter
import ru.urfu.starwars.domain.usecase.ObserveFilterUseCase
import ru.urfu.starwars.domain.usecase.SaveFilterUseCase
import ru.urfu.starwars.presentation.common.FilterBadgeCache
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class FiltersUiState(
    val filter: PeopleFilter = PeopleFilter.DEFAULT,
    val isLoaded: Boolean = false,
    val isSaved: Boolean = false,
    val saveError: String? = null,
)

@HiltViewModel
class FiltersViewModel @Inject constructor(
    private val observeFilter: ObserveFilterUseCase,
    private val saveFilter: SaveFilterUseCase,
    private val badgeCache: FilterBadgeCache,
) : ViewModel() {

    private val _state = MutableStateFlow(FiltersUiState())
    val state: StateFlow<FiltersUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = observeFilter().first()
            _state.update { it.copy(filter = saved, isLoaded = true) }
        }
    }

    fun onNameChange(value: String) = editFilter { copy(nameQuery = value) }

    fun onGenderChange(value: GenderFilter) = editFilter { copy(gender = value) }

    fun onPilotsOnlyChange(value: Boolean) = editFilter { copy(pilotsOnly = value) }

    fun onReset() = editFilter { PeopleFilter.DEFAULT }

    /** «Готово»: сохраняем в DataStore, обновляем бейдж и закрываем экран. */
    fun onApply() {
        val filter = _state.value.filter
        viewModelScope.launch {
            try {
                saveFilter(filter)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(saveError = "Не удалось сохранить фильтры") }
                return@launch
            }
            badgeCache.setFiltersApplied(!filter.copy(nameQuery = filter.nameQuery.trim()).isDefault)
            _state.update { it.copy(isSaved = true, saveError = null) }
        }
    }

    private inline fun editFilter(block: PeopleFilter.() -> PeopleFilter) {
        _state.update { it.copy(filter = it.filter.block()) }
    }
}
