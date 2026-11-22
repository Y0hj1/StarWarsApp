package ru.urfu.starwars.feature.profile.presentation.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.urfu.starwars.feature.profile.presentation.common.ProfileAvatar
import ru.urfu.starwars.presentation.common.FullScreenLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.OpenFile -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.parse(event.uri), event.mimeType)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        viewModel.onOpenFileFailed()
                    }
                }
                is ProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Filled.Edit, contentDescription = "Редактировать")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        val profile = state.profile
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (profile == null) {
                FullScreenLoading()
            } else {
                ProfileContent(
                    profile = profile,
                    isDownloading = state.isDownloadingResume,
                    onResumeClick = viewModel::onResumeClick,
                    onEditClick = onEditClick,
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: ProfileUi,
    isDownloading: Boolean,
    onResumeClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileAvatar(uri = profile.avatarUri, contentDescription = profile.avatarDescription, size = 140.dp)

        if (profile.isEmpty) {
            Text("Профиль пока пуст", style = MaterialTheme.typography.titleLarge)
            Text(
                "Нажмите на карандаш, чтобы указать имя, фото и ссылку на резюме",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            OutlinedButton(onClick = onEditClick) { Text("Заполнить профиль") }
            return@Column
        }

        Text(
            profile.displayName,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        profile.position?.let { position ->
            Text(
                position,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onResumeClick,
            enabled = profile.canOpenResume && !isDownloading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isDownloading) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Скачиваем…")
            } else {
                Icon(Icons.Filled.Description, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Резюме")
            }
        }
        profile.resumeHint?.let { hint ->
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
