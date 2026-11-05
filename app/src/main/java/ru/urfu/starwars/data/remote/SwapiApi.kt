package ru.urfu.starwars.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.urfu.starwars.data.remote.dto.PeopleResponseDto
import ru.urfu.starwars.data.remote.dto.PersonDto

interface SwapiApi {

    /** https://swapi.dev/api/people/?page=2&search=luke */
    @GET("people/")
    suspend fun getPeople(
        @Query("page") page: Int,
        @Query("search") search: String? = null,
    ): PeopleResponseDto

    /** https://swapi.dev/api/people/1/ */
    @GET("people/{id}/")
    suspend fun getPerson(@Path("id") id: Int): PersonDto
}
