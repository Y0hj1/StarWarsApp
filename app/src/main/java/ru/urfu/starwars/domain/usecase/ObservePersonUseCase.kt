package ru.urfu.starwars.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.starwars.domain.model.Person
import ru.urfu.starwars.domain.repository.PeopleRepository
import javax.inject.Inject

class ObservePersonUseCase @Inject constructor(
    private val repository: PeopleRepository,
) {
    operator fun invoke(id: Int): Flow<Person> = repository.observePerson(id)
}
