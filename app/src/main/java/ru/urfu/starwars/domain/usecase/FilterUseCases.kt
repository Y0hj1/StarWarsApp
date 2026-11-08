package ru.urfu.starwars.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.starwars.domain.model.PeopleFilter
import ru.urfu.starwars.domain.repository.FiltersRepository
import javax.inject.Inject

class ObserveFilterUseCase @Inject constructor(
    private val repository: FiltersRepository,
) {
    operator fun invoke(): Flow<PeopleFilter> = repository.observeFilter()
}

class SaveFilterUseCase @Inject constructor(
    private val repository: FiltersRepository,
) {
    suspend operator fun invoke(filter: PeopleFilter) =
        repository.saveFilter(filter.copy(nameQuery = filter.nameQuery.trim()))
}
