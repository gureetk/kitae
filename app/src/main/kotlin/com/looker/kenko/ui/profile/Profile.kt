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

package com.looker.kenko.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.looker.kenko.R
import com.looker.kenko.data.model.PlanStat
import com.looker.kenko.ui.components.BackButton
import com.looker.kenko.ui.components.HealthQuotes
import com.looker.kenko.ui.components.OutlineBorder
import com.looker.kenko.ui.components.SecondaryBorder
import com.looker.kenko.ui.extensions.plus
import com.looker.kenko.ui.extensions.vertical
import com.looker.kenko.ui.profile.components.FrequencyGraph
import com.looker.kenko.ui.theme.KenkoIcons
import com.looker.kenko.ui.theme.KenkoTheme
import com.looker.kenko.ui.theme.KenkoThemeConfig
import com.looker.kenko.ui.theme.KenkoThemePreviewParameter
import com.looker.kenko.ui.theme.end
import com.looker.kenko.ui.theme.numbers
import com.looker.kenko.ui.theme.start

@Composable
fun Profile(
    viewModel: ProfileViewModel,
    onBackPress: () -> Unit,
    onExercisesClick: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onPlanClick: () -> Unit,
    onPlanEdit: (Int) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Profile(
        state = state,
        onBackPress = onBackPress,
        onSettingsClick = onSettingsClick,
        onPlanEdit = onPlanEdit,
        onPlanClick = onPlanClick,
        onAddExerciseClick = onAddExerciseClick,
        onExercisesClick = onExercisesClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Profile(
    state: ProfileUiState,
    onBackPress: () -> Unit,
    onSettingsClick: () -> Unit,
    onPlanClick: () -> Unit,
    onPlanEdit: (Int) -> Unit,
    onAddExerciseClick: () -> Unit,
    onExercisesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.label_profile)) },
                navigationIcon = { BackButton(onBackPress) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(painter = KenkoIcons.Settings, contentDescription = null)
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding + PaddingValues(horizontal = 16.dp))
                .verticalScroll(rememberScrollState()),
        ) {
            if (state.isPlanAvailable) {
                ActivitySection(state.activity)
                Spacer(modifier = Modifier.height(12.dp))
                CurrentPlanCard(
                    onPlanClick = onPlanClick,
                    onPlanEdit = { onPlanEdit(state.planId) },
                    name = state.planName,
                )
            } else {
                SelectPlanCard(onPlanClick)
            }
            Spacer(modifier = Modifier.height(12.dp))
            ExerciseCard(
                numberOfExercises = state.numberOfExercises,
                onAddClick = onAddExerciseClick,
                onExercisesClick = onExercisesClick,
            )
            if (state.totalLifts > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                StatCard(
                    label = stringResource(R.string.label_lifts),
                    value = state.totalLifts,
                    icon = KenkoIcons.Reveal,
                )
                Spacer(modifier = Modifier.height(12.dp))
                StatCard(
                    label = stringResource(R.string.label_days),
                    value = state.daysTrained,
                    icon = KenkoIcons.Dawn,
                )
            }
            Spacer(modifier = Modifier.weight(1F))
            HealthQuotes(Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun CurrentPlanCard(
    onPlanClick: () -> Unit,
    onPlanEdit: () -> Unit,
    name: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = CircleShape,
        border = SecondaryBorder,
        onClick = onPlanClick,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.extraLarge,
                onClick = onPlanEdit,
            ) {
                Text(
                    text = stringResource(R.string.label_current_plan_FORMAT, name),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = KenkoIcons.ArrowOutward,
                contentDescription = null,
            )
        }
    }
}

@Composable
fun SelectPlanCard(
    onSelectPlanClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onTertiaryContainer) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .clickable(onClick = onSelectPlanClick)
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.label_select_plan),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.paddingFromBaseline(bottom = 16.dp),
            )

            Icon(
                painter = KenkoIcons.ArrowOutward,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    numberOfExercises: Int,
    onAddClick: () -> Unit,
    onExercisesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val cardShape = MaterialTheme.shapes.extraLarge
        val surfaceShape = remember(cardShape) {
            cardShape.end(16.dp, 16.dp)
        }
        Surface(
            modifier = Modifier.weight(1.5F),
            shape = surfaceShape,
            border = OutlineBorder,
            onClick = onExercisesClick,
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.label_exercise),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = numberOfExercises.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
        }
        val buttonShape = remember(cardShape) {
            cardShape.start(16.dp, 16.dp)
        }
        Box(
            modifier = Modifier
                .weight(1F)
                .fillMaxHeight()
                .clip(buttonShape)
                .clickable(onClick = onAddClick)
                .border(
                    border = SecondaryBorder,
                    shape = buttonShape,
                )
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = KenkoIcons.Add,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                contentDescription = stringResource(R.string.label_add),
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    icon: ImageVector,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        border = SecondaryBorder,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.vertical(false),
                text = label,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displayLarge.numbers(),
            )
            Spacer(modifier = Modifier.weight(1F))
            Icon(
                imageVector = icon,
                tint = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentDescription = null,
                modifier = Modifier.offset(x = 30.dp),
            )
        }
    }
}

@Composable
private fun ActivitySection(
    activity: Map<Int, Int>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.label_activity),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline,
        )
        if (activity.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            FrequencyGraph(activity = activity) { EmptyPlot() }
        } else {
            EmptyPlot()
        }
    }
}

@Composable
private fun EmptyPlot() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.error_not_enough_activity),
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview
@Composable
private fun PlanCard(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        CurrentPlanCard(
            onPlanClick = {},
            onPlanEdit = {},
            name = "Push-Pull-Leg",
        )
    }
}

@Preview
@Composable
private fun EmptyPlanCardPreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        SelectPlanCard({})
    }
}

@Preview
@Composable
private fun ExerciseCardPreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        ExerciseCard(21, {}, {})
    }
}

@Preview
@Composable
private fun ProfileNoPlanPreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        Profile(
            state = ProfileUiState(
                numberOfExercises = 12,
                isPlanAvailable = false,
                planId = 1,
                planName = "Push-Pull-Leg",
                totalLifts = 2,
                activity = emptyMap(),
                planStat = PlanStat(12, 5)
            ),
            onBackPress = { },
            onSettingsClick = { },
            onPlanClick = { },
            onPlanEdit = {},
            onAddExerciseClick = { },
            onExercisesClick = { },
        )
    }
}

@Preview
@Composable
private fun ProfilePreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        Profile(
            state = ProfileUiState(
                numberOfExercises = 12,
                isPlanAvailable = true,
                planId = 1,
                planName = "Push-Pull-Leg",
                totalLifts = 2,
                activity = mapOf(1 to 2, 2 to 1, 3 to 10),
                planStat = PlanStat(12, 5),
            ),
            onBackPress = { },
            onSettingsClick = { },
            onPlanClick = { },
            onPlanEdit = {},
            onAddExerciseClick = { },
            onExercisesClick = { },
        )
    }
}
