package ru.urfu.starwars.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import ru.urfu.starwars.presentation.common.StubScreen
import ru.urfu.starwars.presentation.favorites.FavoritesScreen
import ru.urfu.starwars.presentation.filters.FiltersScreen
import ru.urfu.starwars.presentation.people.details.PersonDetailsScreen
import ru.urfu.starwars.presentation.people.list.PeopleListScreen

private data class BottomTab(
    val graph: Any,
    val isSelected: (NavDestination) -> Boolean,
    val title: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    BottomTab(PeopleGraph, { it.hasRoute<PeopleGraph>() }, "Персонажи", Icons.Filled.Groups),
    BottomTab(FavoritesGraph, { it.hasRoute<FavoritesGraph>() }, "Избранное", Icons.Filled.Star),
    BottomTab(ProfileGraph, { it.hasRoute<ProfileGraph>() }, "Профиль", Icons.Filled.Person),
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy
                        ?.any(tab.isSelected) == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.graph) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.title) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PeopleGraph,
            // верхний отступ отдаём TopAppBar каждого экрана
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            navigation<PeopleGraph>(startDestination = PeopleListRoute) {
                composable<PeopleListRoute> {
                    PeopleListScreen(
                        onPersonClick = { id -> navController.navigate(PersonDetailsRoute(id)) },
                        onFiltersClick = { navController.navigate(FiltersRoute) },
                    )
                }
                composable<PersonDetailsRoute> {
                    PersonDetailsScreen(onBack = { navController.popBackStack() })
                }
                composable<FiltersRoute> {
                    FiltersScreen(
                        onBack = { navController.popBackStack() },
                        onDone = { navController.popBackStack() },
                    )
                }
            }
            navigation<FavoritesGraph>(startDestination = FavoritesRoute) {
                composable<FavoritesRoute> {
                    FavoritesScreen(
                        onPersonClick = { id -> navController.navigate(FavoriteDetailsRoute(id)) },
                    )
                }
                composable<FavoriteDetailsRoute> {
                    PersonDetailsScreen(onBack = { navController.popBackStack() })
                }
            }
            navigation<ProfileGraph>(startDestination = ProfileRoute) {
                composable<ProfileRoute> {
                    StubScreen("Профиль", Icons.Filled.Person)
                }
            }
        }
    }
}
