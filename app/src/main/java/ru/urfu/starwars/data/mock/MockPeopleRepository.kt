package ru.urfu.starwars.data.mock

import ru.urfu.starwars.domain.model.Person

/**
 * Моковые данные — реальные значения из https://swapi.dev/api/people/.
 * В следующей практике заменим загрузкой из сети.
 */
object MockPeopleRepository {

    private val people = listOf(
        Person(1, "Luke Skywalker", "male", "19BBY", "172", "77", "blond", "fair", "blue", 1, 4, 2, 2),
        Person(2, "C-3PO", "n/a", "112BBY", "167", "75", "n/a", "gold", "yellow", 1, 6, 0, 0),
        Person(3, "R2-D2", "n/a", "33BBY", "96", "32", "n/a", "white, blue", "red", 8, 6, 0, 0),
        Person(4, "Darth Vader", "male", "41.9BBY", "202", "136", "none", "white", "yellow", 1, 4, 0, 1),
        Person(5, "Leia Organa", "female", "19BBY", "150", "49", "brown", "light", "brown", 2, 4, 1, 0),
        Person(6, "Owen Lars", "male", "52BBY", "178", "120", "brown, grey", "light", "blue", 1, 3, 0, 0),
        Person(7, "Beru Whitesun lars", "female", "47BBY", "165", "75", "brown", "light", "blue", 1, 3, 0, 0),
        Person(8, "R5-D4", "n/a", "unknown", "97", "32", "n/a", "white, red", "red", 1, 1, 0, 0),
        Person(9, "Biggs Darklighter", "male", "24BBY", "183", "84", "black", "light", "brown", 1, 1, 0, 1),
        Person(10, "Obi-Wan Kenobi", "male", "57BBY", "182", "77", "auburn, white", "fair", "blue-gray", 20, 6, 1, 5),
        Person(11, "Anakin Skywalker", "male", "41.9BBY", "188", "84", "blond", "fair", "blue", 1, 3, 2, 3),
        Person(12, "Wilhuff Tarkin", "male", "64BBY", "180", "unknown", "auburn, grey", "fair", "blue", 21, 2, 0, 0),
        Person(13, "Chewbacca", "male", "200BBY", "228", "112", "brown", "unknown", "blue", 14, 4, 1, 2),
        Person(14, "Han Solo", "male", "29BBY", "180", "80", "brown", "fair", "brown", 22, 3, 0, 2),
        Person(15, "Greedo", "male", "44BBY", "173", "74", "n/a", "green", "black", 23, 1, 0, 0),
        Person(16, "Jabba Desilijic Tiure", "hermaphrodite", "600BBY", "175", "1,358", "n/a", "green-tan, brown", "orange", 24, 3, 0, 0),
        Person(18, "Wedge Antilles", "male", "21BBY", "170", "77", "brown", "fair", "hazel", 22, 3, 1, 1),
        Person(19, "Jek Tono Porkins", "male", "unknown", "180", "110", "brown", "fair", "blue", 26, 1, 0, 1),
        Person(20, "Yoda", "male", "896BBY", "66", "17", "white", "green", "brown", 28, 5, 0, 0),
        Person(21, "Palpatine", "male", "82BBY", "170", "75", "grey", "pale", "yellow", 8, 5, 0, 0),
    )

    fun getPeople(): List<Person> = people

    fun getPerson(id: Int): Person? = people.firstOrNull { it.id == id }
}
