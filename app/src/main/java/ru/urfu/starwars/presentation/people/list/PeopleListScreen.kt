package ru.urfu.starwars.presentation.people.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import ru.urfu.starwars.domain.model.Person
import ru.urfu.starwars.presentation.common.FullScreenError
import ru.urfu.starwars.presentation.common.FullScreenLoading
import ru.urfu.starwars.presentation.common.InitialsAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleListScreen(
    onPersonClick: (Int) -> Unit,
    viewModel: PeopleListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Персонажи") }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val s = state) {
                PeopleListUiState.Loading -> FullScreenLoading()

                is PeopleListUiState.Error ->
                    FullScreenError(message = s.message, onRetry = viewModel::loadFirstPage)

                is PeopleListUiState.Content ->
                    PullToRefreshBox(
                        isRefreshing = s.isRefreshing,
                        onRefresh = viewModel::refresh,
                    ) {
                        PeopleList(
                            state = s,
                            onPersonClick = onPersonClick,
                            onLoadMore = viewModel::loadMore,
                        )
                    }
            }
        }
    }
}

@Composable
private fun PeopleList(
    state: PeopleListUiState.Content,
    onPersonClick: (Int) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    // Когда до конца списка остаётся 3 элемента — просим следующую страницу
    LaunchedEffect(listState, state.canLoadMore, state.people.size) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - 3
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(state.people, key = { it.id }) { person ->
            PersonRow(person = person, onClick = { onPersonClick(person.id) })
            HorizontalDivider()
        }
        item(key = "footer") {
            ListFooter(state = state, onRetry = onLoadMore)
        }
    }
}

@Composable
private fun ListFooter(state: PeopleListUiState.Content, onRetry: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.isLoadingMore -> CircularProgressIndicator()
            state.loadMoreError != null -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    state.loadMoreError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = onRetry) { Text("Повторить") }
            }
            !state.canLoadMore -> Text(
                "Все персонажи загружены: ${state.people.size}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun PersonRow(person: Person, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingContent = { InitialsAvatar(person.name) },
        headlineContent = { Text(person.name) },
        supportingContent = { Text("Пол: ${person.gender} · Рождён: ${person.birthYear}") },
    )
}
