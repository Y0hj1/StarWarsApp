package ru.urfu.starwars.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.starwars.domain.model.PeopleFilter

interface FiltersRepository {
    fun observeFilter(): Flow<PeopleFilter>
    suspend fun saveFilter(filter: PeopleFilter)
}
