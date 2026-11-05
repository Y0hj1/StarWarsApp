package ru.urfu.starwars.data.mapper

import ru.urfu.starwars.data.remote.dto.PeopleResponseDto
import ru.urfu.starwars.data.remote.dto.PersonDto
import ru.urfu.starwars.domain.model.PeoplePage
import ru.urfu.starwars.domain.model.Person

/** "https://swapi.dev/api/people/1/" -> 1 */
internal fun String.swapiId(): Int? = trimEnd('/').substringAfterLast('/').toIntOrNull()

internal fun PersonDto.toDomain(): Person = Person(
    id = requireNotNull(url.swapiId()) { "Bad SWAPI url: $url" },
    name = name,
    gender = gender,
    birthYear = birthYear,
    height = height,
    mass = mass,
    hairColor = hairColor,
    skinColor = skinColor,
    eyeColor = eyeColor,
    homeworldId = homeworld?.swapiId(),
    filmsCount = films.size,
    vehiclesCount = vehicles.size,
    starshipsCount = starships.size,
)

internal fun PeopleResponseDto.toDomain(): PeoplePage = PeoplePage(
    people = results.map { it.toDomain() },
    totalCount = count,
    hasNextPage = next != null,
)
