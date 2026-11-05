package ru.urfu.starwars.domain.model

/**
 * Настройки фильтрации списка (не более трёх):
 * 1. Поиск по имени — уходит в API как ?search=
 * 2. Пол — фильтруется на клиенте
 * 3. Только пилоты (есть хотя бы один корабль) — на клиенте
 */
data class PeopleFilter(
    val nameQuery: String = "",
    val gender: GenderFilter = GenderFilter.ANY,
    val pilotsOnly: Boolean = false,
) {
    val isDefault: Boolean get() = this == DEFAULT

    fun matches(person: Person): Boolean =
        gender.matches(person.gender) && (!pilotsOnly || person.starshipsCount > 0)

    companion object {
        val DEFAULT = PeopleFilter()
    }
}

enum class GenderFilter(val title: String) {
    ANY("Любой"),
    MALE("Мужской"),
    FEMALE("Женский"),
    DROID("Дроиды (n/a)");

    fun matches(gender: String): Boolean = when (this) {
        ANY -> true
        MALE -> gender.equals("male", ignoreCase = true)
        FEMALE -> gender.equals("female", ignoreCase = true)
        DROID -> gender.equals("n/a", ignoreCase = true)
    }
}
