package ru.urfu.starwars.domain.model

/**
 * Персонаж вселенной «Звёздных войн» (ресурс /people из SWAPI).
 * В списке показываем имя, пол и год рождения, остальное — на экране деталей.
 */
data class Person(
    val id: Int,
    val name: String,
    val gender: String,
    val birthYear: String,
    val height: String,
    val mass: String,
    val hairColor: String,
    val skinColor: String,
    val eyeColor: String,
    val homeworldId: Int?,
    val filmsCount: Int,
    val vehiclesCount: Int,
    val starshipsCount: Int,
)
