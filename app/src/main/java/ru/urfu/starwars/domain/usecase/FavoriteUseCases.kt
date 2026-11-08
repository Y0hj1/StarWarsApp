package ru.urfu.starwars.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.urfu.starwars.domain.model.Person
import ru.urfu.starwars.domain.repository.FavoritesRepository
import javax.inject.Inject

class ObserveFavoritesUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    operator fun invoke(): Flow<List<Person>> = repository.observeFavorites()
}

class ObserveIsFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    operator fun invoke(id: Int): Flow<Boolean> = repository.observeIsFavorite(id)
}

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(person: Person, isFavorite: Boolean) {
        if (isFavorite) repository.remove(person.id) else repository.add(person)
    }
}

class RemoveFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository,
) {
    suspend operator fun invoke(id: Int) = repository.remove(id)
}
