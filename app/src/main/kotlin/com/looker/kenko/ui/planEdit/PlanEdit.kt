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

package com.looker.kenko.ui.planEdit

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.looker.kenko.BuildConfig
import com.looker.kenko.R
import com.looker.kenko.data.model.Exercise
import com.looker.kenko.data.model.MuscleGroups
import com.looker.kenko.data.model.PlannedSet
import com.looker.kenko.data.model.Routine
import com.looker.kenko.data.model.RoutineExercise
import com.looker.kenko.ui.addSet.AddSetSheet
import com.looker.kenko.ui.components.BackButton
import com.looker.kenko.ui.components.ErrorSnackbar
import com.looker.kenko.ui.components.KenkoButton
import com.looker.kenko.ui.components.SetGroupHeader
import com.looker.kenko.ui.components.SwipeToDeleteBox
import com.looker.kenko.ui.components.setLabels
import com.looker.kenko.ui.extensions.plus
import com.looker.kenko.ui.planEdit.components.DeleteRoutineDialog
import com.looker.kenko.ui.planEdit.components.RoutineNameDialog
import com.looker.kenko.ui.selectExercise.SelectExercise
import com.looker.kenko.ui.sessionDetail.components.SetItem
import com.looker.kenko.ui.theme.KenkoIcons
import com.looker.kenko.ui.theme.KenkoTheme
import com.looker.kenko.ui.theme.KenkoThemeConfig
import com.looker.kenko.ui.theme.KenkoThemePreviewParameter
import kotlinx.coroutines.launch

