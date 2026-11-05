package ru.urfu.starwars.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.starwars.domain.model.Person

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<Person>>
    fun observeIsFavorite(id: Int): Flow<Boolean>
    suspend fun add(person: Person)
    suspend fun remove(id: Int)
}
