package ru.urfu.starwars.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PeopleResponseDto(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<PersonDto>,
)

@Serializable
data class PersonDto(
    val name: String,
    val height: String = "unknown",
    val mass: String = "unknown",
    @SerialName("hair_color") val hairColor: String = "unknown",
    @SerialName("skin_color") val skinColor: String = "unknown",
    @SerialName("eye_color") val eyeColor: String = "unknown",
    @SerialName("birth_year") val birthYear: String = "unknown",
    val gender: String = "unknown",
    val homeworld: String? = null,
    val films: List<String> = emptyList(),
    val vehicles: List<String> = emptyList(),
    val starships: List<String> = emptyList(),
    val url: String,
)
