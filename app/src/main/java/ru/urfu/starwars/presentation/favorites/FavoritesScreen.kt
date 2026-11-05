package ru.urfu.starwars.presentation.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.urfu.starwars.presentation.common.FullScreenLoading
import ru.urfu.starwars.presentation.common.InitialsAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onPersonClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Избранное") }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val s = state) {
                FavoritesUiState.Loading -> FullScreenLoading()
                is FavoritesUiState.Content ->
                    if (s.people.isEmpty()) {
                        EmptyFavorites()
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(s.people, key = { it.id }) { person ->
                                ListItem(
                                    modifier = Modifier.clickable { onPersonClick(person.id) },
                                    leadingContent = { InitialsAvatar(person.name) },
                                    headlineContent = { Text(person.name) },
                                    supportingContent = {
                                        Text("Пол: ${person.gender} · Рождён: ${person.birthYear}")
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.onRemove(person.id) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                                        }
                                    },
                                )
                                HorizontalDivider()
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun EmptyFavorites() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.StarOutline, contentDescription = null, modifier = Modifier.size(56.dp))
        Text("Здесь пока пусто", style = MaterialTheme.typography.titleMedium)
        Text(
            "Откройте персонажа и нажмите на звёздочку, чтобы добавить его в избранное",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
