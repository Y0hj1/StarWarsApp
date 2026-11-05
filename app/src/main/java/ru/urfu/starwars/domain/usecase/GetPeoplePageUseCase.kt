package ru.urfu.starwars.domain.usecase

import ru.urfu.starwars.domain.model.PeopleFilter
import ru.urfu.starwars.domain.model.PeoplePage
import ru.urfu.starwars.domain.repository.PeopleRepository
import javax.inject.Inject

/**
 * Загружает страницу с учётом фильтра: поиск по имени выполняет сервер,
 * пол и «только пилоты» отсеиваются здесь.
 */
class GetPeoplePageUseCase @Inject constructor(
    private val repository: PeopleRepository,
) {
    suspend operator fun invoke(page: Int, filter: PeopleFilter = PeopleFilter.DEFAULT): Result<PeoplePage> =
        runCatchingCancellable {
            val search = filter.nameQuery.trim().ifEmpty { null }
            val raw = repository.getPeople(page = page, search = search)
            raw.copy(people = raw.people.filter(filter::matches))
        }
}
