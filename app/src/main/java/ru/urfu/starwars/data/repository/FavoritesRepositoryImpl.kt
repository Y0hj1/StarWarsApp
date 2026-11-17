package ru.urfu.starwars.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.urfu.starwars.data.local.FavoritesDao
import ru.urfu.starwars.data.local.toDomain
import ru.urfu.starwars.data.local.toFavoriteEntity
import ru.urfu.starwars.di.IoDispatcher
import ru.urfu.starwars.domain.model.Person
import ru.urfu.starwars.domain.repository.FavoritesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val dao: FavoritesDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<Person>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeIsFavorite(id: Int): Flow<Boolean> = dao.observeIsFavorite(id)

    override suspend fun add(person: Person) = withContext(ioDispatcher) {
        dao.insert(person.toFavoriteEntity())
    }

    override suspend fun remove(id: Int) = withContext(ioDispatcher) {
        dao.delete(id)
    }
}
