package ru.urfu.starwars.domain.usecase

import ru.urfu.starwars.domain.model.PeoplePage
import ru.urfu.starwars.domain.repository.PeopleRepository
import javax.inject.Inject

class GetPeoplePageUseCase @Inject constructor(
    private val repository: PeopleRepository,
) {
    suspend operator fun invoke(page: Int): Result<PeoplePage> =
        runCatchingCancellable { repository.getPeople(page) }
}
