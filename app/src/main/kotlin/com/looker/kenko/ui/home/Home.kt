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

package com.looker.kenko.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.looker.kenko.R
import com.looker.kenko.data.model.Routine
import com.looker.kenko.ui.components.DaySelectorChip
import com.looker.kenko.ui.components.FinishWorkoutDialog
import com.looker.kenko.ui.components.KenkoBorderWidth
import com.looker.kenko.ui.components.LiftingQuotes
import com.looker.kenko.ui.components.TertiaryKenkoButton
import com.looker.kenko.ui.components.TickerText
import com.looker.kenko.ui.theme.KenkoIcons
import com.looker.kenko.ui.theme.KenkoTheme
import com.looker.kenko.ui.theme.KenkoThemeConfig
import com.looker.kenko.ui.theme.KenkoThemePreviewParameter
import com.looker.kenko.ui.theme.header

@Composable
fun Home(
    viewModel: HomeViewModel,
    onProfileClick: () -> Unit,
    onSelectPlanClick: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onExploreSessionsClick: () -> Unit,
    onExploreExercisesClick: () -> Unit,
    onOpenSession: (sessionId: Int) -> Unit,
    onEditPlan: (planId: Int, routineId: Int?) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var switchTo by remember { mutableStateOf<Routine?>(null) }
    Home(
        state = state,
        onProfileClick = onProfileClick,
        onSelectPlanClick = onSelectPlanClick,
        onAddExerciseClick = onAddExerciseClick,
        onExploreSessionsClick = onExploreSessionsClick,
        onExploreExercisesClick = onExploreExercisesClick,
        onSelectRoutine = viewModel::selectRoutine,
        onStartClick = {
            val active = state.activeSession
            val routine = state.selectedRoutine
            when {
                state.showActive && active != null -> onOpenSession(active.id)
                routine == null -> {
                    val planId = state.planId
                    if (planId != null) onEditPlan(planId, null)
                }
                routine.exerciseCount == 0 -> onEditPlan(routine.planId, routine.id)
                active != null -> {
                    switchTo = routine
                }
                else -> viewModel.startRoutine(routine.id, onOpenSession)
            }
        },
    )
    val active = state.activeSession
    val target = switchTo
    if (active != null && target != null) {
        FinishWorkoutDialog(
            title = stringResource(R.string.label_unfinished_workout),
            message = stringResource(
                R.string.label_unfinished_workout_desc,
                active.routineName ?: stringResource(R.string.label_workout),
                target.name,
            ),
            incompleteSets = active.totalSets - active.completedSets,
            onKeep = {
                switchTo = null
                viewModel.finishAndStart(active.id, target.id, true, onOpenSession)
            },
            onRemove = {
                switchTo = null
                viewModel.finishAndStart(active.id, target.id, false, onOpenSession)
            },
            onDismiss = { switchTo = null },
        )
    }
}

