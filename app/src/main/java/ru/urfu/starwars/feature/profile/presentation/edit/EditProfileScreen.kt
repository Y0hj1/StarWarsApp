package ru.urfu.starwars.feature.profile.presentation.edit

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.urfu.starwars.feature.profile.presentation.common.ProfileAvatar
import ru.urfu.starwars.presentation.common.FullScreenLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSourcePicker by rememberSaveable { mutableStateOf(false) }

    // Галерея: системный Photo Picker, разрешение на чтение медиа не нужно
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let { viewModel.onGalleryImagePicked(it.toString()) } }

    // Камера: системное приложение камеры пишет снимок в наш файл (FileProvider)
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success -> viewModel.onCameraResult(success) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                EditProfileEvent.Saved -> onSaved()
                is EditProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        if (!state.isLoaded) {
            FullScreenLoading(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                ProfileAvatar(
                    uri = state.avatarUri,
                    contentDescription = "Фото профиля",
                    size = 128.dp,
                    modifier = Modifier.clickable { showSourcePicker = true },
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                if (state.isImportingAvatar) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
            Text(
                "Нажмите на фото, чтобы сменить аватар",
                style = MaterialTheme.typography.bodySmall,
            )

            OutlinedTextField(
                value = state.fullName,
                onValueChange = viewModel::onNameChange,
                label = { Text("ФИО") },
                isError = state.nameError != null,
                supportingText = { state.nameError?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.position,
                onValueChange = viewModel::onPositionChange,
                label = { Text("Должность") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.resumeUrl,
                onValueChange = viewModel::onResumeUrlChange,
                label = { Text("Ссылка на резюме (pdf)") },
                placeholder = { Text("https://…/resume.pdf") },
                isError = state.resumeUrlError != null,
                supportingText = { state.resumeUrlError?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = viewModel::onSave,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Готово")
            }
        }
    }

    if (showSourcePicker) {
        val sheetState = rememberModalBottomSheetState()
        val closeSheet: (then: () -> Unit) -> Unit = { then ->
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                showSourcePicker = false
                then()
            }
        }
        ModalBottomSheet(
            onDismissRequest = { showSourcePicker = false },
            sheetState = sheetState,
        ) {
            Column(Modifier.navigationBarsPadding()) {
                Text(
                    "Откуда взять фото?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                ListItem(
                    headlineContent = { Text("Галерея") },
                    leadingContent = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                    modifier = Modifier.clickable {
                        closeSheet {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        }
                    },
                )
                ListItem(
                    headlineContent = { Text("Камера") },
                    leadingContent = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                    modifier = Modifier.clickable {
                        closeSheet {
                            try {
                                cameraLauncher.launch(Uri.parse(viewModel.prepareCameraUri()))
                            } catch (e: ActivityNotFoundException) {
                                viewModel.onCameraResult(false)
                                scope.launch { snackbarHostState.showSnackbar("Камера недоступна") }
                            }
                        }
                    },
                )
                if (state.avatarUri != null) {
                    ListItem(
                        headlineContent = { Text("Удалить фото") },
                        leadingContent = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        modifier = Modifier.clickable { closeSheet { viewModel.onRemoveAvatar() } },
                    )
                }
            }
        }
    }
}
