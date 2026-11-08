package ru.urfu.starwars.presentation.people.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.urfu.starwars.domain.model.Person
import ru.urfu.starwars.presentation.common.InitialsAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailsScreen(
    onBack: () -> Unit,
    viewModel: PersonDetailsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((state as? PersonDetailsUiState.Content)?.person?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val s = state) {
                PersonDetailsUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is PersonDetailsUiState.Error ->
                    Text(s.message, Modifier.align(Alignment.Center))

                is PersonDetailsUiState.Content ->
                    PersonDetailsContent(s.person)
            }
        }
    }
}

/**
 * Разметка на ConstraintLayout: аватар слева, имя и чипы справа от него,
 * под ними — горизонтальная цепочка из трёх плашек со статистикой,
 * ниже — карточка с внешностью.
 */
@Composable
fun PersonDetailsContent(person: Person, modifier: Modifier = Modifier) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        val (avatar, name, gender, birth, height, mass, films, appearance) = createRefs()

        InitialsAvatar(
            name = person.name,
            size = 96.dp,
            modifier = Modifier.constrainAs(avatar) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            },
        )

        Text(
            text = person.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.constrainAs(name) {
                top.linkTo(avatar.top)
                start.linkTo(avatar.end, margin = 16.dp)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            },
        )

        AssistChip(
            onClick = {},
            label = { Text(person.gender) },
            modifier = Modifier.constrainAs(gender) {
                top.linkTo(name.bottom, margin = 8.dp)
                start.linkTo(name.start)
            },
        )

        AssistChip(
            onClick = {},
            label = { Text("Рождён ${person.birthYear}") },
            modifier = Modifier.constrainAs(birth) {
                top.linkTo(gender.top)
                start.linkTo(gender.end, margin = 8.dp)
            },
        )

        val statsBarrier = createBottomBarrier(avatar, gender, birth, margin = 24.dp)

        StatTile("Рост", "${person.height} см", Modifier.constrainAs(height) {
            top.linkTo(statsBarrier)
            width = Dimension.fillToConstraints
        })
        StatTile("Вес", "${person.mass} кг", Modifier.constrainAs(mass) {
            top.linkTo(statsBarrier)
            width = Dimension.fillToConstraints
        })
        StatTile("Фильмы", person.filmsCount.toString(), Modifier.constrainAs(films) {
            top.linkTo(statsBarrier)
            width = Dimension.fillToConstraints
        })
        createHorizontalChain(height, mass, films, chainStyle = ChainStyle.Spread)

        ElevatedCard(
            modifier = Modifier.constrainAs(appearance) {
                top.linkTo(height.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            },
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Внешность и прочее", style = MaterialTheme.typography.titleMedium)
                InfoRow("Волосы", person.hairColor)
                InfoRow("Кожа", person.skinColor)
                InfoRow("Глаза", person.eyeColor)
                InfoRow("Транспорт", person.vehiclesCount.toString())
                InfoRow("Корабли", person.starshipsCount.toString())
                person.homeworldId?.let { InfoRow("Родная планета", "#$it") }
            }
        }
    }
}

@Composable
private fun StatTile(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.padding(horizontal = 4.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
        ) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider()
    }
}
