package ru.urfu.starwars.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.starwars.domain.model.PeoplePage
import ru.urfu.starwars.domain.model.Person

interface PeopleRepository {

    suspend fun getPeople(page: Int, search: String? = null): PeoplePage

    /**
     * Сначала отдаёт персонажа из кэша (память или избранное в БД), затем свежие данные из сети.
     * Без интернета карточка из избранного всё равно откроется.
     */
    fun observePerson(id: Int): Flow<Person>
}
