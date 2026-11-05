package ru.urfu.starwars.presentation.people.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.urfu.starwars.domain.model.Person
import ru.urfu.starwars.presentation.common.InitialsAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleListScreen(
    onPersonClick: (Int) -> Unit,
    viewModel: PeopleListViewModel = viewModel(),
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
                PeopleListUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is PeopleListUiState.Error ->
                    Text(s.message, Modifier.align(Alignment.Center))

                is PeopleListUiState.Content ->
                    PeopleList(people = s.people, onPersonClick = onPersonClick)
            }
        }
    }
}

@Composable
private fun PeopleList(people: List<Person>, onPersonClick: (Int) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
        items(people, key = { it.id }) { person ->
            PersonRow(person = person, onClick = { onPersonClick(person.id) })
            HorizontalDivider()
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
