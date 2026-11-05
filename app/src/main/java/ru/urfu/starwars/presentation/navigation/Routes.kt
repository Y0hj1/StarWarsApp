package ru.urfu.starwars.presentation.navigation

import kotlinx.serialization.Serializable

// Графы вкладок нижней навигации
@Serializable data object PeopleGraph
@Serializable data object FavoritesGraph
@Serializable data object ProfileGraph

// Экраны
@Serializable data object PeopleListRoute
@Serializable data class PersonDetailsRoute(val id: Int)
@Serializable data object FiltersRoute
@Serializable data object FavoritesRoute
@Serializable data class FavoriteDetailsRoute(val id: Int)
@Serializable data object ProfileRoute