// TODO: Add current plan indicator on this page
@Composable
private fun Home(
    state: HomeUiData,
    onProfileClick: () -> Unit = {},
    onSelectPlanClick: () -> Unit = {},
    onAddExerciseClick: () -> Unit = {},
    onExploreSessionsClick: () -> Unit = {},
    onExploreExercisesClick: () -> Unit = {},
    onSelectRoutine: (Int) -> Unit = {},
    onStartClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            KenkoTopBar {
                FilledTonalIconButton(onClick = onProfileClick) {
                    Icon(painter = KenkoIcons.Person, contentDescription = null)
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
        ) {
            HorizontalDivider(thickness = KenkoBorderWidth)
            AnimatedContent(
                modifier = Modifier.align(CenterHorizontally),
                targetState = state.isPlanSelected,
                label = "",
            ) { isPlanActive ->
                if (isPlanActive) {
                    Row(
                        modifier = Modifier
                            .widthIn(240.dp, 420.dp)
                            .height(120.dp),
                    ) {
                        ExploreExerciseCard(
                            onClick = onExploreExercisesClick,
                            onLongClick = onAddExerciseClick,
                            modifier = Modifier.weight(1F),
                        )
                        VerticalDivider()
                        SessionHistoryCard(
                            onClick = onExploreSessionsClick,
                            modifier = Modifier.weight(1F),
                        )
                    }
                } else {
                    TickerText(
                        text = stringResource(R.string.label_select_a_plan),
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            HorizontalDivider(thickness = KenkoBorderWidth)
            when {
                state.isLoading -> Spacer(modifier = Modifier.weight(1F))
                state.isPlanSelected -> StartSession(
                    onStartSessionClick = onStartClick,
                    content = { RoutineHeading(state) },
                    routines = {
                        RoutineChips(
                            routines = state.routines,
                            selectedId = state.selectedRoutineId,
                            onSelect = onSelectRoutine,
                        )
                    },
                    buttonText = {
                        val routine = state.selectedRoutine
                        val label = when {
                            state.showActive -> R.string.label_continue_session
                            routine == null || routine.exerciseCount == 0 -> R.string.label_edit_plan
                            else -> R.string.label_start_session
                        }
                        Text(text = stringResource(label))
                    },
                )

                else -> SelectPlan(onSelectPlanClick = onSelectPlanClick)
            }
            LiftingQuotes(Modifier.align(CenterHorizontally))
        }
    }
}

@Composable
private fun ColumnScope.StartSession(
    onStartSessionClick: () -> Unit,
    content: @Composable () -> Unit,
    routines: @Composable () -> Unit,
    buttonText: @Composable () -> Unit,
) {
    Spacer(modifier = Modifier.weight(1F))
    content()
    Spacer(modifier = Modifier.weight(1F))
    routines()
    Spacer(modifier = Modifier.height(16.dp))
    TertiaryKenkoButton(
        modifier = Modifier.align(CenterHorizontally),
        onClick = onStartSessionClick,
        label = buttonText,
        icon = {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = KenkoIcons.ArrowOutward,
                contentDescription = null,
            )
        },
    )
}

@Composable
private fun ColumnScope.RoutineHeading(state: HomeUiData) {
    val resources = LocalContext.current.resources
    val active = state.activeSession
    val routine = state.selectedRoutine
    val label = when {
        state.showActive -> stringResource(R.string.label_in_progress)
        routine == null -> null
        state.isFirstSession -> stringResource(R.string.label_first_session)
        else -> stringResource(R.string.label_up_next)
    }
    val heading = when {
        state.showActive && active != null ->
            active.routineName ?: stringResource(R.string.label_workout)

        routine != null -> routine.name
        else -> stringResource(R.string.label_no_routines)
    }
    val detail = when {
        state.showActive && active != null -> stringResource(
            R.string.label_session_progress,
            active.completedSets,
            active.totalSets,
        )

        routine != null && routine.exerciseCount > 0 -> {
            val exercises = resources.getQuantityString(
                R.plurals.count_exercises,
                routine.exerciseCount,
                routine.exerciseCount,
            )
            val sets = resources.getQuantityString(
                R.plurals.count_sets,
                routine.setCount,
                routine.setCount,
            )
            "$exercises · $sets"
        }

        routine != null -> stringResource(R.string.no_exercises_yet)
        else -> null
    }
    Column(
        modifier = Modifier
            .align(CenterHorizontally)
            .padding(horizontal = 16.dp),
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        AnimatedContent(
            targetState = heading,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "RoutineHeading",
        ) { text ->
            // Long single words can't wrap
            val style = when {
                text.length <= 6 -> MaterialTheme.typography.header()
                text.length <= 9 -> MaterialTheme.typography.header()
                    .copy(fontSize = 60.sp, lineHeight = 56.sp)

                else -> MaterialTheme.typography.header()
                    .copy(fontSize = 48.sp, lineHeight = 46.sp)
            }
            Text(
                text = text,
                style = style.merge(
                    lineBreak = LineBreak.Heading,
                    color = MaterialTheme.colorScheme.primary,
                ),
            )
        }
        if (detail != null) {
            Text(
                text = detail,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun RoutineChips(
    routines: List<Routine>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (routines.isEmpty()) return
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        items(routines, key = { it.id }) { routine ->
            DaySelectorChip(
                selected = routine.id == selectedId,
                onClick = { onSelect(routine.id) },
            ) {
                Text(text = routine.name, maxLines = 1)
            }
        }
    }
}

@Composable
private fun ColumnScope.SelectPlan(
    onSelectPlanClick: () -> Unit,
) {
    Spacer(modifier = Modifier.weight(1F))
    Text(
        modifier = Modifier
            .align(CenterHorizontally)
            .padding(horizontal = 16.dp),
        text = stringResource(R.string.label_selecting_a_plan),
        style = MaterialTheme.typography.header().copy(
            lineBreak = LineBreak.Heading,
        ),
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.weight(1F))
    Button(
        modifier = Modifier.align(CenterHorizontally),
        onClick = onSelectPlanClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        ),
        contentPadding = PaddingValues(
            vertical = 24.dp,
            horizontal = 40.dp,
        ),
    ) {
        Text(text = stringResource(R.string.label_select_plan_one))
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            painter = KenkoIcons.ArrowOutward,
            contentDescription = null,
        )
    }
}

@Composable
private fun ExploreExerciseCard(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HelperCards(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
    ) {
        Text(text = stringResource(R.string.label_explore_exercises))
        Icon(
            painter = KenkoIcons.ArrowOutward,
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .align(TopEnd),
        )
    }
}

@Composable
private fun SessionHistoryCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HelperCards(onClick = onClick, modifier = modifier) {
        Text(text = stringResource(R.string.label_session_history_home))
        Icon(
            painter = KenkoIcons.History,
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp)
                .align(TopEnd),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KenkoTopBar(
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_app_icon),
                    contentDescription = null,
                    modifier = Modifier.clip(CircleShape)
                )
                Text(
                    text = "KENKO",
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        actions = actions,
        modifier = modifier,
    )
}

@Composable
private fun HelperCards(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        shape = shape,
        color = color,
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ProvideTextStyle(textStyle) { content() }
        }
    }
}

private val PreviewRoutines = listOf(
    Routine(id = 1, planId = 1, name = "Push", position = 0, exerciseCount = 5, setCount = 15),
    Routine(id = 2, planId = 1, name = "Pull", position = 1, exerciseCount = 5, setCount = 15),
    Routine(id = 3, planId = 1, name = "Legs", position = 2, exerciseCount = 4, setCount = 12),
)

@Preview
@Composable
private fun HomePreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        Home(
            state = HomeUiData(
                planId = 1,
                routines = PreviewRoutines,
                selectedRoutineId = 2,
            ),
        )
    }
}

@Preview
@Composable
private fun NoRoutinesPreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        Home(state = HomeUiData(planId = 1))
    }
}

@Preview
@Composable
private fun FirstStartHomePreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        Home(
            state = HomeUiData(
                isPlanSelected = false,
                isFirstSession = true,
            ),
        )
    }
}
