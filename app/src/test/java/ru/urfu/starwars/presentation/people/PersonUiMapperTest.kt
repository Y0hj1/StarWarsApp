package ru.urfu.starwars.presentation.people

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.urfu.starwars.domain.model.Person

class PersonUiMapperTest {

    private val luke = Person(
        id = 1,
        name = "Luke Skywalker",
        gender = "male",
        birthYear = "19BBY",
        height = "172",
        mass = "77",
        hairColor = "blond",
        skinColor = "fair",
        eyeColor = "blue",
        homeworldId = 1,
        filmsCount = 4,
        vehiclesCount = 2,
        starshipsCount = 2,
    )

    @Test
    fun `list item contains formatted subtitle`() {
        val item = PersonUiMapper.toListItem(luke)
        assertEquals("Пол: мужской · Рождён: 19BBY", item.subtitle)
    }

    @Test
    fun `details contain formatted stats`() {
        val details = PersonUiMapper.toDetails(luke)
        assertEquals("172 см", details.height.value)
        assertEquals("77 кг", details.mass.value)
        assertEquals("4", details.films.value)
        assertEquals("Рождён: 19BBY", details.birthLabel)
    }

    @Test
    fun `unknown values are shown as dash`() {
        val details = PersonUiMapper.toDetails(luke.copy(mass = "unknown", gender = "n/a"))
        assertEquals("—", details.mass.value)
        assertEquals("Пол: —", details.genderLabel)
    }
}
