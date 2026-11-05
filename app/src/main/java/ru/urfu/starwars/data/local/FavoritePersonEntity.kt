package ru.urfu.starwars.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.urfu.starwars.domain.model.Person

@Entity(tableName = "favorite_people")
data class FavoritePersonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val gender: String,
    @ColumnInfo(name = "birth_year") val birthYear: String,
    val height: String,
    val mass: String,
    @ColumnInfo(name = "hair_color") val hairColor: String,
    @ColumnInfo(name = "skin_color") val skinColor: String,
    @ColumnInfo(name = "eye_color") val eyeColor: String,
    @ColumnInfo(name = "homeworld_id") val homeworldId: Int?,
    @ColumnInfo(name = "films_count") val filmsCount: Int,
    @ColumnInfo(name = "vehicles_count") val vehiclesCount: Int,
    @ColumnInfo(name = "starships_count") val starshipsCount: Int,
    @ColumnInfo(name = "added_at") val addedAt: Long,
)

fun FavoritePersonEntity.toDomain() = Person(
    id = id,
    name = name,
    gender = gender,
    birthYear = birthYear,
    height = height,
    mass = mass,
    hairColor = hairColor,
    skinColor = skinColor,
    eyeColor = eyeColor,
    homeworldId = homeworldId,
    filmsCount = filmsCount,
    vehiclesCount = vehiclesCount,
    starshipsCount = starshipsCount,
)

fun Person.toFavoriteEntity(addedAt: Long = System.currentTimeMillis()) = FavoritePersonEntity(
    id = id,
    name = name,
    gender = gender,
    birthYear = birthYear,
    height = height,
    mass = mass,
    hairColor = hairColor,
    skinColor = skinColor,
    eyeColor = eyeColor,
    homeworldId = homeworldId,
    filmsCount = filmsCount,
    vehiclesCount = vehiclesCount,
    starshipsCount = starshipsCount,
    addedAt = addedAt,
)
