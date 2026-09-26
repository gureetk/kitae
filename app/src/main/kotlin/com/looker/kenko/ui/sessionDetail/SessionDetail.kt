/*
 * Copyright (C) 2026 LooKeR & Contributors
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.looker.kenko.ui.sessionDetail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.looker.kenko.R
import com.looker.kenko.data.model.Exercise
import com.looker.kenko.data.model.FinishMode
import com.looker.kenko.data.model.Set
import com.looker.kenko.data.timer.RestTimerState
import com.looker.kenko.ui.addSet.AddSetSheet
import com.looker.kenko.ui.components.BackButton
import com.looker.kenko.ui.components.FinishWorkoutDialog
import com.looker.kenko.ui.components.SetGroupHeader
import com.looker.kenko.ui.components.SwipeToDeleteBox
import com.looker.kenko.ui.components.TypingText
import com.looker.kenko.ui.components.setLabels
import com.looker.kenko.ui.extensions.plus
import com.looker.kenko.ui.planEdit.components.dayName
import com.looker.kenko.ui.sessionDetail.components.RestTimerBar
import com.looker.kenko.ui.sessionDetail.components.SetItem
import com.looker.kenko.ui.theme.KenkoIcons
import com.looker.kenko.ui.theme.KenkoTheme
import com.looker.kenko.ui.theme.KenkoThemeConfig
import com.looker.kenko.ui.theme.KenkoThemePreviewParameter
import com.looker.kenko.utils.DateFormat
import com.looker.kenko.utils.formatDate
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.datetime.LocalDate

@Composable
fun SessionDetails(
    viewModel: SessionDetailViewModel,
    onBackPress: () -> Unit,
    onHistoryClick: (sessionId: Int) -> Unit,
    onEditPlanClick: (planId: Int, routineId: Int?) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val restTimer by viewModel.restTimer.collectAsStateWithLifecycle()
    val restSeconds by viewModel.restSeconds.collectAsStateWithLifecycle()
    val sheet by viewModel.sheet.collectAsStateWithLifecycle()
    val requestNotifications = rememberNotificationPermissionRequest()
    var showFinishDialog by rememberSaveable { mutableStateOf(false) }

    SessionDetail(
        state = state,
        restTimer = restTimer,
        restSeconds = restSeconds,
        onBackPress = onBackPress,
        onEditPlanClick = onEditPlanClick,
        onHistoryClick = onHistoryClick,
        onRemoveSet = viewModel::removeSet,
        onToggleSet = { set ->
            if (!set.isCompleted && restSeconds > 0) requestNotifications()
            viewModel.toggleSet(set)
        },
        onEditSet = viewModel::openEditSet,
        onAddSet = viewModel::openAddSet,
        onReferenceClick = viewModel::openReference,
        onFinishClick = { incompleteSets ->
            val newSets = (state as? SessionDetailState.Success)?.data?.newSets ?: 0
            if (incompleteSets > 0 || newSets > 0) {
                showFinishDialog = true
            } else {
                viewModel.finish(FinishMode.KeepSkipped, addNewSets = false, onFinished = onBackPress)
            }
        },
        onStartRest = viewModel::startRest,
        onAdjustRest = viewModel::adjustRest,
        onSkipRest = viewModel::skipRest,
    )

    sheet?.let { current ->
        key(current.id) {
            AddSetSheet(
                exercise = current.exercise,
                initial = current.initial,
                isEdit = current.setId != null,
                onDismiss = viewModel::dismissSheet,
                onDone = viewModel::saveSheet,
            )
        }
    }

    if (showFinishDialog) {
        val data = (state as? SessionDetailState.Success)?.data
        FinishWorkoutDialog(
            incompleteSets = data?.incompleteSets ?: 0,
            completedSets = data?.completedSets ?: 0,
            newSets = data?.newSets ?: 0,
            dayName = data?.title,
            onFinish = { mode, addNewSets ->
                showFinishDialog = false
                viewModel.finish(mode, addNewSets, onBackPress)
            },
            onDismiss = { showFinishDialog = false },
        )
    }
}

@Composable
private fun rememberNotificationPermissionRequest(): () -> Unit {
    val context = LocalContext.current
    var asked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    return remember(launcher) {
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !asked) {
                asked = true
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionDetail(
    state: SessionDetailState,
    restTimer: RestTimerState = RestTimerState.Idle,
    restSeconds: Int = 0,
    onBackPress: () -> Unit = {},
    onEditPlanClick: (Int, Int?) -> Unit = { _, _ -> },
    onHistoryClick: (Int) -> Unit = {},
    onRemoveSet: (Int?) -> Unit = {},
    onToggleSet: (Set) -> Unit = {},
    onEditSet: (Set) -> Unit = {},
    onAddSet: (Exercise) -> Unit = {},
    onReferenceClick: (String) -> Unit = {},
    onFinishClick: (incompleteSets: Int) -> Unit = {},
    onStartRest: () -> Unit = {},
    onAdjustRest: (Int) -> Unit = {},
    onSkipRest: () -> Unit = {},
) {
    when (state) {
        is SessionDetailState.Error -> {
            Column(Modifier.statusBarsPadding()) {
                TopAppBar(
                    navigationIcon = {
                        BackButton(onClick = onBackPress)
                    },
                    title = {},
                )
                SessionError(
                    title = stringResource(state.title),
                    message = stringResource(state.errorMessage),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        SessionDetailState.Loading -> {
            Column(Modifier.statusBarsPadding()) {
                TopAppBar(
                    navigationIcon = {
                        BackButton(onClick = onBackPress)
                    },
                    title = {},
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        is SessionDetailState.Success -> {
            val data = state.data
            Box(modifier = Modifier.fillMaxSize()) {
                SetsList(
                    data = data,
                    onBackPress = onBackPress,
                    onEditPlanClick = onEditPlanClick,
                    onHistoryClick = onHistoryClick,
                    onRemoveSet = onRemoveSet,
                    onToggleSet = onToggleSet,
                    onEditSet = onEditSet,
                    onAddSet = onAddSet,
                    onReferenceClick = onReferenceClick,
                )
                if (data.isEditable) {
                    RestTimerBar(
                        state = restTimer,
                        restSeconds = restSeconds,
                        showFinish = true,
                        onStart = onStartRest,
                        onAdjust = onAdjustRest,
                        onSkip = onSkipRest,
                        onFinish = { onFinishClick(data.incompleteSets) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SetsList(
    data: SessionUiData,
    onBackPress: () -> Unit,
    onEditPlanClick: (Int, Int?) -> Unit,
    onHistoryClick: (Int) -> Unit,
    onRemoveSet: (Int?) -> Unit,
    onToggleSet: (Set) -> Unit,
    onEditSet: (Set) -> Unit,
    onAddSet: (Exercise) -> Unit,
    onReferenceClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(360.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        contentPadding = WindowInsets.navigationBars.asPaddingValues(LocalDensity.current) +
            PaddingValues(bottom = if (data.isEditable) 96.dp else 12.dp),
    ) {
        item(
            span = { GridItemSpan(maxLineSpan) },
        ) {
            Header(
                title = data.title,
                performedOn = data.date,
                onBackPress = onBackPress,
                actions = {
                    val previousSessionId = data.previousSessionId
                    if (previousSessionId != null) {
                        IconButton(onClick = { onHistoryClick(previousSessionId) }) {
                            Icon(
                                painter = KenkoIcons.History,
                                contentDescription = null,
                            )
                        }
                    }
                    val planId = data.planId
                    if (planId != null) {
                        IconButton(onClick = { onEditPlanClick(planId, data.routineId) }) {
                            Icon(
                                painter = KenkoIcons.Rename,
                                contentDescription = null,
                            )
                        }
                    }
                },
            )
        }
        if (data.exercises.isEmpty()) {
            item(
                span = { GridItemSpan(maxLineSpan) },
            ) {
                Text(
                    text = stringResource(R.string.no_exercises_yet),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        data.exercises.forEach { (exercise, sets) ->
            item(
                key = "exercise_${exercise.id}",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                SetGroupHeader(name = exercise.name) {
                    if (!exercise.reference.isNullOrBlank()) {
                        FilledTonalIconButton(onClick = { onReferenceClick(exercise.reference) }) {
                            Icon(painter = KenkoIcons.Lightbulb, contentDescription = null)
                        }
                    }
                    if (data.isEditable) {
                        FilledTonalIconButton(
                            shapes = IconButtonShapes(
                                shape = MaterialShapes.Circle.toShape(),
                                pressedShape = MaterialShapes.Cookie6Sided.toShape(),
                            ),
                            onClick = { onAddSet(exercise) },
                        ) {
                            Icon(painter = KenkoIcons.Add, contentDescription = null)
                        }
                    }
                }
            }
            val labels = setLabels(sets.map { it.type })
            itemsIndexed(
                items = sets,
                key = { index, set -> set.id ?: "${exercise.id}_$index" },
            ) { index, set ->
                val item = @Composable {
                    SetItem(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        repsOrDuration = set.repsOrDuration,
                        weight = set.weight,
                        isIsometric = exercise.isIsometric,
                        isCompleted = if (data.isEditable) set.isCompleted else null,
                        isSkipped = !data.isEditable && !set.isCompleted,
                        onCompletedChange = if (data.isEditable) {
                            { onToggleSet(set) }
                        } else {
                            null
                        },
                        onClick = if (data.isEditable) {
                            { onEditSet(set) }
                        } else {
                            null
                        },
                        title = {
                            Text(labels[index])
                        },
                    )
                }
                if (data.isEditable) {
                    SwipeToDeleteBox(
                        modifier = Modifier.animateItem(),
                        onDismiss = { onRemoveSet(set.id) },
                    ) {
                        item()
                    }
                } else {
                    Box(modifier = Modifier.animateItem()) {
                        item()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Header(
    title: String?,
    performedOn: LocalDate,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable (RowScope.() -> Unit),
) {
    val dayName = dayName(performedOn.dayOfWeek)
    val heading = title ?: dayName
    val date = remember(performedOn) {
        formatDate(performedOn, DateFormat.SessionLabel)
    }
    val subtitle = if (heading != dayName) "$dayName · $date" else date
    TopAppBar(
        modifier = modifier,
        actions = actions,
        navigationIcon = { BackButton(onClick = onBackPress) },
        title = {
            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                var startAnimatingDate by remember {
                    mutableStateOf(false)
                }
                TypingText(
                    text = heading,
                    onCompleteListener = {
                        startAnimatingDate = true
                    },
                )
                TypingText(
                    text = subtitle,
                    startTyping = startAnimatingDate,
                    initialDelay = 0.milliseconds,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        },
    )
}

@Composable
private fun SessionError(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Preview
@Composable
private fun SessionDetailPreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        val data = remember {
            SessionDetailState.Success(
                SessionUiData(
                    sessionId = 1,
                    date = LocalDate(2024, 4, 15),
                    title = "Push",
                    exercises = emptyList(),
                    isEditable = true,
                ),
            )
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            SessionDetail(state = data, restSeconds = 90)
        }
    }
}

@Preview
@Composable
private fun SessionErrorPreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        val data = remember {
            SessionDetailState.Error.InvalidSession
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            SessionDetail(state = data)
        }
    }
}
