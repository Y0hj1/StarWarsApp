package ru.urfu.starwars.domain.model

/** Страница списка: SWAPI отдаёт по 10 элементов и ссылку на следующую страницу. */
data class PeoplePage(
    val people: List<Person>,
    val totalCount: Int,
    val hasNextPage: Boolean,
)
