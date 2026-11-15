package ru.urfu.starwars.presentation.people

import ru.urfu.starwars.domain.model.Person

/** Готовая к отрисовке строка списка: экран ничего не форматирует сам. */
data class PersonListItemUi(
    val id: Int,
    val name: String,
    val subtitle: String,
)

/** Плашка статистики на экране деталей: заголовок и уже отформатированное значение. */
data class StatUi(
    val title: String,
    val value: String,
)

/** Строка «подпись — значение» в карточке деталей. */
data class InfoRowUi(
    val label: String,
    val value: String,
)

/** Готовые данные для экрана деталей. */
data class PersonDetailsUi(
    val id: Int,
    val name: String,
    val genderLabel: String,
    val birthLabel: String,
    val height: StatUi,
    val mass: StatUi,
    val films: StatUi,
    val infoRows: List<InfoRowUi>,
)

/**
 * Перевод доменной модели в UI-модели. Вызывается из ViewModel:
 * подготовка текста для отображения — её работа, а не Composable-функций.
 */
object PersonUiMapper {

    fun toListItem(person: Person): PersonListItemUi = PersonListItemUi(
        id = person.id,
        name = person.name,
        subtitle = "Пол: ${gender(person.gender)} · Рождён: ${birthYear(person.birthYear)}",
    )

    fun toDetails(person: Person): PersonDetailsUi = PersonDetailsUi(
        id = person.id,
        name = person.name,
        genderLabel = "Пол: ${gender(person.gender)}",
        birthLabel = "Рождён: ${birthYear(person.birthYear)}",
        height = StatUi(title = "Рост", value = measure(person.height, "см")),
        mass = StatUi(title = "Вес", value = measure(person.mass, "кг")),
        films = StatUi(title = "Фильмы", value = person.filmsCount.toString()),
        infoRows = buildList {
            add(InfoRowUi("Волосы", attribute(person.hairColor)))
            add(InfoRowUi("Кожа", attribute(person.skinColor)))
            add(InfoRowUi("Глаза", attribute(person.eyeColor)))
            add(InfoRowUi("Транспорт", person.vehiclesCount.toString()))
            add(InfoRowUi("Корабли", person.starshipsCount.toString()))
            person.homeworldId?.let { add(InfoRowUi("Родная планета", "#$it")) }
        },
    )

    private fun gender(raw: String): String = when (raw.lowercase()) {
        "male" -> "мужской"
        "female" -> "женский"
        "hermaphrodite" -> "гермафродит"
        "n/a", "none" -> "—"
        "unknown" -> "неизвестен"
        else -> raw
    }

    private fun birthYear(raw: String): String =
        if (raw.equals("unknown", ignoreCase = true)) "неизвестно" else raw

    /** "172" -> "172 см", "1,358" -> "1,358 кг", "unknown" -> "—" */
    private fun measure(raw: String, unit: String): String =
        if (raw.replace(",", "").toDoubleOrNull() != null) "$raw $unit" else "—"

    private fun attribute(raw: String): String = when (raw.lowercase()) {
        "n/a", "none", "unknown" -> "—"
        else -> raw
    }
}
