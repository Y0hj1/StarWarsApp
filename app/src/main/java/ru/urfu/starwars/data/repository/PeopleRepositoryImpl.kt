package ru.urfu.starwars.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.urfu.starwars.data.local.FavoritesDao
import ru.urfu.starwars.data.local.toDomain
import ru.urfu.starwars.data.mapper.toDomain
import ru.urfu.starwars.data.remote.SwapiApi
import ru.urfu.starwars.di.IoDispatcher
import ru.urfu.starwars.domain.model.PeoplePage
import ru.urfu.starwars.domain.model.Person
import ru.urfu.starwars.domain.repository.PeopleRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class PeopleRepositoryImpl @Inject constructor(
    private val api: SwapiApi,
    private val favoritesDao: FavoritesDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PeopleRepository {

    /** Простой кэш в памяти: всё, что пришло со страниц списка, сразу доступно на деталях. */
    private val personCache = ConcurrentHashMap<Int, Person>()

    override suspend fun getPeople(page: Int, search: String?): PeoplePage = withContext(ioDispatcher) {
        api.getPeople(page = page, search = search).toDomain().also { result ->
            result.people.forEach { personCache[it.id] = it }
        }
    }

    override fun observePerson(id: Int): Flow<Person> = flow {
        val cached = personCache[id] ?: favoritesDao.getById(id)?.toDomain()
        if (cached != null) emit(cached)

        val fresh = try {
            api.getPerson(id).toDomain()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Если есть кэш — показываем его, ошибку пробрасываем только когда показать нечего
            if (cached == null) throw e
            null
        }
        if (fresh != null) {
            personCache[id] = fresh
            if (fresh != cached) emit(fresh)
        }
    }.flowOn(ioDispatcher)
}