@Composable
fun PlanEdit(
    viewModel: PlanEditViewModel,
    onBackPress: () -> Unit,
    onAddNewExerciseClick: (name: String?, target: MuscleGroups?) -> Unit,
) {
    val pageStage by viewModel.pageState.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val restSeconds by viewModel.restSeconds.collectAsStateWithLifecycle()
    val suggestions = stringArrayResource(R.array.routine_name_suggestions)
    BackHandler {
        viewModel.onBackPress(pageStage, onBackPress)
    }
    FullEdit(
        snackbarHostState = viewModel.snackbarState,
        stage = pageStage,
        fab = {
            PlanEditFAB(
                pageStage = pageStage,
                onClick = {
                    if (pageStage == PlanEditStage.NameEdit) {
                        viewModel.saveName()
                    } else {
                        viewModel.onAddClick()
                    }
                },
            )
        },
        onBackPress = { viewModel.onBackPress(pageStage, onBackPress) },
        actions = {
            if (pageStage == PlanEditStage.PlanEdit && state.selectedRoutine != null) {
                IconButton(onClick = viewModel::openRenameRoutine) {
                    Icon(
                        painter = KenkoIcons.Rename,
                        contentDescription = stringResource(R.string.label_rename_day),
                    )
                }
                IconButton(onClick = viewModel::openDeleteRoutine) {
                    Icon(
                        painter = KenkoIcons.Delete,
                        contentDescription = stringResource(R.string.label_delete_day),
                    )
                }
            }
        },
        onDoneClick = { viewModel.done(onBackPress) },
        onDebugMockClick = viewModel::debugFillMockData,
    ) { stage ->
        when (stage) {
            PlanEditStage.NameEdit -> {
                val isNameAlreadyUsed by viewModel.isNameAlreadyUsed.collectAsStateWithLifecycle()
                NameEdit(
                    state = viewModel.planNameState,
                    isNameAlreadyUsed = isNameAlreadyUsed,
                    onSaveClick = viewModel::saveName,
                )
            }

            PlanEditStage.PlanEdit -> {
                RoutineEditor(
                    state = state,
                    suggestions = suggestions.toList(),
                    onSelectRoutine = viewModel::selectRoutine,
                    onAddRoutine = viewModel::openCreateRoutine,
                    onCreateRoutine = viewModel::createRoutine,
                    onRemoveExercise = viewModel::removeExercise,
                    onAddSet = viewModel::openAddSet,
                    onEditSet = viewModel::openEditSet,
                    onRemoveSet = viewModel::removeSet,
                )
            }
        }
    }

    when (val sheet = state.sheet) {
        PlanEditSheet.AddExercise -> AddExerciseSheet(
            onDismiss = viewModel::closeSheet,
            onDone = viewModel::addExercise,
            onAddNewExerciseClick = onAddNewExerciseClick,
        )

        is PlanEditSheet.EditSet -> key(sheet.id) {
            AddSetSheet(
                exercise = sheet.exercise,
                initial = sheet.initial,
                defaultRestSeconds = sheet.exercise.restSeconds ?: restSeconds,
                isEdit = sheet.setId != null,
                onDismiss = viewModel::closeSheet,
                onDone = viewModel::saveSet,
            )
        }

        null -> Unit
    }

    when (val dialog = state.dialog) {
        RoutineDialog.Create -> {
            val usedNames = remember(state.routines) {
                state.routines.map { it.name.lowercase() }.toSet()
            }
            RoutineNameDialog(
                title = stringResource(R.string.label_new_day),
                initialName = "",
                suggestions = suggestions.filter { it.lowercase() !in usedNames },
                onConfirm = viewModel::createRoutine,
                onDismiss = viewModel::dismissDialog,
            )
        }

        is RoutineDialog.Rename -> RoutineNameDialog(
            title = stringResource(R.string.label_rename_day),
            initialName = dialog.routine.name,
            suggestions = emptyList(),
            onConfirm = { name -> viewModel.renameRoutine(dialog.routine.id, name) },
            onDismiss = viewModel::dismissDialog,
        )

        is RoutineDialog.Delete -> DeleteRoutineDialog(
            name = dialog.routine.name,
            onConfirm = { viewModel.deleteRoutine(dialog.routine.id) },
            onDismiss = viewModel::dismissDialog,
        )

        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullEdit(
    snackbarHostState: SnackbarHostState,
    stage: PlanEditStage,
    fab: @Composable () -> Unit,
    onBackPress: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    onDoneClick: (() -> Unit)? = null,
    onDebugMockClick: (() -> Unit)? = null,
    ui: @Composable (stage: PlanEditStage) -> Unit,
) {
    Scaffold(
        floatingActionButton = fab,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                ErrorSnackbar(data = it)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onBackPress) },
                actions = {
                    actions()
                    // Debug only: adds random past workouts
                    if (BuildConfig.DEBUG && stage == PlanEditStage.PlanEdit) {
                        IconButton(onClick = { onDebugMockClick?.invoke() }) {
                            Icon(painter = KenkoIcons.Add, contentDescription = "Mock data")
                        }
                    }
                    if (onDoneClick != null && stage == PlanEditStage.PlanEdit) {
                        FilledTonalIconButton(onClick = onDoneClick) {
                            Icon(
                                painter = KenkoIcons.Done,
                                contentDescription = stringResource(R.string.label_save),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            modifier = Modifier.padding(innerPadding + PaddingValues(horizontal = 16.dp)),
            targetState = stage,
            label = "Plan edit stage",
            transitionSpec = {
                when (targetState) {
                    PlanEditStage.NameEdit -> {
                        slideInHorizontally { -it / 2 } + fadeIn() togetherWith
                            slideOutHorizontally { it / 2 } + fadeOut()
                    }

                    PlanEditStage.PlanEdit -> {
                        slideInHorizontally { it / 2 } + fadeIn() togetherWith
                            slideOutHorizontally { -it / 2 } + fadeOut()
                    }
                } using SizeTransform(clip = false)
            },
        ) {
            ui(it)
        }
    }
}

@Composable
private fun PlanEditFAB(
    pageStage: PlanEditStage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KenkoButton(
        modifier = modifier,
        onClick = onClick,
        label = {
            AnimatedContent(
                targetState = pageStage,
                label = "FAB label",
                transitionSpec = {
                    when (targetState) {
                        PlanEditStage.NameEdit -> {
                            slideInVertically { it } + fadeIn() togetherWith
                                slideOutVertically { -it } + fadeOut()
                        }

                        PlanEditStage.PlanEdit -> {
                            slideInVertically { -it } + fadeIn() togetherWith
                                slideOutVertically { it } + fadeOut()
                        }
                    } using SizeTransform(clip = false)
                },
            ) {
                if (it == PlanEditStage.NameEdit) {
                    Text(stringResource(R.string.label_next))
                } else {
                    Text(stringResource(R.string.label_add))
                }
            }
        },
        icon = {
            AnimatedContent(
                targetState = pageStage,
                label = "FAB icon",
                transitionSpec = {
                    when (targetState) {
                        PlanEditStage.NameEdit -> {
                            slideInHorizontally { it * 2 } + fadeIn() togetherWith
                                slideOutHorizontally { -it * 2 } + fadeOut()
                        }

                        PlanEditStage.PlanEdit -> {
                            slideInHorizontally { -it * 2 } + fadeIn() togetherWith
                                slideOutHorizontally { it * 2 } + fadeOut()
                        }
                    } using SizeTransform(clip = false)
                },
            ) {
                if (it == PlanEditStage.NameEdit) {
                    Icon(
                        painter = KenkoIcons.ArrowForward,
                        contentDescription = stringResource(R.string.label_next),
                    )
                } else {
                    Icon(
                        painter = KenkoIcons.Add,
                        contentDescription = stringResource(R.string.label_add),
                    )
                }
            }
        },
    )
}

@Composable
private fun NameEdit(
    state: TextFieldState,
    isNameAlreadyUsed: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlanName(
        planName = state,
        error = isNameAlreadyUsed,
        onNext = { onSaveClick() },
        modifier = modifier.fillMaxSize(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseSheet(
    onDismiss: () -> Unit,
    onDone: (Exercise) -> Unit,
    onAddNewExerciseClick: (name: String?, target: MuscleGroups?) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(sheetState = state, onDismissRequest = onDismiss) {
        SelectExercise(
            onRequestNewExercise = onAddNewExerciseClick,
            onDone = { exercise ->
                scope.launch {
                    onDone(exercise)
                    state.hide()
                }.invokeOnCompletion {
                    if (!state.isVisible) onDismiss()
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RoutineEditor(
    state: PlanEditState,
    suggestions: List<String>,
    onSelectRoutine: (Int) -> Unit,
    onAddRoutine: () -> Unit,
    onCreateRoutine: (String) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onAddSet: (RoutineExercise) -> Unit,
    onEditSet: (RoutineExercise, PlannedSet) -> Unit,
    onRemoveSet: (Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    PlanExercise(
        modifier = Modifier.fillMaxSize(),
        header = {
            PlanDaysHeader(
                routines = state.routines,
                selectedId = state.selectedRoutineId,
                onSelect = onSelectRoutine,
                onAddRoutine = onAddRoutine,
            )
        },
        items = {
            item { Spacer(Modifier.height(12.dp)) }
            when {
                state.routines.isEmpty() -> item(key = "first_day") {
                    FirstDay(
                        suggestions = suggestions,
                        onCreate = onCreateRoutine,
                        onCustomClick = onAddRoutine,
                    )
                }

                state.exercises.isEmpty() -> item(key = "no_exercises") {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.no_exercises_yet),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                else -> state.exercises.forEach { routineExercise ->
                    val exercise = routineExercise.exercise
                    item(key = "exercise_${routineExercise.id}") {
                        SwipeToDeleteBox(
                            modifier = Modifier
                                .animateItem()
                                .padding(top = 8.dp)
                                .clip(MaterialTheme.shapes.small),
                            onDismiss = {
                                focusManager.clearFocus()
                                onRemoveExercise(routineExercise.id)
                            },
                        ) {
                            SetGroupHeader(
                                name = exercise.name,
                                subtitle = stringResource(exercise.target.stringRes),
                            ) {
                                FilledTonalIconButton(
                                    shapes = IconButtonShapes(
                                        shape = MaterialShapes.Circle.toShape(),
                                        pressedShape = MaterialShapes.Cookie6Sided.toShape(),
                                    ),
                                    onClick = { onAddSet(routineExercise) },
                                ) {
                                    Icon(
                                        painter = KenkoIcons.Add,
                                        contentDescription = stringResource(R.string.label_add),
                                    )
                                }
                            }
                        }
                    }
                    val labels = setLabels(routineExercise.sets.map { it.type })
                    itemsIndexed(
                        items = routineExercise.sets,
                        key = { _, set -> "set_${set.id}" },
                    ) { index, set ->
                        SwipeToDeleteBox(
                            modifier = Modifier.animateItem(),
                            onDismiss = { onRemoveSet(set.id) },
                        ) {
                            SetItem(
                                modifier = Modifier.padding(vertical = 4.dp),
                                repsOrDuration = set.repsOrDuration,
                                weight = set.weight,
                                isIsometric = exercise.isIsometric,
                                restSeconds = set.restSeconds,
                                onClick = { onEditSet(routineExercise, set) },
                                title = { Text(labels[index]) },
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(96.dp)) }
        },
    )
}

@Preview
@Composable
private fun RoutineEditorPreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        RoutineEditor(
            state = PlanEditState(
                routines = listOf(
                    Routine(id = 1, planId = 1, name = "Push", position = 0),
                    Routine(id = 2, planId = 1, name = "Pull", position = 1),
                ),
                selectedRoutineId = 1,
            ),
            suggestions = listOf("Push", "Pull", "Legs"),
            onSelectRoutine = {},
            onAddRoutine = {},
            onCreateRoutine = {},
            onRemoveExercise = {},
            onAddSet = {},
            onEditSet = { _, _ -> },
            onRemoveSet = {},
        )
    }
}
