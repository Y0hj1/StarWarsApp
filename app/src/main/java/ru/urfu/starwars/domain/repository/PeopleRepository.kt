package ru.urfu.starwars.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.urfu.starwars.domain.model.PeoplePage
import ru.urfu.starwars.domain.model.Person

interface PeopleRepository {

    suspend fun getPeople(page: Int): PeoplePage

    /** Сначала отдаёт персонажа из кэша (если он есть), затем свежие данные из сети. */
    fun observePerson(id: Int): Flow<Person>
}
